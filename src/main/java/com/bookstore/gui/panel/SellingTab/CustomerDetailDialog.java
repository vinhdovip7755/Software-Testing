package com.bookstore.gui.panel.SellingTab;

import com.bookstore.bus.CustomerBUS;
import com.bookstore.dto.CustomerDTO;
import com.bookstore.util.AppConstant;
import com.bookstore.util.MoneyFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;

public class CustomerDetailDialog extends JDialog {
    private CustomerDTO customer;
    private final CustomerBUS customerBUS = new CustomerBUS();

    public CustomerDetailDialog(JFrame owner, CustomerDTO customer) {
        super(owner, "Chi Tiết Khách Hàng", true);
        this.customer = customer;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setSize(450, 450);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);

        JLabel lbHeader = new JLabel("HỒ SƠ KHÁCH HÀNG", SwingConstants.CENTER);
        lbHeader.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 20));
        lbHeader.setForeground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        lbHeader.setBorder(new EmptyBorder(20, 0, 15, 0));
        add(lbHeader, BorderLayout.NORTH);

        Object[] stats = customerBUS.getCustomerStatistics(customer.getCustomerId());
        int totalBooks = stats[0] != null ? (int) stats[0] : 0;
        double totalSpent = stats[1] != null ? (double) stats[1] : 0.0;
        Timestamp lastPurchase = (Timestamp) stats[2];

        String lastPurchaseStr = "Chưa từng mua hàng";
        if (lastPurchase != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            lastPurchaseStr = sdf.format(lastPurchase);
        }

        JPanel pContent = new JPanel(new GridLayout(7, 1, 0, 15));
        pContent.setBackground(Color.WHITE);
        pContent.setBorder(new EmptyBorder(10, 30, 20, 30));

        pContent.add(createInfoRow("Tên khách hàng:", customer.getCustomerName()));
        pContent.add(createInfoRow("Số điện thoại:", customer.getCustomerPhone()));
        pContent.add(createInfoRow("Hạng thành viên:", customer.getRankName()));
        pContent.add(createInfoRow("Điểm thưởng hiện tại:", String.valueOf(customer.getPoint()) + " điểm"));
        pContent.add(createInfoRow("Tổng số sách đã mua:", String.valueOf(totalBooks) + " cuốn"));
        pContent.add(createInfoRow("Tổng chi tiêu:", MoneyFormatter.toVND(totalSpent)));
        pContent.add(createInfoRow("Lần mua gần nhất:", lastPurchaseStr));

        add(pContent, BorderLayout.CENTER);

        JPanel pBottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pBottom.setBackground(Color.WHITE);
        pBottom.setBorder(new EmptyBorder(0, 0, 20, 0));

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnClose.setPreferredSize(new Dimension(120, 40));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        pBottom.add(btnClose);
        add(pBottom, BorderLayout.SOUTH);
    }

    private JPanel createInfoRow(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel lbLabel = new JLabel(label);
        lbLabel.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        lbLabel.setForeground(Color.DARK_GRAY);

        JLabel lbValue = new JLabel(value);
        lbValue.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 15));
        lbValue.setForeground(Color.BLACK);

        panel.add(lbLabel, BorderLayout.WEST);
        panel.add(lbValue, BorderLayout.EAST);

        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#EEEEEE")));
        return panel;
    }
}
