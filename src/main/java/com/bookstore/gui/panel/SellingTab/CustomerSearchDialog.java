package com.bookstore.gui.panel.SellingTab;

import com.bookstore.bus.CustomerBUS;
import com.bookstore.dto.CustomerDTO;
import com.bookstore.util.AppConstant;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

public class CustomerSearchDialog extends JDialog {
    private JTextField txtPhone;
    private JLabel lbName, lbRank, lbPoints;
    private JButton btnSelect, btnGuest, btnFind, btnCreateNew;

    private CustomerDTO selectedCustomer = null;
    private CustomerBUS customerBUS = new CustomerBUS();

    public CustomerSearchDialog(JFrame parent) {
        super(parent, "Tìm kiếm khách hàng", true);
        initUI();
    }

    private void initUI() {
        setSize(550, 350);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10,10));
        setResizable(false);
        setBackground(Color.WHITE);

        JPanel pSearch = new JPanel(new BorderLayout(5,5));
        pSearch.setBorder(BorderFactory.createEmptyBorder(10,20,0,20));
        pSearch.setOpaque(false);

        pSearch.add(new JLabel("Nhập số điện thoại:"), BorderLayout.NORTH);
        txtPhone = new JTextField();
        txtPhone.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ví dụ: 0912345678");
        txtPhone.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
        txtPhone.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));

        txtPhone.addActionListener(e -> performSearch());

        btnFind = new JButton("Tìm");
        btnFind.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        btnFind.setForeground(Color.WHITE);
        btnFind.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
        btnFind.addActionListener(e -> performSearch());

        JPanel pInput = new JPanel(new BorderLayout(5,0));
        pInput.setOpaque(false);
        pInput.add(txtPhone, BorderLayout.CENTER);
        pInput.add(btnFind, BorderLayout.EAST);
        pSearch.add(pInput, BorderLayout.CENTER);

        JPanel pResult = new JPanel(new GridLayout(3,1,5,5));
        pResult.setBorder(BorderFactory.createTitledBorder("Kết quả tìm kiếm"));
        pResult.setOpaque(false);

        lbName = createResultLabel("Tên khách hàng: -");
        lbRank = createResultLabel("Hạng thành viên: -");
        lbPoints = createResultLabel("Điểm tích lũy: -");

        pResult.add(lbName);
        pResult.add(lbRank);
        pResult.add(lbPoints);

        JPanel pCenter = new JPanel(new BorderLayout());
        pCenter.setOpaque(false);
        pCenter.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));
        pCenter.add(pResult, BorderLayout.CENTER);

        JPanel pBottom = new JPanel(new GridLayout(1, 3, 10, 10));
        pBottom.setBorder(BorderFactory.createEmptyBorder(0,20,10,20));
        pBottom.setOpaque(false);

        btnGuest = new JButton("Khách vãng lai");
        btnGuest.setPreferredSize(new Dimension(1, 40));
        btnGuest.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnGuest.addActionListener(e -> {
            this.selectedCustomer = null;
            dispose();
        });
        
        btnCreateNew = new JButton("+ Tạo mới");
        btnCreateNew.setPreferredSize(new Dimension(1, 40));
        btnCreateNew.setBackground(Color.decode("#FBC02D"));
        btnCreateNew.setForeground(Color.WHITE);
        btnCreateNew.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnCreateNew.setVisible(false);
        btnCreateNew.addActionListener(e -> {
            new CustomerEditDialog((JFrame) SwingUtilities.getWindowAncestor(this), null, null).setVisible(true);
            performSearch();
        });

        btnSelect = new JButton("Chọn khách hàng này");
        btnSelect.setPreferredSize(new Dimension(1,40));
        btnSelect.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        btnSelect.setForeground(Color.WHITE);
        btnSelect.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnSelect.setEnabled(false);
        btnSelect.addActionListener(e -> {
            if (selectedCustomer != null) {
                dispose();
            }
        });

        pBottom.add(btnGuest);
        pBottom.add(btnCreateNew);
        pBottom.add(btnSelect);

        add(pSearch, BorderLayout.NORTH);
        add(pCenter, BorderLayout.CENTER);
        add(pBottom, BorderLayout.SOUTH);
    }

    private void performSearch() {
        String phone = txtPhone.getText().trim();
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Số điện thoại!");
            txtPhone.requestFocus();
            return;
        }

        if (!phone.matches("^0\\d{9}$")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ (Phải có 10 chữ số và bắt đầu bằng số 0)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtPhone.requestFocus();
            return;
        }

        CustomerDTO customer = customerBUS.selectByPhone(phone);
        
        if (customer != null) {
            this.selectedCustomer = customer;
            lbName.setText("Tên khách hàng: " + customer.getCustomerName());
            lbName.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));

            String rankName = customer.getRankName() != null ? customer.getRankName() : "Chưa Cập nhật";
            lbRank.setText("Hạng thành viên: " + rankName);

            lbRank.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));
            lbPoints.setText("Điểm tích lũy: " + customer.getPoint());
            lbPoints.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));

            lbName.setForeground(Color.decode(AppConstant.GREEN_COLOR_CODE));
            btnSelect.setEnabled(true);
            if (btnCreateNew != null) btnCreateNew.setVisible(false);
        } else {
            this.selectedCustomer = null;
            lbName.setText("Không tìm thấy khách hàng");
            lbName.setForeground(Color.RED);
            lbRank.setText("Hạng thành viên: -");
            lbPoints.setText("Điểm tích lũy: -");
            btnSelect.setEnabled(false);
            if (btnCreateNew != null) btnCreateNew.setVisible(true);
        }
    }

    private JLabel createResultLabel(String text) {
        JLabel lb = new JLabel(text);
        lb.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN,14));
        lb.setForeground(Color.DARK_GRAY);
        return lb;
    }

    public CustomerDTO getSelectedCustomer() {
        return selectedCustomer;
    }
}
