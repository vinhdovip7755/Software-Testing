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
        if (importDAO.updateStatus(importId, 2, approverId)) {
            java.util.List<ImportDetailDTO> details = detailDAO.getDetailsByImportId(importId);

            for (ImportDetailDTO d : details) {
                int currentStock = bookDAO.getQuantityByID(d.getBookID());
                int newStock = currentStock + d.getQuantity();

                bookDAO.updateQuantity(d.getBookID(), newStock);

                BookLotDTO lot = new BookLotDTO();
                lot.setBookId(d.getBookID());
                lot.setImportTicketId(importId);
                lot.setImportDate(new java.sql.Timestamp(System.currentTimeMillis()));
                lot.setCoverPrice(d.getCoverPrice());
                lot.setDiscountPercent(d.getDiscountPercent());
                lot.setImportPrice(d.getPrice());
                
                // GiA bAn lA giA bAa khAng  i
                lot.setSellingPrice(d.getCoverPrice());
                
                lot.setQuantityInitial(d.getQuantity());
                lot.setQuantityRemain(d.getQuantity());
                
                bookLotDAO.add(lot);

                InventoryLogDTO log = new InventoryLogDTO();
                log.setAction("Nhập hàng");
                log.setChangeQuantity(d.getQuantity());
                log.setRemainQuantity(newStock);
                log.setReferenceId(importId);
                log.setBookId(d.getBookID());

                logBUS.addLog(log);
            }
            return true;
        }
        return false;
    }

    public boolean cancelImport(int importId, int approverId) {
        return importDAO.updateStatus(importId, 0, approverId);
    }

    public boolean importBooks(ImportTicketDTO importDTO, ImportDetailDTO[] details) {
        int newImportID = importDAO.add(importDTO);
        if (newImportID != -1) {
            for (ImportDetailDTO detail : details) {
                if (detail != null) {
                    detail.setImportID(newImportID);
                    if (!detailDAO.add(detail)) {
                        importDAO.delete(newImportID);
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
}
