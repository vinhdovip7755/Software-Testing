package com.bookstore.bus;

import com.bookstore.dao.BookDAO;
import com.bookstore.dao.BookLotDAO;
import com.bookstore.dao.ImportDAO;
import com.bookstore.dao.ImportDetailDAO;
import com.bookstore.dto.BookLotDTO;
import com.bookstore.dto.ImportTicketDTO;
import com.bookstore.dto.ImportDetailDTO;
import com.bookstore.dto.InventoryLogDTO;

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

    public boolean approveImport(int importId, int approverId) {
        java.util.List<ImportDetailDTO> details = detailDAO.getDetailsByImportId(importId);
        return importDAO.approveImportTransaction(importId, approverId, details);
    }

    public boolean cancelImport(int importId, int approverId) {
        return importDAO.updateStatus(importId, 0, approverId);
    }

    public boolean importBooks(ImportTicketDTO importDTO, ImportDetailDTO[] details) {
        return importDAO.importBooksTransaction(importDTO, details);
    }
}
