package com.bookstore.bus;

import com.bookstore.dao.BookDAO;
import com.bookstore.dao.BookLotDAO;
import com.bookstore.dao.ImportDAO;
import com.bookstore.dao.ImportDetailDAO;
import com.bookstore.dao.PriceDAO;
import com.bookstore.dto.BookLotDTO;
import com.bookstore.dto.ImportTicketDTO;
import com.bookstore.dto.ImportDetailDTO;
import com.bookstore.dto.InventoryLogDTO;
import com.bookstore.dto.PriceDTO;

import java.util.List;

public class ImportBUS {

    private ImportDAO importDAO = new ImportDAO();
    private ImportDetailDAO detailDAO = new ImportDetailDAO();
    private BookDAO bookDAO = new BookDAO();
    private InventoryLogBUS logBUS = new InventoryLogBUS();
    private PriceDAO priceDAO = new PriceDAO();
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

                PriceDTO currentPrice = priceDAO.getActivePriceByBookId(d.getBookID());

                if (currentPrice != null) {
                    double oldBasePrice = currentPrice.getBasePrice();
                    double importPrice = d.getPrice();
                    System.out.println("Import Price: " + importPrice);

                    double totalOldValue = currentStock * oldBasePrice;
                    double totalImportValue = d.getQuantity() * importPrice;

                    double newBasePrice = (totalOldValue + totalImportValue) / newStock;

                    if (Math.round(newBasePrice) != Math.round(oldBasePrice)) {
                        double profitRate = currentPrice.getProfitRate();
                        double newSellingPrice = newBasePrice * (1 + profitRate);

                        priceDAO.deactivatePrice(d.getBookID());

                        PriceDTO newPriceObj = new PriceDTO();
                        newPriceObj.setBookId(d.getBookID());
                        newPriceObj.setBasePrice(newBasePrice);
                        newPriceObj.setProfitRate(profitRate);
                        newPriceObj.setSellingPrice(Math.round(newSellingPrice));
                        newPriceObj.setEffectiveDate(new java.sql.Timestamp(System.currentTimeMillis()));
                        newPriceObj.setIsActive(1);

                        priceDAO.addPrice(newPriceObj);
                    }
                } else {
                    double initialImportPrice = d.getPrice();
                    double defaultProfitRate = 0.4;
                    double initialSellingPrice = initialImportPrice * (1 + defaultProfitRate);

                    PriceDTO newPriceObj = new PriceDTO();
                    newPriceObj.setBookId(d.getBookID());
                    newPriceObj.setBasePrice(initialImportPrice);
                    newPriceObj.setProfitRate(defaultProfitRate);
                    newPriceObj.setSellingPrice(Math.round(initialSellingPrice));
                    newPriceObj.setEffectiveDate(new java.sql.Timestamp(System.currentTimeMillis()));
                    newPriceObj.setIsActive(1);

                    priceDAO.addPrice(newPriceObj);
                }

                bookDAO.updateQuantity(d.getBookID(), newStock);

                BookLotDTO lot = new BookLotDTO();
                lot.setBookId(d.getBookID());
                lot.setImportTicketId(importId);
                lot.setImportDate(new java.sql.Timestamp(System.currentTimeMillis()));
                lot.setCoverPrice(d.getCoverPrice());
                lot.setDiscountPercent(d.getDiscountPercent());
                lot.setImportPrice(d.getPrice());
                
                PriceDTO activePrice = priceDAO.getActivePriceByBookId(d.getBookID());
                if (activePrice != null) {
                    lot.setSellingPrice(activePrice.getSellingPrice());
                } else {
                    lot.setSellingPrice(0);
                }
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
                    detailDAO.add(detail);
                }
            }
            return true;
        }
        return false;
    }
}