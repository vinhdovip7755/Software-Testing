package com.bookstore.bus;

import com.bookstore.dao.BookDAO;
import com.bookstore.dao.BookLotDAO;
import com.bookstore.dao.ImportDAO;
import com.bookstore.dao.ImportDetailDAO;
import com.bookstore.dto.BookLotDTO;
import com.bookstore.dto.ImportTicketDTO;
import com.bookstore.dto.ImportDetailDTO;
import com.bookstore.dto.InventoryLogDTO;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.util.ImportApprovalPolicy;
import com.bookstore.util.SharedData;

import java.util.List;

public class ImportBUS {

    private ImportDAO importDAO = new ImportDAO();
    private ImportDetailDAO detailDAO = new ImportDetailDAO();
    private BookDAO bookDAO = new BookDAO();
    private InventoryLogBUS logBUS = new InventoryLogBUS();
    private BookLotDAO bookLotDAO = new BookLotDAO();

    public List<ImportTicketDTO> getAllImports() {
        return importDAO.getAll();
    }

    public List<ImportDetailDTO> getDetailsByImportId(int importId) {
        return detailDAO.getDetailsByImportId(importId);
    }

    public int getCreatorEmployeeId(int importId) {
        return importDAO.getCreatorEmployeeId(importId);
    }

    public boolean approveImport(int importId, int approverId) {
        EmployeeDTO currentUser = SharedData.currentUser;
        if (currentUser == null || currentUser.getEmployeeId() != approverId) {
            return false;
        }
        return approveImport(importId, currentUser);
    }

    public boolean approveImport(int importId, EmployeeDTO approver) {
        if (!isAuthorizedChecker(importId, approver)) {
            return false;
        }
        java.util.List<ImportDetailDTO> details = detailDAO.getDetailsByImportId(importId);
        return importDAO.approveImportTransaction(importId, approver.getEmployeeId(), details);
    }

    public boolean cancelImport(int importId, int approverId) {
        EmployeeDTO currentUser = SharedData.currentUser;
        if (currentUser == null || currentUser.getEmployeeId() != approverId) {
            return false;
        }
        return cancelImport(importId, currentUser);
    }

    public boolean cancelImport(int importId, EmployeeDTO approver) {
        if (!isAuthorizedChecker(importId, approver)) {
            return false;
        }
        return importDAO.updateStatus(importId, 0, approver.getEmployeeId());
    }

    private boolean isAuthorizedChecker(int importId, EmployeeDTO approver) {
        return approver != null
                && ImportApprovalPolicy.canAct(
                        approver.getRoleId(),
                        approver.getEmployeeId(),
                        importDAO.getCreatorEmployeeId(importId));
    }

    public boolean importBooks(ImportTicketDTO importDTO, ImportDetailDTO[] details) {
        return importDAO.importBooksTransaction(importDTO, details);
    }
}
