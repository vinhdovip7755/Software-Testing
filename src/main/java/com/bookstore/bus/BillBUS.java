package com.bookstore.bus;

import com.bookstore.dao.BillDAO;
import com.bookstore.dto.BillDTO;
import com.bookstore.dto.BillDetailDTO;

import java.util.List;

public class BillBUS {
    private final BillDAO billDAO = new BillDAO();

    public int createBill(BillDTO bill, List<BillDetailDTO> details) {
        if (bill == null || details == null || details.isEmpty()) {
            return -1;
        }
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