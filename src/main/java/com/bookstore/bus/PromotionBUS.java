package com.bookstore.bus;

import com.bookstore.dao.BookDAO;
import com.bookstore.dao.PromotionDAO;
import com.bookstore.dto.BookDTO;
import com.bookstore.dto.PromotionDTO;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PromotionBUS {
    public static final int MAX_NAME_LENGTH = 255;

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

    public List<PromotionDTO> selectAllPromotionsWithEffectiveStatus() {
        List<PromotionDTO> all = promotionDAO.selectAllPromotions();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        for (PromotionDTO p : all) {
            if (p.getStatus() == 1 && p.getEndDate() != null && now.after(p.getEndDate())) {
                p.setStatus(0);
            }
        }
        return all;
    }

    public List<PromotionDTO> search(String keyword, String type) {
        List<PromotionDTO> all = selectAllPromotionsWithEffectiveStatus();
        if (keyword == null || keyword.isEmpty()) {
            return all;
        }

        List<PromotionDTO> result = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();

        for (PromotionDTO p : all) {
            boolean match;
            switch (type) {
                case "Tên":
                    match = p.getPromotionName() != null
                            && p.getPromotionName().toLowerCase().contains(lowerKeyword);
                    break;
                case "Phần trăm":
                    try {
                        double percent = Double.parseDouble(lowerKeyword);
                        match = Double.isFinite(percent) && (p.getPercent() == percent);
                    } catch (Exception e) {
                        match = false;
                    }
                    break;
                case "Thời gian":
                    try {
                        String[] dates = lowerKeyword.split("\\|", -1);
                        long startLimit = (dates.length == 0 || dates[0].isBlank())
                                ? Long.MIN_VALUE
                                : Timestamp.valueOf(LocalDate.parse(dates[0].trim()).atStartOfDay()).getTime();
                        long endLimit = (dates.length < 2 || dates[1].isBlank())
                                ? Long.MAX_VALUE
                                : Timestamp.valueOf(LocalDate.parse(dates[1].trim()).atTime(23, 59, 59, 999_000_000)).getTime();
                        match = p.getStartDate() != null && p.getEndDate() != null
                                && p.getStartDate().getTime() >= startLimit
                                && p.getEndDate().getTime() <= endLimit;
                    } catch (Exception e) {
                        match = false;
                    }
                    break;
                default:
                    match = false;
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
            if (p.getEndDate() == null) {
                continue;
            }
            // Only expire active promotions past their end date; never re-enable manually disabled promotions (status = 0).
            if (p.getStatus() == 1 && now.after(p.getEndDate())) {
                p.setStatus(0);
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
        if (name == null || name.trim().isEmpty() || name.trim().length() > MAX_NAME_LENGTH) {
            return false;
        }
        if (!Double.isFinite(percent) || percent <= 0 || percent > 100) {
            return false;
        }
        if (start == null || end == null || start.after(end)) {
            return false;
        }
        if (status != 0 && status != 1) {
            return false;
        }
        if (bookIds == null || bookIds.isEmpty()) {
            return false;
        }
        Set<Integer> sanitizedBookIds = new LinkedHashSet<>();
        for (Integer bookId : bookIds) {
            if (bookId == null || bookId <= 0) {
                return false;
            }
            sanitizedBookIds.add(bookId);
        }

        String trimmedName = name.trim();
        List<Integer> uniqueBookIds = new ArrayList<>(sanitizedBookIds);
        if (!isEdit) {
            PromotionDTO newPromo = new PromotionDTO(0, trimmedName, percent, start, end, status);
            return promotionDAO.savePromotionTransaction(false, newPromo, uniqueBookIds);
        } else {
            if (dto == null || dto.getPromotionId() <= 0) {
                return false;
            }
            dto.setPromotionName(trimmedName);
            dto.setPercent(percent);
            dto.setStartDate(start);
            dto.setEndDate(end);
            dto.setStatus(status);
            return promotionDAO.savePromotionTransaction(true, dto, uniqueBookIds);
        }
    }

    public List<Integer> getSelectedBookIds(int promoId) {
        return promotionDAO.getSelectedBookIds(promoId);
    }

    public List<BookDTO> suggestBooksByName(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return bookDAO.selectAllBooks();
        }
        return bookDAO.searchByName(keyword);
    }

    public List<BookDTO> suggestBooksForManagement(String keyword, int categoryId) {
        if (keyword != null && !keyword.isBlank()) {
            return bookDAO.searchByName(keyword);
        }
        if (categoryId > 0) {
            return bookDAO.getByCategoryId(categoryId);
        }
        return bookDAO.selectAllBooks();
    }
}
