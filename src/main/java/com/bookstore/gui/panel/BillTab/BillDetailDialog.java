package com.bookstore.gui.panel.BillTab;

import com.bookstore.util.AppConstant;
import com.bookstore.util.MoneyFormatter;
import com.bookstore.bus.BillBUS;
import com.bookstore.dto.BillDTO;
import com.bookstore.dto.BillDetailDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class BillDetailDialog extends JDialog{
    private static final double VAT_RATE = 0.08;

    private JTable table;
    private DefaultTableModel model;

    private JLabel lbBillId;
    private JLabel lbCreatedDate;
    private JLabel lbEmployee;
    private JLabel lbCustomer;
    private JLabel lbPaymentMethod;

    private JLabel lbSubtotal;
    private JLabel lbTax;
    private JLabel lbDiscount;
    private JLabel lbGrandTotal;

    private final BillBUS billBUS = new BillBUS();
    public BillDetailDialog(int billId){
        setTitle("Chi tiết hóa đơn #" + billId);
        setSize(850, 550);
        setLayout(new BorderLayout(10, 10));
        setModal(true);
        setLocationRelativeTo(null);

        add(createInfoPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        loadData(billId);
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        Font font = new Font(AppConstant.FONT_NAME, Font.BOLD, 16);

        lbBillId = new JLabel("Mã hóa đơn: --");
        lbCreatedDate = new JLabel("Ngày lập: --");
        lbEmployee = new JLabel("Nhân viên: --");
        lbCustomer = new JLabel("Khách hàng: --");
        lbPaymentMethod = new JLabel("Thanh toán: --");

        lbBillId.setFont(font);
        lbCreatedDate.setFont(font);
        lbEmployee.setFont(font);
        lbCustomer.setFont(font);
        lbPaymentMethod.setFont(font);

        panel.add(lbBillId);
        panel.add(lbCreatedDate);
        panel.add(lbEmployee);
        panel.add(lbCustomer);
        panel.add(lbPaymentMethod);

        return panel;
    }


    private JScrollPane createTablePanel() {
        String[] columns = {"Tên sách", "Số lượng", "Đơn giá", "Thành tiền"};

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column){
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 1; i < 4; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        return new JScrollPane(table);
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        JPanel totalPanel = new JPanel(new GridLayout(4, 1, 0, 8));
        totalPanel.setBackground(Color.WHITE);

        Font normalFont = new Font(AppConstant.FONT_NAME, Font.PLAIN, 15);
        Font boldFont = new Font(AppConstant.FONT_NAME, Font.BOLD, 16);

        lbSubtotal = new JLabel("Tạm tính: " + MoneyFormatter.toVND(0), SwingConstants.RIGHT);
        lbTax = new JLabel("VAT (8%): " + MoneyFormatter.toVND(0), SwingConstants.RIGHT);
        lbDiscount = new JLabel("Giảm giá: -" + MoneyFormatter.toVND(0), SwingConstants.RIGHT);
        lbGrandTotal = new JLabel("Tổng thanh toán: " + MoneyFormatter.toVND(0), SwingConstants.RIGHT);

        lbSubtotal.setFont(normalFont);
        lbTax.setFont(normalFont);
        lbDiscount.setFont(normalFont);
        lbDiscount.setForeground(new java.awt.Color(220, 53, 69));
        lbGrandTotal.setFont(boldFont);

        totalPanel.add(lbSubtotal);
        totalPanel.add(lbTax);
        totalPanel.add(lbDiscount);
        totalPanel.add(lbGrandTotal);

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));
        btnClose.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(btnClose);

        panel.add(totalPanel, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadData(int billId) {
        model.setRowCount(0);

        BillDTO bill = billBUS.getBillById(billId);
        List<BillDetailDTO> details = billBUS.getBillDetailsByBillId(billId);


        if (bill == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn.");
            dispose();
            return;
        }

        fillBillInfo(bill);
        fillBillDetails(details, bill.getTotalBillPrice());
    }

    private void fillBillInfo(BillDTO bill) {
        String customerName = bill.getCustomerName() == null ? "Khách lẻ" : bill.getCustomerName();
        String createdDate = bill.getCreatedDate() == null
                ? "--"
                : new SimpleDateFormat("HH:mm:ss - dd/MM/yyyy").format(bill.getCreatedDate());

        lbBillId.setText("Mã hóa đơn: " + bill.getBillId());
        lbCreatedDate.setText("Ngày lập: " + createdDate);
        lbEmployee.setText("Nhân viên: " + bill.getEmployeeName());
        lbCustomer.setText("Khách hàng: " + customerName);
        lbPaymentMethod.setText("Thanh toán: " + bill.getPaymentMethodName());
    }

    private void fillBillDetails(List<BillDetailDTO> details, double grandTotalFromBill) {
        double subtotal = 0;

        for (BillDetailDTO detail : details) {
            double lineTotal = detail.getUnitPrice() * detail.getQuantity();
            subtotal += lineTotal;

            model.addRow(new Object[]{
                    detail.getBookName(),
                    detail.getQuantity(),
                    MoneyFormatter.toVND(detail.getUnitPrice()),
                    MoneyFormatter.toVND(0),
                    MoneyFormatter.toVND(lineTotal)
            });
        }

        double tax = subtotal * VAT_RATE;
        double grandTotal = grandTotalFromBill > 0 ? grandTotalFromBill : subtotal + tax;

        double discount = (subtotal + tax) - grandTotal;
        if (discount < 0) discount = 0;
        lbSubtotal.setText("Tạm tính: " + MoneyFormatter.toVND(subtotal));
        lbTax.setText("VAT (8%): " + MoneyFormatter.toVND(tax));
        lbDiscount.setText("Giảm giá: -" + MoneyFormatter.toVND(discount));
        lbGrandTotal.setText("Tổng thanh toán: " + MoneyFormatter.toVND(grandTotal));
    }
}
