package com.bookstore.util;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExcelUtilTest {
    private static final String[] HEADERS = {
            "Tên sách", "Tác giả", "Thể loại", "Nhà cung cấp", "Năm xuất bản",
            "Giá bán", "Số lượng", "Trạng thái", "Dịch giả", "Tags", "Mô tả"
    };

    @Test
    void identifiesWindowsFileLockErrors() {
        IOException lockedFile = new IOException(
                "The process cannot access the file because it is being used by another process");
        IOException unrelatedError = new IOException("Disk is full");

        assertTrue(ExcelUtil.isFileInUse(lockedFile));
        assertFalse(ExcelUtil.isFileInUse(unrelatedError));
        assertTrue(ExcelUtil.isFileInUse(new IOException("Export failed", lockedFile)));
        assertTrue(ExcelUtil.isFileInUse(new AccessDeniedException("products.xlsx")));
        assertTrue(ExcelUtil.isFileInUse(new IOException("Sharing violation on file")));
    }

    @Test
    void parsesVietnameseThousandsAndDecimalNumbersOnImport(@TempDir Path tempDir) throws IOException {
        File excelFile = tempDir.resolve("books.xlsx").toFile();
        try (Workbook wb = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(excelFile)) {
            Sheet sheet = wb.createSheet("Products");
            writeHeaders(sheet);

            Row vnRow = sheet.createRow(1);
            vnRow.createCell(0).setCellValue("Sách A");
            vnRow.createCell(1).setCellValue("Tác giả A");
            vnRow.createCell(2).setCellValue("Văn học");
            vnRow.createCell(3).setCellValue("NXB Trẻ");
            vnRow.createCell(4).setCellValue("2025");
            vnRow.createCell(5).setCellValue("150.000");
            vnRow.createCell(6).setCellValue("1.200");
            vnRow.createCell(7).setCellValue("Đang bán");

            Row decimalRow = sheet.createRow(2);
            decimalRow.createCell(0).setCellValue("Sách B");
            decimalRow.createCell(1).setCellValue("Tác giả B");
            decimalRow.createCell(2).setCellValue("Khoa học");
            decimalRow.createCell(3).setCellValue("NXB Kim Đồng");
            decimalRow.createCell(4).setCellValue(2024);
            decimalRow.createCell(5).setCellValue(150.5);
            decimalRow.createCell(6).setCellValue(25);
            decimalRow.createCell(7).setCellValue("Ngừng bán");

            wb.write(fos);
        }

        List<ExcelUtil.ExcelBookRow> rows = ExcelUtil.importBooks(excelFile);
        assertEquals(2, rows.size());
        assertEquals(150000.0, rows.get(0).getSellingPrice());
        assertEquals(1200, rows.get(0).getQuantity());
        assertEquals(1, rows.get(0).getStatus());

        assertEquals(150.5, rows.get(1).getSellingPrice());
        assertEquals(25, rows.get(1).getQuantity());
        assertEquals(0, rows.get(1).getStatus());
    }

    @Test
    void handlesBooleanAndErrorFormulaCellsSafely(@TempDir Path tempDir) throws IOException {
        File excelFile = tempDir.resolve("formulas.xlsx").toFile();
        try (Workbook wb = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(excelFile)) {
            Sheet sheet = wb.createSheet("Products");
            writeHeaders(sheet);

            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("Sách Công Thức");
            row.createCell(1).setCellValue("Tác giả");
            row.createCell(2).setCellValue("Văn học");
            row.createCell(3).setCellValue("NXB Trẻ");
            row.createCell(4).setCellValue(2025);
            row.createCell(5).setCellValue(95000);
            row.createCell(6).setCellValue(10);
            row.createCell(7).setCellValue("Đang bán");
            row.createCell(8).setCellFormula("1=1");
            row.createCell(9).setCellFormula("1/0");

            wb.getCreationHelper().createFormulaEvaluator().evaluateAll();
            wb.write(fos);
        }

        List<ExcelUtil.ExcelBookRow> rows = ExcelUtil.importBooks(excelFile);
        assertEquals(1, rows.size());
        assertEquals("true", rows.get(0).getTranslator());
        assertEquals("", rows.get(0).getTagDetail());
    }

    @Test
    void rejectsQuantityOverflowDecimalYearAndUnknownOrNegatedStatus(@TempDir Path tempDir) throws IOException {
        File overflowQty = createSingleRowFile(tempDir.resolve("overflow.xlsx").toFile(), "2024", "95000", "9999999999", "Đang bán");
        assertThrows(IllegalArgumentException.class, () -> ExcelUtil.importBooks(overflowQty));

        File decimalYear = createSingleRowFile(tempDir.resolve("dec-year.xlsx").toFile(), "2023.4", "95000", "10", "Đang bán");
        assertThrows(IllegalArgumentException.class, () -> ExcelUtil.importBooks(decimalYear));

        File negatedStatus = createSingleRowFile(tempDir.resolve("neg-status.xlsx").toFile(), "2024", "95000", "10", "không đang bán");
        assertThrows(IllegalArgumentException.class, () -> ExcelUtil.importBooks(negatedStatus));

        File unknownStatus = createSingleRowFile(tempDir.resolve("unknown-status.xlsx").toFile(), "2024", "95000", "10", "Hết hàng");
        assertThrows(IllegalArgumentException.class, () -> ExcelUtil.importBooks(unknownStatus));
    }

    @Test
    void neutralizesFormulaInjectionOnExport(@TempDir Path tempDir) throws IOException {
        File excelFile = tempDir.resolve("exported.xlsx").toFile();
        ExcelUtil.ExcelBookRow row = new ExcelUtil.ExcelBookRow();
        row.setBookName("=CMD|' /C calc'!A0");
        row.setAuthorNames("+Author");
        row.setCategoryName("-Category");
        row.setSupplierName("@Supplier");
        row.setPublicationYear(2025);
        row.setSellingPrice(95000);
        row.setQuantity(5);
        row.setStatus(1);
        row.setDescription("Bình thường");

        ExcelUtil.exportBooks(excelFile, List.of(row));

        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook wb = WorkbookFactory.create(fis)) {
            Row exported = wb.getSheet("Products").getRow(1);
            assertEquals("'=CMD|' /C calc'!A0", exported.getCell(0).getStringCellValue());
            assertEquals("'+Author", exported.getCell(1).getStringCellValue());
            assertEquals("'-Category", exported.getCell(2).getStringCellValue());
            assertEquals("'@Supplier", exported.getCell(3).getStringCellValue());
            assertEquals("Bình thường", exported.getCell(10).getStringCellValue());
        }
    }

    private static void writeHeaders(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            headerRow.createCell(i).setCellValue(HEADERS[i]);
        }
    }

    private static File createSingleRowFile(File file, String year, String price, String qty, String status) throws IOException {
        try (Workbook wb = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(file)) {
            Sheet sheet = wb.createSheet("Products");
            writeHeaders(sheet);
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("Sách Test");
            row.createCell(1).setCellValue("Tác giả");
            row.createCell(2).setCellValue("Văn học");
            row.createCell(3).setCellValue("NXB Trẻ");
            row.createCell(4).setCellValue(year);
            row.createCell(5).setCellValue(price);
            row.createCell(6).setCellValue(qty);
            row.createCell(7).setCellValue(status);
            wb.write(fos);
        }
        return file;
    }
}
