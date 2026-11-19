// Additional checkout cases. Fixtures are read from the API, never written directly.
module.exports = function extraSalesCases() {
  const event = (listen, code) => ({listen, script:{type:'text/javascript',exec:code.trim().split('\n')}});
  function make(id, title, status, body, mode = 'normal', checks = '') {
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
 const empty=books.find(b=>b.status===1 && b.quantity===0);
 const mode=${JSON.stringify(mode)};
 const ready=active.length >= (mode==='multi' || mode==='rollback' ? 2 : 1) && (mode!=='empty' || !!empty);
 if (!ready) {
   pm.test('Chuẩn bị dữ liệu ${id}', () => { throw new Error('Cần sách đang bán giá dương tồn >=3; ca nhiều sách cần 2 mã; ca hết hàng cần sách tồn 0.'); });
   pm.execution.skipRequest(); return;
 }
 pm.variables.set('bhBookA',active[0].bookId);
 pm.variables.set('bhBookB',active[1] ? active[1].bookId : active[0].bookId);
 pm.variables.set('bhTooMany',active[1] ? active[1].quantity+1 : 1);
 pm.variables.set('bhEmptyBook',empty ? empty.bookId : 0);
 pm.variables.set('bhMissingBook',Math.max(...books.map(b=>b.bookId))+1);
 pm.variables.set('bhSnapshot',JSON.stringify(books.map(b=>({bookId:b.bookId,quantity:b.quantity}))));
 pm.sendRequest({url:url+'/api/bills',header:headers}, (e,r) => {
   if (e || !r || r.code!==200) {
     pm.test('Chuẩn bị: đọc được danh sách hóa đơn', () => { throw new Error('GET /api/bills thất bại'); });
     pm.execution.skipRequest(); return;
   }
   pm.variables.set('bhBillsBefore',JSON.stringify(r.json().map(b=>b.billId).sort((a,b)=>a-b)));
 });
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
 ${checks}
}`}`;
    return {name:`${id} - ${title} [${status}]`,request:{method:'POST',auth:{type:'noauth'},
      header:[{key:'Content-Type',value:'application/json'},{key:'Authorization',value:'Bearer {{securitySalesToken}}'}],
      url:'{{baseUrl}}/api/bills',body:{mode:'raw',raw:body,options:{raw:{language:'json'}}}},
      event:[event('prerequest',pre),event('test',test)]};
  }
  const body = (details, payment=1) => `{"customerId":0,"paymentMethodId":${payment},"details":${details}}`;
  const one='[{"bookId":{{bhBookA}},"quantity":1}]';
  return [
    make('BH01','Thanh toán bằng thẻ và lưu đúng phương thức',201,body(one,3)),
    make('BH03','Mua hai sách khác nhau và trừ đúng tồn từng sách',201,
      body('[{"bookId":{{bhBookA}},"quantity":1},{"bookId":{{bhBookB}},"quantity":2}]'),'multi'),
    make('BH05','Sách đang bán nhưng hết hàng - Từ chối tạo đơn',409,
      body('[{"bookId":{{bhEmptyBook}},"quantity":1}]'),'empty'),
    make('BH06','Số lượng 1.5 cuốn - Không được tự làm tròn thành 1',400,
      body('[{"bookId":{{bhBookA}},"quantity":1.5}]')),
    make('BH07A','Thiếu số lượng sách - Từ chối tạo đơn',400,body('[{"bookId":{{bhBookA}}}]')),
    make('BH07B','Số lượng null - Từ chối tạo đơn',400,body('[{"bookId":{{bhBookA}},"quantity":null}]')),
    make('BH08','Mã sách chưa tồn tại - Từ chối tạo đơn',400,body('[{"bookId":{{bhMissingBook}},"quantity":1}]')),
    make('BH09','Sách A đủ, sách B thiếu - Hủy cả đơn và giữ nguyên tồn A',409,
      body('[{"bookId":{{bhBookA}},"quantity":1},{"bookId":{{bhBookB}},"quantity":{{bhTooMany}}}]'),'rollback')
  ];
};
