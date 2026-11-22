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
        if (bill.getEmployeeId() <= 0 || bill.getPaymentMethodId() <= 0 || bill.getCustomerId() < 0) {
            return -1;
        }
        if (!Double.isFinite(bill.getTotalBillPrice()) || bill.getTotalBillPrice() < 0
                || bill.getTotalBillPrice() > BillDAO.MAX_AMOUNT) {
            return -1;
        }
        if (!Double.isFinite(bill.getTax()) || bill.getTax() < 0 || bill.getTax() > 1) {
            return -1;
        }
        if (bill.getEarnedPoints() < 0) {
            return -1;
        }
        for (BillDetailDTO detail : details) {
            if (detail == null || detail.getBookId() <= 0 || detail.getQuantity() <= 0
                    || !Double.isFinite(detail.getUnitPrice()) || detail.getUnitPrice() < 0
                    || detail.getUnitPrice() > BillDAO.MAX_AMOUNT) {
                return -1;
            }
        }
        if (bill.getCustomerId() == 0) {
            bill.setEarnedPoints(0);
        }
        return billDAO.createBillTransaction(bill, details);
    }

    public List<BillDTO> getAllBills() {
        return billDAO.getAllBills();
    }

    public BillDTO getBillById(int billId) {
        return billDAO.getBillById(billId);
    }

    public List<BillDetailDTO> getBillDetailsByBillId(int billId) {
        return billDAO.getBillDetailsByBillId(billId);
    }
}
