package com.bookstore.util;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExcelUtilTest {
    @Test
    void identifiesWindowsFileLockErrors() {
        IOException lockedFile = new IOException(
                "The process cannot access the file because it is being used by another process");
        IOException unrelatedError = new IOException("Disk is full");

        assertTrue(ExcelUtil.isFileInUse(lockedFile));
        assertFalse(ExcelUtil.isFileInUse(unrelatedError));
        assertTrue(ExcelUtil.isFileInUse(new IOException("Export failed", lockedFile)));
    }

    @Test
    void parsesVietnameseThousandsAndDecimalNumbersOnImport(@TempDir Path tempDir) throws IOException {
        File excelFile = tempDir.resolve("books.xlsx").toFile();
        String[] headers = {
                "Tên sách", "Tác giả", "Thể loại", "Nhà cung cấp", "Năm xuất bản",
                "Giá bán", "Số lượng", "Trạng thái", "Dịch giả", "Tags", "Mô tả"
        };
        try (Workbook wb = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(excelFile)) {
            Sheet sheet = wb.createSheet("Products");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

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
}
