package com.bookstore.bus;

import com.bookstore.dao.BookDAO;
import com.bookstore.dao.PromotionDAO;
import com.bookstore.dto.BookDTO;
import com.bookstore.dto.PromotionDTO;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class PromotionBUS {
    private PromotionDAO promotionDAO = new PromotionDAO();
    private BookDAO bookDAO = new BookDAO();

    public String getPromotionNameByBookId(int bookId) {
        return promotionDAO.getPromotionNameByBookId(bookId);
    }
    
    public double getPromotionPercentByBookId(int bookId) {
        return promotionDAO.getPromotionPercentByBookId(bookId);
    }

    public List<PromotionDTO> selectAllPromotions() {
        return promotionDAO.selectAllPromotions();
    }

    public List<PromotionDTO> search(String keyword, String type) {
        List<PromotionDTO> all = promotionDAO.selectAllPromotions();
        if (keyword == null || keyword.isEmpty())
            return all;

        List<PromotionDTO> result = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();

        for (PromotionDTO p : all) {
            boolean match;
            switch (type) {
                case "Tên":
                    match = p.getPromotionName().toLowerCase().contains(lowerKeyword);
                    break;
                case "Phần trăm":
                    try {
                        double percent = Double.parseDouble(lowerKeyword);
                        match = (p.getPercent() == percent);
                    } catch (Exception e) {
                        match = false;
                    }
                    break;
                case "Thời gian":
                    try {
                        String[] dates = lowerKeyword.split("\\|");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        long startLimit = (dates[0].isEmpty()) ? 0 : sdf.parse(dates[0]).getTime();
                        long endLimit = (dates.length < 2 || dates[1].isEmpty()) ? Long.MAX_VALUE
                                : sdf.parse(dates[1]).getTime();
                        match = (p.getStartDate().getTime() >= startLimit && p.getEndDate().getTime() <= endLimit);
                    } catch (Exception e) {
                        match = false;
                    }
                    break;
                default:
                    match = true;
                    break;
            }

            if (match) {
                result.add(p);
            }
        }
        return result;
    }

    public void updateAutoStatus() {
        List<PromotionDTO> all = promotionDAO.selectAllPromotions();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        for (PromotionDTO p : all) {
            int newStatus = p.getStatus();
            if (now.after(p.getEndDate())) {
                newStatus = 0;
            } else if (now.after(p.getStartDate()) && now.before(p.getEndDate())) {
                newStatus = 1;
            }
            if (newStatus != p.getStatus()) {
                p.setStatus(newStatus);
                promotionDAO.updateStatus(p);
            }
        }
    }

    public List<BookDTO> suggestBooksByPromotionName(String promoName) {
        if (promoName == null || promoName.trim().isEmpty()) {
            return bookDAO.selectAllBooks();
        }
        String name = promoName.toLowerCase();
        com.bookstore.dao.CategoryDAO categoryDAO = new com.bookstore.dao.CategoryDAO();
        for (com.bookstore.dto.CategoryDTO cat : categoryDAO.selectAllCategories()) {
            if (cat.getCategoryName() != null && name.contains(cat.getCategoryName().toLowerCase().trim())) {
                return bookDAO.getByCategoryId(cat.getCategoryId());
            }
        }
        return bookDAO.selectAllBooks();
    }

    public boolean savePromotion(boolean isEdit, PromotionDTO dto, String name, double percent, Timestamp start,
                                 Timestamp end, int status, List<Integer> bookIds) {
        if (!isEdit) {
            PromotionDTO newPromo = new PromotionDTO(0, name, percent, start, end, status);
            return promotionDAO.savePromotionTransaction(false, newPromo, bookIds);
        } else {
            dto.setPromotionName(name);
            dto.setPercent(percent);
            dto.setStartDate(start);
            dto.setEndDate(end);
            dto.setStatus(status);
            return promotionDAO.savePromotionTransaction(true, dto, bookIds);
        }
    }

    public List<Integer> getSelectedBookIds(int promoId) {
        return promotionDAO.getSelectedBookIds(promoId);
    }

    public List<BookDTO> suggestBooksByName(String keyword) {
        if (keyword == null || keyword.isEmpty())
            return bookDAO.selectAllBooks();
        return bookDAO.searchByName(keyword);
    }
}
