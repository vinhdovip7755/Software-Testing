const fs = require('fs');
const path = require('path');

const root = path.join(__dirname, '..');
const read = name => JSON.parse(fs.readFileSync(path.join(root, name), 'utf8'));
const bills = read('BookStore_POS_Bills_14_TC.import.postman_collection.json');
const security = read('BookStore_POS_Security.postman_collection.json');
const variables = new Map();
for (const variable of [...bills.variable, ...security.variable]) {
  variables.set(variable.key, variable);
}
variables.delete('missingBillId');
variables.set('baseUrl', {key: 'baseUrl', value: 'http://127.0.0.1:8080', type: 'string'});


// Keep only the checkout flow; reuse its login for authorization tests.
const login = structuredClone(bills.item[0]);
login.event.find(e => e.listen === 'test').script.exec.push(
  'if (pm.response.code === 200 && pm.response.json().accessToken) {',
  '  const data = pm.response.json();',
  '  pm.collectionVariables.set("securitySalesToken", data.accessToken);',
  '  pm.collectionVariables.set("securityEmployeeId", data.employee.employeeId);',
  '  pm.collectionVariables.unset("securityBaseline");',
  '  pm.collectionVariables.unset("securityTamperedBillId");',
  '}'
);
const billCases = bills.item.slice(1).filter(i =>
  !/^TC_BILL_(12|13|14) /.test(i.name) && i.name !== 'TC_BILL_09 - Số lượng âm');
const securityCases = security.item.filter(i => !i.name.startsWith('00 -') && !/^SEC(15|16) /.test(i.name));
const extraCases = require('./pos-sales-extra.cjs')();
const collection = {
  info: {
    name: 'BookStore POS | Kiểm thử bán hàng',
    schema: 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json',
    description: 'Chỉ kiểm thử bán hàng: tạo đơn, thanh toán, giỏ hàng, tồn kho, khách hàng và quyền tạo đơn. Bỏ tra cứu hóa đơn độc lập TC_BILL_13/14; gộp số lượng âm vào SEC08; dùng chung phiên bán hàng. Đăng nhập/đăng xuất là bước chuẩn bị kiểm tra quyền tạo đơn. Đọc hóa đơn sau bán để xác nhận dữ liệu lưu. Dùng environment BookStore_API_Local. bookId đang bán, giá dương, tồn sách và lô ít nhất 7 cuốn; customerId có chiết khấu; missingCustomerId chưa tồn tại. inactiveBookId có thể để trống để tự tìm sách ngừng bán. Chạy trên DB test, không có giao dịch song song hoặc đổi giá. Chạy thành công tạo 5 hóa đơn và giảm tồn kho.'
  },
  variable: [...variables.values()],
  item: [login, ...billCases, ...securityCases.slice(0, -2), ...extraCases, ...securityCases.slice(-2)]
};
collection.info.description += ' Bổ sung 8 ca đầu vào/thanh toán/tồn kho BH01, BH03, BH05, BH06, BH07A/B, BH08, BH09. Tự chọn sách thử từ API: cần ít nhất 2 sách đang bán có giá dương và tồn >=3, cùng 1 sách đang bán tồn 0. Thiếu dữ liệu sẽ báo assertion chuẩn bị thất bại và bỏ qua request phụ thuộc. Ca mới tạo thêm 2 hóa đơn khi đạt (tổng 7 hóa đơn toàn bộ collection). Bỏ các ca đổi điểm vì nghiệp vụ chưa hỗ trợ. Chưa đưa các ca mua hết kho, thay đổi dữ liệu lô, đồng thời và quy tắc chưa thống nhất vào lượt chạy chung.';
collection.info.description = collection.info.description.replace('Chạy thành công tạo 5 hóa đơn và giảm tồn kho.', 'Các ca thành công tạo hóa đơn và giảm tồn kho.');
const caseNames = {
  TC_BILL_01: 'Tạo đơn cho khách lẻ trả tiền mặt',
  TC_BILL_02: 'Tạo đơn cho khách thành viên được giảm giá',
  TC_BILL_03: 'Tạo đơn thanh toán chuyển khoản',
  TC_BILL_04: 'Xem chi tiết đơn vừa bán',
  TC_BILL_05: 'Từ chối tạo đơn không có sách',
  TC_BILL_06: 'Từ chối tạo đơn có hai dòng trùng mã sách',
  TC_BILL_07: 'Từ chối bán số lượng vượt tồn kho',
  TC_BILL_08: 'Từ chối bán sách đã ngừng bán',
  TC_BILL_09: 'Từ chối bán sách có số lượng bằng 0',
  TC_BILL_10: 'Từ chối phương thức thanh toán không hợp lệ',
  TC_BILL_11: 'Từ chối tạo đơn cho mã khách hàng không tồn tại',
  TC_BILL_12: 'Từ chối dùng điểm thanh toán vì chưa hỗ trợ đổi điểm',
  SEC01: 'Chưa đăng nhập gửi yêu cầu tạo đơn - Bị từ chối',
  SEC02: 'Dùng token giả gửi yêu cầu tạo đơn - Bị từ chối',
  SEC03: 'Không điền token gửi yêu cầu tạo đơn - Bị từ chối',
  SEC04: 'Dùng sai kiểu xác thực gửi yêu cầu tạo đơn - Bị từ chối',
  SEC05: 'Đã đăng nhập nhưng không có quyền bán hàng - Không được tạo đơn',
  SEC06: 'Không có quyền bán hàng, tự sửa quyền trong request - Vẫn bị từ chối',
  SEC07: 'Đã đăng xuất nhưng dùng lại token để tạo đơn - Bị từ chối',
  SEC08: 'Gửi số lượng âm để tăng tồn kho - Bị từ chối',
  SEC09: 'Gửi cùng sách với số lượng dương và âm - Bị từ chối',
  SEC10: 'Gửi số lượng lớn vượt giới hạn dữ liệu - Bị từ chối',
  SEC11: 'Chèn câu lệnh SQL vào mã sách - Bị từ chối',
  SEC12: 'Gửi dòng sách rỗng trong giỏ hàng - Bị từ chối',
  SEC13: 'Gửi mã khách hàng là số âm - Bị từ chối',
  SEC14: 'Tạo đơn không gửi phương thức thanh toán - Bị từ chối',
  SEC15: 'Gửi số điểm thanh toán âm - Bị từ chối',
  SEC16: 'Khách lẻ yêu cầu dùng điểm thanh toán - Bị từ chối',
  SEC17: 'Tự sửa giá, thuế, điểm và người bán - Server vẫn tính đơn đúng',
  SEC18: 'Đọc lại đơn để xác nhận đúng người bán và số tiền'
};
const setupNames = {
  '00': 'Chuẩn bị 01 - Đăng nhập tài khoản có quyền bán hàng',
  '01': 'Chuẩn bị 02 - Đăng nhập nhân viên kho không có quyền bán hàng',
  '02': 'Chuẩn bị 03 - Đăng nhập phiên riêng để thử tạo đơn sau đăng xuất',
  '03': 'Chuẩn bị 04 - Đăng xuất phiên riêng trước khi thử tạo đơn',
  '04': 'Chuẩn bị 05 - Tạo đơn giá đúng để so sánh với đơn bị sửa dữ liệu'
};
for (const request of collection.item) {
  const code = request.name.split(' - ')[0];
  const status = JSON.stringify(request.event).match(/status\((\d+)\)/)?.[1];
  if (caseNames[code]) request.name = `${code} - ${caseNames[code]} [${status}]`;
  else if (setupNames[code]) request.name = `${setupNames[code]} [${status}]`;
  else if (request.name === 'Dọn phiên bán hàng') request.name = 'Kết thúc - Đăng xuất tài khoản bán hàng [204]';
  else if (request.name === 'Dọn phiên kho') request.name = 'Kết thúc - Đăng xuất tài khoản nhân viên kho [204]';
  for (const event of request.event || []) new Function(event.script.exec.join('\n'));
}
fs.writeFileSync(path.join(root, 'BookStore_POS_All.postman_collection.json'),
  JSON.stringify(collection, null, 2) + '\n');
console.log('Sales collection: ' + collection.item.length + ' requests; scripts validated.');
