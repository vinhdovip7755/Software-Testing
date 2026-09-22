package com.bookstore.util;

import com.bookstore.bus.SystemParameterBUS;
import com.bookstore.dao.SystemParameterDAO;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BillPDFExporter {
    private static final String FONT_PATH = "C:\\Windows\\Fonts\\Arial.ttf";

    public static boolean exportInvoice(String destPath, int billId, String customerName, List<Object[]> cartItems, double totalAmount) {
        Document document = new Document(PageSize.A5);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(destPath));
            document.open();

            BaseFont bf = BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font titleFont = new Font(bf, 20, Font.BOLD);
            Font headerFont = new Font(bf, 12, Font.BOLD);
            Font normalFont = new Font(bf, 12, Font.NORMAL);

            Paragraph title = new Paragraph("NHÀ SÁCH Ms.Hong Anh", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subTitle = new Paragraph("HÓA ĐƠN THANH TOÁN", headerFont);
            subTitle.setAlignment(Element.ALIGN_CENTER);
            subTitle.setSpacingAfter(20);
            document.add(subTitle);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String formattedBillId = String.format("HD%05d", billId);
            document.add(new Paragraph("Mã hóa đơn: " + formattedBillId, normalFont));
            document.add(new Paragraph("Ngày tạo: " + sdf.format(new Date()), normalFont));
            document.add(new Paragraph("Khách hàng: " + (customerName.isEmpty() ? "Khách vãng lai" : customerName), normalFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.2f, 3.5f, 1f, 2.5f, 1.2f, 3f});

            String[] headers = {"STT", "Tên sách", "SL", "Đơn giá", "Giảm", "Thành tiền"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setPadding(8f);
                cell.setPaddingBottom(10f);
                cell.setNoWrap(true);
                table.addCell(cell);
            }

            int stt = 1;
            for (Object[] item : cartItems) {
                String bookName = String.valueOf(item[1]);
                String quantity = String.valueOf(item[2]);
                String unitPrice = String.valueOf(item[3]);
                String discount = String.valueOf(item[4]);
                String subTotal = String.valueOf(item[5]);

                PdfPCell cellSTT = new PdfPCell(new Phrase(String.valueOf(stt++), normalFont));
                cellSTT.setHorizontalAlignment(Element.ALIGN_CENTER);
                cellSTT.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cellSTT.setPadding(8f);
                table.addCell(cellSTT);

                PdfPCell cellBookName = new PdfPCell(new Phrase(bookName, normalFont));
                cellBookName.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellBookName.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cellBookName.setPadding(8f);
                cellBookName.setPaddingBottom(10f);
                table.addCell(cellBookName);

                PdfPCell cellQty = new PdfPCell(new Phrase(quantity, normalFont));
                cellQty.setHorizontalAlignment(Element.ALIGN_CENTER);
                cellQty.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cellQty.setPadding(8f);
                cellQty.setNoWrap(true);
                table.addCell(cellQty);

                PdfPCell cellUnitPrice = new PdfPCell(new Phrase(unitPrice, normalFont));
                cellUnitPrice.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellUnitPrice.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cellUnitPrice.setPadding(8f);
                cellUnitPrice.setNoWrap(true);
                table.addCell(cellUnitPrice);

                PdfPCell cellDiscount = new PdfPCell(new Phrase(discount, normalFont));
                cellDiscount.setHorizontalAlignment(Element.ALIGN_CENTER);
                cellDiscount.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cellDiscount.setPadding(8f);
                cellDiscount.setNoWrap(true);
                table.addCell(cellDiscount);

                PdfPCell cellPrice = new PdfPCell(new Phrase(subTotal, normalFont));
                cellPrice.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellPrice.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cellPrice.setPadding(8f);
                cellPrice.setNoWrap(true);
                table.addCell(cellPrice);
            }
            document.add(table);

            double totalCart = 0;
            for (Object[] item : cartItems) {
                String subTotalStr = String.valueOf(item[5]).replaceAll("[^0-9]", "");
                if (!subTotalStr.isEmpty()) {
                    totalCart += Double.parseDouble(subTotalStr);
                }
            }

            SystemParameterDAO systemParameterDAO = new SystemParameterDAO();
            String vatStr = systemParameterDAO.getValueByKey("VAT");
            SystemParameterBUS systemParameterBUS = new SystemParameterBUS();
            double vatRate = systemParameterBUS.getVAT();

            double subTotalAfterDiscount = totalAmount / (1 + vatRate);
            double vatAmount = totalAmount - subTotalAfterDiscount;
            double memberDiscount = totalCart - subTotalAfterDiscount;

            Paragraph sumTxt = new Paragraph("Tổng tiền hàng: " + MoneyFormatter.toVND(totalCart), normalFont);
            sumTxt.setAlignment(Element.ALIGN_RIGHT);
            sumTxt.setSpacingBefore(15);
            document.add(sumTxt);

            if (memberDiscount > 1) {
                Paragraph discountTxt = new Paragraph("Giảm giá thành viên: -" + MoneyFormatter.toVND(memberDiscount), normalFont);
                discountTxt.setAlignment(Element.ALIGN_RIGHT);
                document.add(discountTxt);
            }

            Paragraph vatLine = new Paragraph("Thuế VAT(+" + vatStr + "%): " + MoneyFormatter.toVND(vatAmount), normalFont);
            vatLine.setAlignment(Element.ALIGN_RIGHT);
            document.add(vatLine);

            Paragraph total = new Paragraph("Tổng cộng: " + MoneyFormatter.toVND(totalAmount), titleFont);
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            Paragraph footer1 = new Paragraph("Cảm ơn quý khách và hẹn gặp lại!", normalFont);
            Paragraph footer2 = new Paragraph("Mọi góp ý xin gửi đến zalo: 0909901421: LÊ NGỌC QUÝ", normalFont);
            footer1.setAlignment(Element.ALIGN_CENTER);
            footer1.setSpacingBefore(30);
            footer2.setAlignment(Element.ALIGN_CENTER);
            document.add(footer1);
            document.add(footer2);

            document.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
