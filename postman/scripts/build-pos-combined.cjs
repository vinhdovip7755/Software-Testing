const fs = require('fs');
const path = require('path');

const root = path.join(__dirname, '..');
const read = name => JSON.parse(fs.readFileSync(path.join(root, name), 'utf8'));
const bills = require('./pos-bill-requests.json');
const security = read('BookStore_POS_Security.postman_collection.json');
const variables = new Map();
for (const variable of [...bills.variable, ...security.variable]) {
  variables.set(variable.key, variable);
}
for (const key of ['missingBillId', 'outOfStockQuantity', 'pointsUsed']) variables.delete(key);
variables.set('baseUrl', {key: 'baseUrl', value: 'http://127.0.0.1:8080', type: 'string'});
variables.set('customerPhone', {key: 'customerPhone', value: '', type: 'string'});

const event = (listen, code) => ({listen, script: {type: 'text/javascript', exec: code.trim().split('\n')}});
const findEvent = (item, listen) => item.event.find(e => e.listen === listen);
const auth = `
const base = pm.variables.replaceIn('{{baseUrl}}');
const headers = {Authorization: 'Bearer ' + pm.collectionVariables.get('billsRunToken')};
`;
const readBook = `
const bookUrl = pm.variables.replaceIn('{{baseUrl}}/api/books/{{bookId}}');
`;
const stop = `
function missingFixture(reason) {
  pm.test('Chuẩn bị dữ liệu', () => { throw new Error(reason); });
  pm.execution.skipRequest();
}
`;
const roundVnd = `
function roundVnd(amount) {
  const lower = Math.floor(amount);
  return amount - lower === 0.5 ? lower + lower % 2 : Math.round(amount);
}
function subtotalAfterPromotion(receipt) {
  pm.expect(receipt.details, 'Danh sách dòng sách').to.be.an('array').that.is.not.empty;
  return receipt.details.reduce((sum, line) => {
    pm.expect(Number.isFinite(line.totalPrice), 'details[].totalPrice phải là số hữu hạn').to.eql(true);
    return sum + line.totalPrice;
  }, 0);
}
`;
const message = text => `
pm.test('Thông báo nghiệp vụ', () => pm.expect(pm.response.json().message).to.eql(${JSON.stringify(text)}));
`;

const login = bills.item[0];
findEvent(login, 'test').script.exec.push(
  'pm.collectionVariables.unset("securitySalesToken");',
  'pm.collectionVariables.unset("securityBaseline");',
  'pm.collectionVariables.unset("securityTamperedBillId");',
  'if (pm.response.code === 200 && pm.response.json().accessToken) {',
  '  const data = pm.response.json();',
  '  pm.collectionVariables.set("securitySalesToken", data.accessToken);',
  '  pm.collectionVariables.set("securityEmployeeId", data.employee.employeeId);',
  '}'
);
const billCases = bills.item.slice(1);
const byCode = (items, code) => {
  const item = items.find(candidate => candidate.name.startsWith(code + ' - '));
  if (!item) throw new Error('Không tìm thấy request ' + code);
  return item;
};

const guest = byCode(billCases, 'TC_BILL_01');
findEvent(guest, 'test').script.exec.push(...event('test', `
${roundVnd}
if (pm.response.code === 201) {
  const receipt = pm.response.json();
  pm.test('Khách lẻ trả tổng tiền có VAT và không giảm theo hạng', () => {
    const subtotal = subtotalAfterPromotion(receipt);
    pm.expect(receipt.totalBillPrice).to.eql(roundVnd(subtotal * (1 + receipt.tax)));
  });
  pm.collectionVariables.set('securityBaseline', JSON.stringify(receipt));
}
`).script.exec);
guest.request.description = 'Kiểm nhánh khách lẻ và tổng tiền sau VAT. Hóa đơn này đồng thời là dữ liệu đối chứng cho SEC17.';

const member = byCode(billCases, 'TC_BILL_02');
member.request.description = 'Đặt customerPhone là số điện thoại của thành viên có chiết khấu dương. Đọc hạng và điểm trước khi bán, sau đó kiểm tổng tiền và điểm đã cộng.';
member.request.body.raw = member.request.body.raw.replace('{{customerId}}', '{{posMemberId}}');
const memberUrl = `
const customerUrl = base + '/api/customers/phone/' + encodeURIComponent(pm.variables.get('customerPhone') || '');
`;
member.event.unshift(event('prerequest', `
${auth}${memberUrl}${stop}
if (!pm.variables.get('customerPhone')) {
  missingFixture('Cần customerPhone của thành viên có chiết khấu dương.');
} else {
  pm.sendRequest({url: customerUrl, header: headers}, (error, response) => {
    if (error || !response || response.code !== 200) return missingFixture('Không tra được khách qua customerPhone.');
    const customer = response.json();
    if (!(customer.customerId > 0) || !Number.isInteger(customer.point)) {
      return missingFixture('Dữ liệu thành viên hoặc điểm hiện tại không hợp lệ.');
    }
    pm.sendRequest({url: base + '/api/membership-ranks/by-points/' + customer.point, header: headers}, (rankError, rankResponse) => {
      if (rankError || !rankResponse || rankResponse.code !== 200) return missingFixture('Không tra được hạng thành viên.');
      const discount = rankResponse.json().discountPercent;
      if (!(discount > 0 && discount <= 100)) return missingFixture('Thành viên phải có chiết khấu dương.');
      pm.variables.set('posMemberId', customer.customerId);
      pm.variables.set('posMemberBefore', JSON.stringify(customer));
      pm.variables.set('posMemberDiscount', discount);
    });
  });
}
`));
member.event[1] = event('test', `
pm.test('HTTP 201', () => pm.response.to.have.status(201));
${auth}${memberUrl}${roundVnd}
if (pm.response.code === 201) {
  const receipt = pm.response.json();
  const customer = JSON.parse(pm.variables.get('posMemberBefore'));
  const discount = Number(pm.variables.get('posMemberDiscount'));
  pm.test('Áp dụng chiết khấu hạng trước VAT', () => {
    pm.expect(receipt.customerName).to.eql(customer.customerName);
    const subtotal = subtotalAfterPromotion(receipt);
    const expected = roundVnd(subtotal * (1 - discount / 100) * (1 + receipt.tax));
    pm.expect(receipt.totalBillPrice).to.eql(expected);
  });
  pm.sendRequest({url: customerUrl, header: headers}, (error, response) => {
    pm.test('Điểm thành viên tăng đúng số điểm trên hóa đơn', () => {
      pm.expect(error).to.not.exist;
      pm.expect(response.code).to.eql(200);
      pm.expect(receipt.earnedPoints, 'Dữ liệu thử phải phát sinh điểm').to.be.above(0);
      pm.expect(response.json().point).to.eql(customer.point + receipt.earnedPoints);
    });
  });
}
`);

const transfer = byCode(billCases, 'TC_BILL_03');
transfer.request.body.raw = transfer.request.body.raw.replace('{{customerId}}', '0');
transfer.request.description = 'Dùng khách lẻ để cô lập phương thức thanh toán. Đọc lại hóa đơn và kiểm paymentMethodId = 2.';

const stock = byCode(billCases, 'TC_BILL_07');
stock.request.body.raw = stock.request.body.raw.replace('{{outOfStockQuantity}}', '{{posStockPlusOne}}');
stock.request.description = 'Đọc tồn sách đang bán trước khi gửi, thử đặt đúng tồn + 1 và xác nhận tồn không đổi.';
stock.event.unshift(event('prerequest', `
${auth}${readBook}${stop}
pm.sendRequest({url: bookUrl, header: headers}, (error, response) => {
  if (error || !response || response.code !== 200) return missingFixture('Không đọc được sách thử.');
  const book = response.json();
  if (book.status !== 1 || !Number.isInteger(book.quantity) || book.quantity < 0 || book.quantity >= 2147483647) {
    return missingFixture('Cần mã sách đang bán có số tồn hợp lệ.');
  }
  pm.variables.set('posStockBefore', book.quantity);
  pm.variables.set('posStockPlusOne', book.quantity + 1);
});
`));
findEvent(stock, 'test').script.exec.push(...event('test', `
${auth}${readBook}
pm.test('Không trả mã hóa đơn mới', () => pm.expect(pm.response.json()).not.to.have.property('billId'));
pm.sendRequest({url: bookUrl, header: headers}, (error, response) => {
  pm.test('Tồn kho không đổi sau khi từ chối', () => {
    pm.expect(error).to.not.exist;
    pm.expect(response.code).to.eql(200);
    pm.expect(response.json().quantity).to.eql(Number(pm.variables.get('posStockBefore')));
  });
});
`).script.exec);

const inactive = byCode(billCases, 'TC_BILL_08');
inactive.request.description = 'Chọn sách có thật với status = 0. Nếu nhập inactiveBookId, script kiểm tra trạng thái của chính sách đó trước khi gửi.';
inactive.event = [event('prerequest', `
${auth}${stop}
const configured = String(pm.variables.get('inactiveBookId') || '').trim();
pm.sendRequest({url: base + '/api/books', header: headers}, (error, response) => {
  if (error || !response || response.code !== 200) return missingFixture('Không đọc được danh sách sách.');
  const book = response.json().find(item => item.status === 0 && item.bookId > 0 &&
    (!configured || item.bookId === Number(configured)));
  if (!book) return missingFixture('Cần sách có thật với status = 0 để chạy TC_BILL_08.');
  pm.variables.set('inactiveBookId', book.bookId);
});
`), event('test', `
pm.test('HTTP 400', () => pm.response.to.have.status(400));
${message('Sách không tồn tại hoặc đã ngừng bán.')}
`)];

const zeroQuantity = byCode(billCases, 'TC_BILL_09');
zeroQuantity.request.description = 'Chọn quantity = 0 làm giá trị biên đại diện cho nhóm số lượng không dương. Kiểm tồn kho không bị thay đổi.';
const stockRejection = structuredClone(byCode(security.item, 'SEC08').event);
zeroQuantity.event = stockRejection;
const index = findEvent(zeroQuantity, 'test').script.exec.findIndex(line => line.includes('Thông báo lỗi rõ ràng'));
if (index < 0) throw new Error('Thiếu assertion thông báo ở SEC08.');
findEvent(zeroQuantity, 'test').script.exec.splice(index, 1, message('Thông tin sách trong hóa đơn không hợp lệ.').trim());

// Keep one equivalent checkout case in this combined run and reuse the guest receipt as baseline.
const omittedSecurity = new Set(['00', '04', 'SEC03', 'SEC04', 'SEC05', 'SEC08', 'SEC09', 'SEC14', 'SEC15', 'SEC16']);
const securityCases = security.item.filter(item => !omittedSecurity.has(item.name.split(' - ')[0]));
byCode(securityCases, 'SEC17').event.unshift(event('prerequest', `
${stop}
if (!pm.collectionVariables.get('securityBaseline')) missingFixture('Cần hóa đơn đối chứng từ TC_BILL_01.');
`));
byCode(securityCases, 'SEC18').event.unshift(event('prerequest', `
${stop}
if (!pm.collectionVariables.get('securityTamperedBillId')) missingFixture('Cần mã hóa đơn từ SEC17.');
`));

const cleanup = securityCases.slice(-2);
const collection = {
  info: {
    name: 'BookStore POS | Kiểm thử bán hàng',
    schema: 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json',
    description: 'Bộ chạy tổng hợp POS. Dùng environment BookStore_API_Local và DB test. Chọn bookId đang bán, giá dương, tồn sách và lô ít nhất 5 cuốn; customerPhone là thành viên có chiết khấu và đơn mua 2 cuốn phải phát sinh điểm. Cần ít nhất hai sách đang bán còn đủ tồn để chạy ca nhiều dòng. Có thể để trống inactiveBookId để tự tìm sách ngừng bán. Không chạy song song hoặc đổi giá, khuyến mãi trong lượt test. Chạy đủ và đạt dự kiến tạo 5 hóa đơn. Xem POS_SALES_ADDITIONS.md để biết các ca đã gộp.'
  },
  variable: [...variables.values()],
  item: [login, ...billCases, ...securityCases.slice(0, -2), ...require('./pos-sales-extra.cjs')(), ...cleanup]
};

const names = {
  '00': 'Chuẩn bị 01 - Đăng nhập tài khoản có quyền bán hàng',
  '01': 'Chuẩn bị 02 - Đăng nhập nhân viên kho không có quyền bán hàng',
  '02': 'Chuẩn bị 03 - Đăng nhập phiên thử thu hồi',
  '03': 'Chuẩn bị 04 - Đăng xuất phiên thử thu hồi',
  TC_BILL_01: 'Kiểm tổng tiền có VAT cho khách lẻ',
  TC_BILL_02: 'Kiểm chiết khấu hạng và cộng điểm thành viên',
  TC_BILL_03: 'Kiểm phương thức chuyển khoản trên hóa đơn đã lưu',
  TC_BILL_07: 'Đặt nhiều hơn tồn đúng một cuốn và giữ nguyên tồn',
  TC_BILL_09: 'Từ chối số lượng không dương tại biên 0',
  SEC06: 'Từ chối tài khoản không có quyền dù body khai quyền bán hàng'
};
for (const request of collection.item) {
  const code = request.name.split(' - ')[0];
  const status = JSON.stringify(request.event).match(/status\((\d+)\)/)?.[1];
  if (names[code]) {
    const title = /^\d/.test(code) ? names[code] : code + ' - ' + names[code];
    request.name = `${title} [${status}]`;
  }
  for (const entry of request.event || []) new Function(entry.script.exec.join('\n'));
}
fs.writeFileSync(path.join(root, 'BookStore_POS_All.postman_collection.json'),
  JSON.stringify(collection, null, 2) + '\n');
console.log('Sales collection: ' + collection.item.length + ' requests; scripts validated.');
