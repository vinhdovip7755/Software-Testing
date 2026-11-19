const fs = require('fs');
const path = require('path');
const item = [];
const event = (listen, code) => ({ listen, script: { type: 'text/javascript', exec: code.trim().split('\n') } });
const base = '{"customerId":0,"paymentMethodId":1,"details":[{"bookId":{{bookId}},"quantity":1}]}';
function request(name, method, endpoint, body, auth, tests, pre = '') {
  const header = [{key:'Content-Type',value:'application/json'}];
  if (auth !== null) header.push({key:'Authorization',value:auth});
  const request = {method, auth:{type:'noauth'}, header, url:'{{baseUrl}}' + endpoint};
  if (body !== null) request.body = {mode:'raw',raw:body,options:{raw:{language:'json'}}};
  item.push({name,request,event:[...(pre ? [event('prerequest',pre)] : []),event('test',tests)]});
}
function login(label, prefix, variable, extra = '') {
  request(label,'POST','/api/auth/login',JSON.stringify({username:`{{${prefix}Username}}`,password:`{{${prefix}Password}}`}),null,`
pm.test('Đăng nhập thành công', () => pm.response.to.have.status(200));
if (pm.response.code !== 200) { pm.execution.setNextRequest(null); }
else {
 const data = pm.response.json();
 pm.test('Có token hợp lệ', () => pm.expect(data.accessToken).to.be.a('string').and.not.empty);
 if (!data.accessToken) pm.execution.setNextRequest(null);
 else pm.collectionVariables.set('${variable}', data.accessToken);
 ${extra}
}`, `pm.collectionVariables.unset('${variable}');`);
}
const sales = 'Bearer {{securitySalesToken}}';
login('00 - Đăng nhập nhân viên bán hàng','sales','securitySalesToken',`
pm.collectionVariables.set('securityEmployeeId', data.employee.employeeId);
pm.collectionVariables.unset('securityBaseline');
pm.collectionVariables.unset('securityTamperedBillId');
`);
login('01 - Đăng nhập nhân viên kho','import','securityWarehouseToken',`
const canSell = !!(data.permissions.MANAGE_SELLING && data.permissions.MANAGE_SELLING.action);
pm.test('Tài khoản thử nghiệm không có quyền bán hàng', () => pm.expect(canSell).to.eql(false));
if (canSell) pm.execution.setNextRequest(null);
`);
const before = `
pm.variables.unset('securityStockBefore');
pm.sendRequest({url:pm.variables.replaceIn('{{baseUrl}}/api/books/{{bookId}}'),method:'GET',
 header:{Authorization:pm.variables.replaceIn('Bearer {{securitySalesToken}}')}}, (err,res) => {
 pm.test('Đọc tồn kho trước request: HTTP 200', () => {
   pm.expect(err, 'Lỗi kết nối khi đọc sách').to.not.exist;
   pm.expect(res && res.code, 'GET /api/books/{{bookId}}').to.eql(200);
 });
 if (!err && res && res.code === 200) pm.variables.set('securityStockBefore',res.json().quantity);
});`;
function reject(id, title, status, body = base, auth = sales) {
  request(`${id} - ${title}`,'POST','/api/bills',body,auth,`
pm.test('HTTP ${status}', () => pm.response.to.have.status(${status}));
pm.test('Thông báo lỗi rõ ràng', () => pm.expect(pm.response.json().message).to.be.a('string').and.not.empty);
pm.test('Không trả mã hóa đơn mới', () => pm.expect(pm.response.json()).not.to.have.property('billId'));
pm.sendRequest({url:pm.variables.replaceIn('{{baseUrl}}/api/books/{{bookId}}'),method:'GET',
 header:{Authorization:pm.variables.replaceIn('Bearer {{securitySalesToken}}')}}, (err,res) => {
 pm.test('Tồn kho không thay đổi khi từ chối', () => {
   pm.expect(err).to.not.exist;
   pm.expect(res.code).to.eql(200);
   pm.expect(pm.variables.get('securityStockBefore')).to.be.a('number');
   pm.expect(res.json().quantity).to.eql(pm.variables.get('securityStockBefore'));
 });
});`,before);
}
reject('SEC01','Chưa đăng nhập, không gửi Authorization',401,base,null);
reject('SEC02','Token giả mạo',401,base,'Bearer invalid-forged-token');
reject('SEC03','Bearer rỗng',401,base,'Bearer ');
reject('SEC04','Sai cơ chế xác thực Basic',401,base,'Basic invalid-token');
reject('SEC05','Nhân viên kho tạo đơn',403,base,'Bearer {{securityWarehouseToken}}');
reject('SEC06','Nhân viên kho giả role, quyền và employeeId',403,
 base.replace('"customerId":0','"customerId":0,"roleId":1,"employeeId":1,"permissions":{"MANAGE_SELLING":{"action":true,"view":true}}'),
 'Bearer {{securityWarehouseToken}}');
login('02 - Tạo phiên riêng để kiểm tra đăng xuất','sales','securityRevokedToken');
request('03 - Đăng xuất phiên thử nghiệm','POST','/api/auth/logout',null,'Bearer {{securityRevokedToken}}',
 "pm.test('Đăng xuất thành công', () => pm.response.to.have.status(204));");
reject('SEC07','Dùng lại token sau đăng xuất',401,base,'Bearer {{securityRevokedToken}}');
reject('SEC08','Số lượng âm để tăng tồn kho',400,base.replace('"quantity":1','"quantity":-1'));
reject('SEC09','Hai dòng cùng sách bù trừ số lượng',400,
 base.replace('"quantity":1}', '"quantity":2},{"bookId":{{bookId}},"quantity":-1}'));
reject('SEC10','Số lượng vượt kiểu int',400,base.replace('"quantity":1','"quantity":2147483648'));
reject('SEC11','SQL injection vào mã sách',400,base.replace('{{bookId}}','"1 OR 1=1"'));
reject('SEC12','Dòng giỏ hàng null',400,'{"customerId":0,"paymentMethodId":1,"details":[null]}');
reject('SEC13','Khách hàng âm',400,base.replace('"customerId":0','"customerId":-1'));
reject('SEC14','Bỏ qua phương thức thanh toán',400,base.replace('"paymentMethodId":1,',''));
reject('SEC15','Giả điểm sử dụng âm',400,base.replace('"customerId":0','"customerId":0,"pointsUsed":-100'));
reject('SEC16','Khách lẻ tự tiêu điểm',400,base.replace('"customerId":0','"customerId":0,"pointsUsed":999999'));
request('04 - Tạo hóa đơn đối chứng (giảm tồn 1)','POST','/api/bills',base,sales,`
pm.test('HTTP 201', () => pm.response.to.have.status(201));
if (pm.response.code !== 201) pm.execution.setNextRequest(null);
else {
 const receipt = pm.response.json();
 pm.test('Sách đối chứng có giá dương', () => pm.expect(receipt.totalBillPrice).to.be.above(0));
 if (!(receipt.totalBillPrice > 0)) pm.execution.setNextRequest(null);
 pm.collectionVariables.set('securityBaseline', JSON.stringify(receipt));
}`);
request('SEC17 - Giả giá, tổng tiền, VAT, khuyến mãi, điểm và người lập đơn (giảm tồn 1)',
 'POST','/api/bills',base.replace('"customerId":0',
 '"customerId":0,"employeeId":-999,"billId":-999,"totalBillPrice":1,"tax":0,"earnedPoints":999999,"memberDiscountPercent":100')
 .replace('"quantity":1','"quantity":1,"unitPrice":1,"sellingPrice":1,"lineTotal":1,"promotionPercent":100'),sales,`
pm.test('Server tính lại hóa đơn', () => pm.response.to.have.status(201));
if (pm.response.code === 201) {
 const actual = pm.response.json();
 const expected = JSON.parse(pm.collectionVariables.get('securityBaseline'));
 for (const key of ['totalBillPrice','tax','earnedPoints','details']) {
   pm.test('Không nhận giá trị giả: ' + key, () => pm.expect(actual[key]).to.eql(expected[key]));
 }
 pm.test('Sinh mã hóa đơn mới', () => {
   pm.expect(actual.billId).to.be.above(0);
   pm.expect(actual.billId).not.to.eql(expected.billId);
 });
 pm.collectionVariables.set('securityTamperedBillId',actual.billId);
} else pm.execution.setNextRequest(null);
`);
request('SEC18 - Kiểm tra người lập và giá trị hóa đơn đã lưu','GET','/api/bills/{{securityTamperedBillId}}',null,sales,`
pm.test('HTTP 200', () => pm.response.to.have.status(200));
if (pm.response.code === 200) {
 const bill = pm.response.json();
 const baseline = JSON.parse(pm.collectionVariables.get('securityBaseline'));
 pm.test('Người lập lấy từ phiên đăng nhập', () => pm.expect(bill.employeeId).to.eql(Number(pm.collectionVariables.get('securityEmployeeId'))));
 for (const key of ['totalBillPrice','tax','earnedPoints']) {
   pm.test('Dữ liệu lưu không bị giả: ' + key, () => pm.expect(bill[key]).to.eql(baseline[key]));
 }
}`);
for (const [label, token] of [['bán hàng','securitySalesToken'],['kho','securityWarehouseToken']]) {
 request('Dọn phiên ' + label,'POST','/api/auth/logout',null,`Bearer {{${token}}}`,
   `pm.test('Đăng xuất', () => pm.response.to.have.status(204)); pm.collectionVariables.unset('${token}');`);
}
const collection = {
 info:{name:'BookStore POS | Security & Abuse - 18 test cases',schema:'https://schema.getpostman.com/json/collection/v2.1.0/collection.json',
 description:'Chạy toàn bộ theo thứ tự với environment BookStore_API_Local. bookId phải là sách đang bán, giá dương, tồn ít nhất 2 và đủ tồn lô. Hai request thành công tạo 2 hóa đơn và giảm tồn 2. Chạy trên DB test, không có giao dịch song song và không đổi giá/khuyến mãi trong lúc chạy. Tài khoản kho phải thiếu quyền MANAGE_SELLING; thu ngân cần quyền bán và xem hóa đơn. Các kiểm thử âm tính phải từ chối và giữ nguyên tồn kho.'},
 variable:[{key:'baseUrl',value:'http://127.0.0.1:8080'},{key:'bookId',value:'1'}], item
};
for (const i of item) for (const e of i.event) new Function(e.script.exec.join('\n'));
const output = path.join(__dirname,'../BookStore_POS_Security.postman_collection.json');
fs.writeFileSync(output,JSON.stringify(collection,null,2)+'\n');
console.log(`Generated ${item.length} requests; all scripts parse successfully.`);
