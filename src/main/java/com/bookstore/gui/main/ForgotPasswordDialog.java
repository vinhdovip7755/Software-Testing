package com.bookstore.gui.main;

import com.bookstore.bus.AccountBUS;
import com.bookstore.util.EmailUtil;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ForgotPasswordDialog extends JDialog {
    private JTextField txtEmail;
    private JTextField txtOtp;
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;
    private JButton btnSendOtp;
    private JButton btnReset;
    private AccountBUS accountBUS = new AccountBUS();

    private String currentOtp = "";
    private boolean isOtpSent = false;

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

                txtEmail = new JTextField();
        txtEmail.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập email của bạn");
        txtEmail.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                String email = txtEmail.getText().trim();
                if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    txtEmail.putClientProperty(FlatClientProperties.OUTLINE, "error");
                    javax.swing.JOptionPane.showMessageDialog(ForgotPasswordDialog.this, "Định dạng Email không hợp lệ!", "Cảnh báo", javax.swing.JOptionPane.WARNING_MESSAGE);
                    txtEmail.requestFocusInWindow();
                } else {
                    txtEmail.putClientProperty(FlatClientProperties.OUTLINE, null);
                }
            }
        });

        txtOtp = new JTextField();
        txtOtp.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Mã OTP (6 số)");
        txtOtp.setEnabled(false);

        txtNewPassword = new JPasswordField();
        txtNewPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Mật khẩu mới");
        txtNewPassword.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true");
        txtNewPassword.setEnabled(false);

        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Xác nhận mật khẩu mới");
        txtConfirmPassword.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true");
        txtConfirmPassword.setEnabled(false);

        btnSendOtp = new JButton("Nhận mã OTP");
        btnSendOtp.setBackground(Color.decode("#1976D2"));
        btnSendOtp.setForeground(Color.WHITE);
        btnSendOtp.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSendOtp.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSendOtp.addActionListener(this::handleSendOtp);

        btnReset = new JButton("Đổi mật khẩu");
        btnReset.setBackground(Color.decode("#00A364"));
        btnReset.setForeground(Color.WHITE);
        btnReset.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.setEnabled(false);
        btnReset.addActionListener(this::handleReset);

        int gridy = 0;
        gbc.gridx = 0; gbc.gridy = gridy++;
        mainPanel.add(new JLabel("Email:"), gbc);
        gbc.gridy = gridy++;
        mainPanel.add(txtEmail, gbc);

        gbc.gridy = gridy++;
        gbc.insets = new Insets(10, 5, 10, 5);
        mainPanel.add(btnSendOtp, gbc);
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridy = gridy++;
        mainPanel.add(new JSeparator(), gbc);

        gbc.gridy = gridy++;
        mainPanel.add(new JLabel("Mã OTP:"), gbc);
        gbc.gridy = gridy++;
        mainPanel.add(txtOtp, gbc);

        gbc.gridy = gridy++;
        mainPanel.add(new JLabel("Mật khẩu mới:"), gbc);
        gbc.gridy = gridy++;
        mainPanel.add(txtNewPassword, gbc);

        gbc.gridy = gridy++;
        mainPanel.add(new JLabel("Xác nhận mật khẩu mới:"), gbc);
        gbc.gridy = gridy++;
        mainPanel.add(txtConfirmPassword, gbc);

        gbc.gridy = gridy++;
        gbc.insets = new Insets(15, 5, 5, 5);
        mainPanel.add(btnReset, gbc);

        add(mainPanel);
    }

    private void handleSendOtp(ActionEvent e) {
        String email = txtEmail.getText().trim();

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Email!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if email exists in DB (Account uses Email as username)
        if (!accountBUS.isEmailExists(email)) {
            JOptionPane.showMessageDialog(this, "Email này không tồn tại trong hệ thống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        btnSendOtp.setEnabled(false);
        btnSendOtp.setText("Đang gửi...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                currentOtp = EmailUtil.generateOTP();
                return EmailUtil.sendOTP(email, currentOtp);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(ForgotPasswordDialog.this, "Mã OTP đã được gửi đến email của bạn!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        
                        isOtpSent = true;
                        txtEmail.setEditable(false);
                        
                        txtOtp.setEnabled(true);
                        txtNewPassword.setEnabled(true);
                        txtConfirmPassword.setEnabled(true);
                        btnReset.setEnabled(true);
                        txtOtp.requestFocus();

                        javax.swing.Timer timer = new javax.swing.Timer(1000, null);
                        timer.addActionListener(new java.awt.event.ActionListener() {
                            int countdown = 120;
                            @Override
                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                countdown--;
                                if (countdown <= 0) {
                                    btnSendOtp.setText("Gửi lại mã OTP");
                                    btnSendOtp.setEnabled(true);
                                    timer.stop();
                                } else {
                                    btnSendOtp.setText("Gửi lại sau " + countdown + "s");
                                }
                            }
                        });
                        timer.start();

                    } else {
                        JOptionPane.showMessageDialog(ForgotPasswordDialog.this, "Không thể gửi email. Vui lòng kiểm tra lại kết nối mạng hoặc cấu hình SMTP!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        btnSendOtp.setEnabled(true);
                        btnSendOtp.setText("Nhận lại mã OTP");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void handleReset(ActionEvent e) {
        if (!isOtpSent) return;
        
        String otp = txtOtp.getText().trim();
        String newPass = new String(txtNewPassword.getPassword());
        String confirmPass = new String(txtConfirmPassword.getPassword());

        if (otp.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ mã OTP và mật khẩu mới!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!otp.equals(currentOtp)) {
            JOptionPane.showMessageDialog(this, "Mã OTP không chính xác!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String email = txtEmail.getText().trim();
        // Here we update password by email (username is email)
        String result = accountBUS.resetPasswordByEmail(email, newPass); // Re-using existing method since it queries by username
        if (result.equals("OK")) {
            JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, result, "Thất bại", JOptionPane.ERROR_MESSAGE);
        }
    }
}
