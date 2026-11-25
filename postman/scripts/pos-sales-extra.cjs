// Additional checkout cases. Fixtures are read from the API, never written directly.
module.exports = function extraSalesCases() {
  const event = (listen, code) => ({listen, script:{type:'text/javascript',exec:code.trim().split('\n')}});
  function make(id, title, status, body, requiredBooks = 1) {
    const pre = `
const headers = {Authorization: 'Bearer ' + pm.collectionVariables.get('securitySalesToken')};
const url = pm.variables.replaceIn('{{baseUrl}}');
pm.variables.unset('bhSnapshot');
pm.sendRequest({url:url+'/api/books',header:headers}, (err,res) => {
 if (err || !res || res.code !== 200) {
   pm.test('Chuẩn bị: đọc được danh sách sách', () => { throw new Error('GET /api/books thất bại'); });
   pm.execution.skipRequest(); return;
 }
 const books=res.json();
 const active=books.filter(b=>b.status===1 && b.quantity>=3 && b.sellingPrice>0).sort((a,b)=>a.bookId-b.bookId);
 if (active.length < ${requiredBooks}) {
   pm.test('Chuẩn bị dữ liệu ${id}', () => { throw new Error('Cần ${requiredBooks} sách đang bán giá dương, tồn ít nhất 3.'); });
   pm.execution.skipRequest(); return;
 }
 pm.variables.set('bhBookA',active[0].bookId);
 if (${requiredBooks} === 2) {
   pm.variables.set('bhBookB',active[1].bookId);
   pm.variables.set('bhTooMany',active[1].quantity+1);
 }
 pm.variables.set('bhSnapshot',JSON.stringify(books.map(b=>({bookId:b.bookId,quantity:b.quantity}))));
${status === 201 ? '' : `
 pm.sendRequest({url:url+'/api/bills',header:headers}, (e,r) => {
   if (e || !r || r.code!==200) {
     pm.test('Chuẩn bị: đọc được danh sách hóa đơn', () => { throw new Error('GET /api/bills thất bại'); });
     pm.execution.skipRequest(); return;
   }
   pm.variables.set('bhBillsBefore',JSON.stringify(r.json().map(b=>b.billId).sort((a,b)=>a-b)));
 });`}
});`;
    const test = `
pm.test('HTTP ${status}', () => pm.response.to.have.status(${status}));
const headers={Authorization:'Bearer '+pm.collectionVariables.get('securitySalesToken')};
const url=pm.variables.replaceIn('{{baseUrl}}');
const snapshot=JSON.parse(pm.variables.get('bhSnapshot'));
const sent=JSON.parse(pm.variables.replaceIn(pm.request.body.raw));
pm.sendRequest({url:url+'/api/books',header:headers}, (err,res)=>{
 pm.test('${status === 201 ? 'Trừ đúng số lượng từng sách' : 'Từ chối đơn không làm thay đổi tồn kho'}',()=>{
   pm.expect(err).not.to.exist; pm.expect(res.code).to.eql(200);
   const actual=res.json();
   for(const before of snapshot){
     const line=sent.details.find(d=>d && d.bookId===before.bookId);
     const sold=${status === 201 ? '(line ? line.quantity : 0)' : '0'};
     pm.expect(actual.find(b=>b.bookId===before.bookId).quantity,'Sách '+before.bookId).to.eql(before.quantity-sold);
   }
 });
});
${status !== 201 ? `
pm.test('Có thông báo và không trả mã hóa đơn mới',()=>{
 pm.expect(pm.response.json().message).to.be.a('string').and.not.empty;
 pm.expect(pm.response.json()).not.to.have.property('billId');
});
pm.sendRequest({url:url+'/api/bills',header:headers}, (err,res)=>{
 pm.test('Không lưu hóa đơn khi từ chối',()=>{
   pm.expect(err).not.to.exist; pm.expect(res.code).to.eql(200);
   pm.expect(res.json().map(b=>b.billId).sort((a,b)=>a-b)).to.eql(JSON.parse(pm.variables.get('bhBillsBefore')));
 });
});` : `
if(pm.response.code===201){
 const receipt=pm.response.json();
 pm.test('Lưu đủ các dòng sách đã mua',()=>{
   pm.expect(receipt.details.map(d=>({bookId:d.bookId,quantity:d.quantity}))).to.eql(sent.details);
 });
 pm.sendRequest({url:url+'/api/bills/'+receipt.billId,header:headers},(err,res)=>{
   pm.test('Lưu đúng phương thức thanh toán và người bán',()=>{
     pm.expect(err).not.to.exist; pm.expect(res.code).to.eql(200);
     pm.expect(res.json().paymentMethodId).to.eql(sent.paymentMethodId);
     pm.expect(res.json().employeeId).to.eql(Number(pm.collectionVariables.get('securityEmployeeId')));
   });
 });

}`}`;
    return {name:`${id} - ${title} [${status}]`,request:{method:'POST',auth:{type:'noauth'},
      header:[{key:'Content-Type',value:'application/json'},{key:'Authorization',value:'Bearer {{securitySalesToken}}'}],
      url:'{{baseUrl}}/api/bills',body:{mode:'raw',raw:body,options:{raw:{language:'json'}}}},
      event:[event('prerequest',pre),event('test',test)]};
  }
  const body = details => `{"customerId":0,"paymentMethodId":1,"details":${details}}`;
  return [
    make('BH03','Mua hai sách khác nhau và trừ đúng tồn từng sách',201,
      body('[{"bookId":{{bhBookA}},"quantity":1},{"bookId":{{bhBookB}},"quantity":2}]'),2),
    make('BH06','Số lượng 1.5 cuốn - Không được tự làm tròn thành 1',400,
      body('[{"bookId":{{bhBookA}},"quantity":1.5}]')),
    make('BH09','Sách A đủ, sách B thiếu - Hủy cả đơn và giữ nguyên tồn A',409,
      body('[{"bookId":{{bhBookA}},"quantity":1},{"bookId":{{bhBookB}},"quantity":{{bhTooMany}}}]'),2)
  ];
};
