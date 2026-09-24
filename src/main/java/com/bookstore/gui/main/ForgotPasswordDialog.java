package com.bookstore.gui.main;

import com.bookstore.bus.AccountBUS;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;

public class ForgotPasswordDialog extends JDialog {
    private JTextField txtName;
    private JTextField txtPhone;
    private JTextField txtDob;
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;
    private JButton btnVerify;
    private JButton btnReset;
    private AccountBUS accountBUS = new AccountBUS();

    private boolean isVerified = false;

    public ForgotPasswordDialog(JFrame parent) {
        super(parent, "Khôi phục mật khẩu", true);
        setSize(450, 450);
        setLocationRelativeTo(parent);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        txtName = new JTextField();
        txtName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Họ và tên");

        txtPhone = new JTextField();
        txtPhone.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Số điện thoại đăng ký");

        txtDob = new JTextField();
        txtDob.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ngày sinh (dd/MM/yyyy)");

        txtNewPassword = new JPasswordField();
        txtNewPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Mật khẩu mới");
        txtNewPassword.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true");
        txtNewPassword.setEnabled(false);

        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Xác nhận mật khẩu mới");
        txtConfirmPassword.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true");
        txtConfirmPassword.setEnabled(false);

        btnVerify = new JButton("Kiểm tra thông tin");
        btnVerify.setBackground(Color.decode("#1976D2"));
        btnVerify.setForeground(Color.WHITE);
        btnVerify.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnVerify.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVerify.addActionListener(this::handleVerify);

        btnReset = new JButton("Đổi mật khẩu");
        btnReset.setBackground(Color.decode("#00A364"));
        btnReset.setForeground(Color.WHITE);
        btnReset.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.setEnabled(false);
        btnReset.addActionListener(this::handleReset);

        int gridy = 0;
        gbc.gridx = 0; gbc.gridy = gridy++;
        mainPanel.add(new JLabel("Họ và tên:"), gbc);
        gbc.gridy = gridy++;
        mainPanel.add(txtName, gbc);

        gbc.gridy = gridy++;
        mainPanel.add(new JLabel("Số điện thoại:"), gbc);
        gbc.gridy = gridy++;
        mainPanel.add(txtPhone, gbc);

        gbc.gridy = gridy++;
        mainPanel.add(new JLabel("Ngày sinh (dd/MM/yyyy):"), gbc);
        gbc.gridy = gridy++;
        mainPanel.add(txtDob, gbc);

        gbc.gridy = gridy++;
        gbc.insets = new Insets(10, 5, 10, 5);
        mainPanel.add(btnVerify, gbc);
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridy = gridy++;
        mainPanel.add(new JSeparator(), gbc);

        gbc.gridy = gridy++;
        mainPanel.add(new JLabel("Mật khẩu mới:"), gbc);
        gbc.gridy = gridy++;
        mainPanel.add(txtNewPassword, gbc);

        gbc.gridy = gridy++;
        mainPanel.add(new JLabel("Xácầnhận mật khẩu mới:"), gbc);
        gbc.gridy = gridy++;
        mainPanel.add(txtConfirmPassword, gbc);

        gbc.gridy = gridy++;
        gbc.insets = new Insets(15, 5, 5, 5);
        mainPanel.add(btnReset, gbc);

        add(mainPanel);
    }

    private void handleVerify(ActionEvent e) {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String dobStr = txtDob.getText().trim();

        if (name.isEmpty() || phone.isEmpty() || dobStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Họ tên, SĐT và Ngày sinh!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.sql.Date dob = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            java.util.Date parsed = sdf.parse(dobStr);
            dob = new java.sql.Date(parsed.getTime());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ngày sinh không hợp lệ! Vui lòng nhập đúng định dạng dd/MM/yyyy", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String result = accountBUS.verifyEmployeeInfoByPhone(name, phone, dob);
        if (result.equals("OK")) {
            JOptionPane.showMessageDialog(this, "Xác minh thông tin chính xác! Bạn có thể nhập mật khẩu mới.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            isVerified = true;
            txtName.setEditable(false);
            txtPhone.setEditable(false);
            txtDob.setEditable(false);
            btnVerify.setEnabled(false);

            txtNewPassword.setEnabled(true);
            txtConfirmPassword.setEnabled(true);
            btnReset.setEnabled(true);
            txtNewPassword.requestFocus();
        } else {
            JOptionPane.showMessageDialog(this, result, "Xác minh thất bại", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleReset(ActionEvent e) {
        if (!isVerified) return;

        String newPass = new String(txtNewPassword.getPassword());
        String confirmPass = new String(txtConfirmPassword.getPassword());

        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mật khẩu mới!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu xácầnhận không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String phone = txtPhone.getText().trim();
        String result = accountBUS.resetPasswordByPhone(phone, newPass);
        if (result.equals("OK")) {
            JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, result, "thất bại", JOptionPane.ERROR_MESSAGE);
        }
    }
}
