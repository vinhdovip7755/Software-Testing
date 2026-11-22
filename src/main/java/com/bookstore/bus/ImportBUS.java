package com.bookstore.bus;

import com.bookstore.dao.BookDAO;
import com.bookstore.dao.ImportDAO;
import com.bookstore.dao.ImportDetailDAO;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.ImportDetailDTO;
import com.bookstore.dto.ImportTicketDTO;
import com.bookstore.dto.PermissionDTO;
import com.bookstore.util.ImportApprovalPolicy;
import com.bookstore.util.SharedData;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImportBUS {

    private ImportDAO importDAO = new ImportDAO();
    private ImportDetailDAO detailDAO = new ImportDetailDAO();
    @SuppressWarnings("unused")
    private BookDAO bookDAO = new BookDAO();

    public List<ImportTicketDTO> getAllImports() {
        return importDAO.getAll();
    }

    public ImportTicketDTO getTicketById(int importId) {
        return importDAO.getById(importId);
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
        if (!isAuthorizedChecker(importId, approver, null)) {
            return false;
        }
        return importDAO.approveImportTransaction(importId, approver.getEmployeeId());
    }

    public boolean approveImport(int importId, EmployeeDTO approver, Map<String, PermissionDTO> permissions) {
        if (!isAuthorizedChecker(importId, approver, permissions)) {
            return false;
        }
        return importDAO.approveImportTransaction(importId, approver.getEmployeeId());
    }

    public boolean cancelImport(int importId, int approverId) {
        EmployeeDTO currentUser = SharedData.currentUser;
        if (currentUser == null || currentUser.getEmployeeId() != approverId) {
            return false;
        }
        return cancelImport(importId, currentUser);
    }

    public boolean cancelImport(int importId, EmployeeDTO approver) {
        if (!isAuthorizedChecker(importId, approver, null)) {
            return false;
        }
        return importDAO.updateStatus(importId, 0, approver.getEmployeeId());
    }

    public boolean cancelImport(int importId, EmployeeDTO approver, Map<String, PermissionDTO> permissions) {
        if (!isAuthorizedChecker(importId, approver, permissions)) {
            return false;
        }
        return importDAO.updateStatus(importId, 0, approver.getEmployeeId());
    }

    private boolean isAuthorizedChecker(int importId, EmployeeDTO approver, Map<String, PermissionDTO> permissions) {
        if (importId <= 0 || approver == null) {
            return false;
        }
        ImportTicketDTO ticket = importDAO.getById(importId);
        if (ticket == null) {
            return false;
        }
        if (permissions != null) {
            return ImportApprovalPolicy.canAct(approver, permissions, ticket.getEmployeeID(), ticket.getStatus());
        }
        return ImportApprovalPolicy.canAct(approver, ticket.getEmployeeID(), ticket.getStatus());
    }

    public boolean importBooks(ImportTicketDTO importDTO, ImportDetailDTO[] details) {
        if (importDTO == null || importDTO.getEmployeeID() <= 0 || importDTO.getSupplierID() <= 0
                || details == null || details.length == 0
                || !Double.isFinite(importDTO.getTotalPrice())
                || importDTO.getTotalPrice() < 0 || importDTO.getTotalPrice() > ImportDAO.MAX_AMOUNT) {
            return false;
        }

        long totalQuantity = 0L;
        Set<Integer> seenBookIds = new HashSet<>();
        for (ImportDetailDTO detail : details) {
            if (detail == null || detail.getBookID() <= 0 || detail.getQuantity() <= 0
                    || !seenBookIds.add(detail.getBookID())
                    || !Double.isFinite(detail.getPrice()) || detail.getPrice() < 0 || detail.getPrice() > ImportDAO.MAX_AMOUNT
                    || !Double.isFinite(detail.getCoverPrice()) || detail.getCoverPrice() <= 0 || detail.getCoverPrice() > ImportDAO.MAX_AMOUNT
                    || !Double.isFinite(detail.getDiscountPercent()) || detail.getDiscountPercent() < 0 || detail.getDiscountPercent() > 100) {
                return false;
            }
            totalQuantity += detail.getQuantity();
            if (totalQuantity > Integer.MAX_VALUE) {
                return false;
            }
        }
        return importDAO.importBooksTransaction(importDTO, details);
    }
}
