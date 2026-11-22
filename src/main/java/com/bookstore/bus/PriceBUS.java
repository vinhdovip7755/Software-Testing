package com.bookstore.bus;

import com.bookstore.dao.PriceDAO;
import com.bookstore.dto.PriceDTO;

import java.util.List;
import java.util.stream.Collectors;

public class PriceBUS {
    private PriceDAO priceDAO = new PriceDAO();

    public List<PriceDTO> getPriceDTOs() {
        return priceDAO.getAllActivePrices();
    }

    public boolean createNewPrice(int bookId, double basePrice, double profitRate, double predictedPrice) {
        if (bookId <= 0
                || !Double.isFinite(basePrice) || basePrice <= 0 || basePrice > PriceDAO.MAX_PRICE
                || !Double.isFinite(profitRate) || profitRate < 0 || profitRate > PriceDAO.MAX_PROFIT_RATE
                || !Double.isFinite(predictedPrice) || predictedPrice <= 0 || predictedPrice > PriceDAO.MAX_PRICE) {
            return false;
        }
        return priceDAO.createNewPrice(bookId, basePrice, profitRate, predictedPrice);
    }

    public boolean updateBulkPrice(List<PriceDTO> listHienThi, double newProfitRate) {
        if (listHienThi == null || listHienThi.isEmpty()
                || !Double.isFinite(newProfitRate) || newProfitRate < 0 || newProfitRate > PriceDAO.MAX_PROFIT_RATE) {
            return false;
        }
        return priceDAO.updateBulkPrice(listHienThi, newProfitRate);
    }

    public List<PriceDTO> timKiemSach(List<PriceDTO> currentList, String keyword, String type) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return currentList;
        }

        String lowerKeyword = keyword.trim().toLowerCase();

        return currentList.stream().filter(p -> {
            switch (type) {
                case "Tên":
                    return p.getBookName() != null && p.getBookName().toLowerCase().contains(lowerKeyword);
                case "Tác giả":
                    return p.getAuthorName() != null && p.getAuthorName().toLowerCase().contains(lowerKeyword);
                case "Thể loại":
                    return p.getCategoryName() != null && p.getCategoryName().toLowerCase().contains(lowerKeyword);
                case "Giá":
                    try {
                        if (lowerKeyword.contains("-")) {
                            String[] parts = lowerKeyword.split("-", -1);

                            double min = (parts[0].trim().isEmpty()) ? 0 : Double.parseDouble(parts[0].trim());

                            double max = (parts.length < 2 || parts[1].trim().isEmpty())
                                    ? Double.MAX_VALUE : Double.parseDouble(parts[1].trim());

                            return p.getSellingPrice() >= min && p.getSellingPrice() <= max;
                        } else {
                            double price = Double.parseDouble(lowerKeyword);
                            return p.getSellingPrice() <= price;
                        }
                    } catch (NumberFormatException e) {
                        return false;
                    }
                default:
                    return false;
            }
        }).collect(Collectors.toList());
    }
}
