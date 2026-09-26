package com.bookstore.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtil {
    private static final String SHEET_NAME = "Products";
    private static final String[] HEADERS = {
            "Tên sách", "Tác giả", "Thể loại", "Nhà cung cấp", "Năm xuất bản", "Giá bán", "Số lượng", "Trạng thái", "Dịch giả", "Tags", "Mô tả"
    };

    private ExcelUtil() {
    }

    public static void exportBooks(File file, List<ExcelBookRow> rows) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(SHEET_NAME);
            createHeaderRow(workbook, sheet);

            int rowIndex = 1;
            for (ExcelBookRow data : rows) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(defaultString(data.getBookName()));
                row.createCell(1).setCellValue(defaultString(data.getAuthorNames()));
                row.createCell(2).setCellValue(defaultString(data.getCategoryName()));
                row.createCell(3).setCellValue(defaultString(data.getSupplierName()));
                if (data.getPublicationYear() > 0) {
                    row.createCell(4).setCellValue(data.getPublicationYear());
                } else {
                    row.createCell(4).setCellValue("");
                }
                row.createCell(5).setCellValue(data.getSellingPrice());
                row.createCell(6).setCellValue(data.getQuantity());
                row.createCell(7).setCellValue(data.getStatus() == 1 ? "Đang bán" : "Ngừng bán");
                row.createCell(8).setCellValue(defaultString(data.getTranslator()));
                row.createCell(9).setCellValue(defaultString(data.getTagDetail()));
                row.createCell(10).setCellValue(defaultString(data.getDescription()));
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                workbook.write(outputStream);
            }
        }
    }

    public static List<ExcelBookRow> importBooks(File file) throws IOException {
        List<ExcelBookRow> rows = new ArrayList<>();

        try (FileInputStream inputStream = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return rows;
            }
            validateHeaderRow(headerRow);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmpty(row)) {
                    continue;
                }

                int sourceRow = i + 1;
                ExcelBookRow data = new ExcelBookRow();
                data.setSourceRow(sourceRow);
                data.setBookName(getCellValue(row.getCell(0)));
                data.setAuthorNames(getCellValue(row.getCell(1)));
                data.setCategoryName(getCellValue(row.getCell(2)));
                data.setSupplierName(getCellValue(row.getCell(3)));
                data.setPublicationYear(parsePublicationYear(getCellValue(row.getCell(4)), sourceRow));
                data.setSellingPrice(parseSellingPrice(getCellValue(row.getCell(5)), sourceRow));
                data.setQuantity(parseQuantity(getCellValue(row.getCell(6)), sourceRow));
                data.setStatus(parseStatus(getCellValue(row.getCell(7))));
                data.setTranslator(getCellValue(row.getCell(8)));
                data.setTagDetail(getCellValue(row.getCell(9)));
                data.setDescription(getCellValue(row.getCell(10)));
                rows.add(data);
            }
        }

        return rows;
    }

    private static void createHeaderRow(Workbook workbook, Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield "";
                }
                double numberValue = cell.getNumericCellValue();
                if (numberValue == (long) numberValue) {
                    yield String.valueOf((long) numberValue);
                }
                yield String.valueOf(numberValue);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue().trim();
                } catch (IllegalStateException ex) {
                    double numberValue = cell.getNumericCellValue();
                    if (numberValue == (long) numberValue) {
                        yield String.valueOf((long) numberValue);
                    }
                    yield String.valueOf(numberValue);
                }
            }
            default -> "";
        };
    }

    private static void validateHeaderRow(Row headerRow) {
        for (int i = 0; i < HEADERS.length; i++) {
            String actual = getCellValue(headerRow.getCell(i));
            String expected = HEADERS[i];
            if (!expected.equalsIgnoreCase(actual)) {
                throw new IllegalArgumentException(
                        "Dòng tiêu đề không đúng định dạng tại cột " + (i + 1)
                                + ". Mong đợi '" + expected + "' nhưng nhận '" + actual + "'.");
            }
        }
    }

    private static int parseStatus(String statusText) {
        if (statusText == null) {
            return 1;
        }
        String normalized = statusText.trim().toLowerCase();
        if (normalized.equals("1") || normalized.contains("đang bán") || normalized.contains("dang ban")) {
            return 1;
        }
        if (normalized.equals("0") || normalized.contains("ngừng bán") || normalized.contains("ngung ban")) {
            return 0;
        }
        return 1;
    }

    private static int parsePublicationYear(String text, int rowNum) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        try {
            int year = (int) Math.round(Double.parseDouble(text.trim()));
            if (year < 1000 || year > 9999) {
                throw new IllegalArgumentException("Dòng " + rowNum + ": Năm xuất bản phải gồm 4 chữ số hợp lệ (ví dụ: 2023).");
            }
            return year;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Dòng " + rowNum + ": Năm xuất bản không hợp lệ: '" + text + "'.");
        }
    }

    private static double parseSellingPrice(String text, int rowNum) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        try {
            double price = Double.parseDouble(text.trim().replace(",", ""));
            if (price < 0) {
                throw new IllegalArgumentException("Dòng " + rowNum + ": Giá bán không được là số âm.");
            }
            return price;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Dòng " + rowNum + ": Giá bán không hợp lệ: '" + text + "'.");
        }
    }

    private static int parseQuantity(String text, int rowNum) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        try {
            int qty = (int) Math.round(Double.parseDouble(text.trim().replace(",", "")));
            if (qty < 0) {
                throw new IllegalArgumentException("Dòng " + rowNum + ": Số lượng không được là số âm.");
            }
            return qty;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Dòng " + rowNum + ": Số lượng không hợp lệ: '" + text + "'.");
        }
    }

    private static boolean isEmpty(Row row) {
        short lastCellNum = row.getLastCellNum();
        if (lastCellNum <= 0) {
            return true;
        }

        for (int i = 0; i < lastCellNum; i++) {
            String value = getCellValue(row.getCell(i));
            if (!value.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static String defaultString(String value) {
        return value == null ? "" : value;
    }

    public static class ExcelBookRow {
        private int sourceRow;
        private String bookName;
        private int publicationYear;
        private double sellingPrice;
        private int quantity;
        private String authorNames;
        private String categoryName;
        private String supplierName;
        private int status;
        private String translator;
        private String tagDetail;
        private String description;
        private String imagePath;

        public int getSourceRow() {
            return sourceRow;
        }

        public void setSourceRow(int sourceRow) {
            this.sourceRow = sourceRow;
        }

        public int getPublicationYear() {
            return publicationYear;
        }

        public void setPublicationYear(int publicationYear) {
            this.publicationYear = publicationYear;
        }

        public String getBookName() {
            return bookName;
        }

        public void setBookName(String bookName) {
            this.bookName = bookName;
        }

        public double getSellingPrice() {
            return sellingPrice;
        }

        public void setSellingPrice(double sellingPrice) {
            this.sellingPrice = sellingPrice;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public String getAuthorNames() {
            return authorNames;
        }

        public void setAuthorNames(String authorNames) {
            this.authorNames = authorNames;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getSupplierName() {
            return supplierName;
        }

        public void setSupplierName(String supplierName) {
            this.supplierName = supplierName;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getTranslator() {
            return translator;
        }

        public void setTranslator(String translator) {
            this.translator = translator;
        }

        public String getTagDetail() {
            return tagDetail;
        }

        public void setTagDetail(String tagDetail) {
            this.tagDetail = tagDetail;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getImagePath() {
            return imagePath;
        }

        public void setImagePath(String imagePath) {
            this.imagePath = imagePath;
        }
    }
}
