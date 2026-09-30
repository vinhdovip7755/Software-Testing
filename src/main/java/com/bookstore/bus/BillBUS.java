package com.bookstore.bus;

import com.bookstore.dao.BillDAO;
import com.bookstore.dto.BillDTO;
import com.bookstore.dto.BillDetailDTO;

import java.util.List;

public class BillBUS {
    private BillDAO billDAO = new BillDAO();

    public int createBill(BillDTO bill, List<BillDetailDTO> details) {
        return billDAO.createBillTransaction(bill, details);
    }

    public List<BillDTO> getAllBills(){
        return billDAO.getAllBills();
    }

    public BillDTO getBillById(int billId) {
        return billDAO.getBillById(billId);
    }

    public List<BillDetailDTO> getBillDetailsByBillId(int billId) {
        return billDAO.getBillDetailsByBillId(billId);
    }
}