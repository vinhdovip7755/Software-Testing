package com.bookstore.gui.main;

import com.bookstore.api.client.ApiException;
import com.bookstore.api.client.PasswordResetApiClient;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class ForgotPasswordDialog extends JDialog {
    private static final int OTP_VALIDITY_SECONDS = 120;

    private JTextField txtEmail;
    private JTextField txtOtp;
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;
    private JButton btnSendOtp;
    private JButton btnVerifyOtp;
    private JButton btnReset;
    private JLabel requestFeedback;
    private InlineFieldValidation emailValidation;
    private InlineFieldValidation otpValidation;
    private InlineFieldValidation newPasswordValidation;
    private InlineFieldValidation confirmPasswordValidation;

    private final PasswordResetApiClient passwordResetApiClient = new PasswordResetApiClient();
    private final Timer otpTimer = new Timer(1000, e -> updateCountdown());
    private String requestedEmail;
    private long otpExpiresAt;
    private boolean otpActive;
    private boolean otpVerified;
    private boolean busy;

    public ForgotPasswordDialog(JFrame parent) {
        super(parent, "Khôi phục mật khẩu", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        initComponents();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 4, 0);

        txtEmail = new JTextField(42);
        txtEmail.setName("reset.email");
        txtEmail.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập email của bạn");
        emailValidation = new InlineFieldValidation(txtEmail,
                () -> InlineFieldValidation.emailError(txtEmail.getText()));

        txtOtp = new JTextField();
        txtOtp.setName("reset.otp");
        txtOtp.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Mã OTP (6 số)");
        otpValidation = new InlineFieldValidation(txtOtp, () -> {
            String otp = txtOtp.getText().trim();
            if (otp.isEmpty()) {
                return "Vui lòng nhập mã OTP!";
            }
            return otp.matches("[0-9]{6}") ? null : "Mã OTP phải gồm đúng 6 chữ số!";
        });

        txtNewPassword = createPasswordField("reset.newPassword", "Mật khẩu mới");
        newPasswordValidation = new InlineFieldValidation(txtNewPassword, () -> {
            String password = new String(txtNewPassword.getPassword());
            if (password.isBlank()) {
                return "Vui lòng nhập mật khẩu mới!";
            }
            return password.trim().length() < 4 ? "Mật khẩu mới phải có ít nhất 4 ký tự!" : null;
        });

        txtConfirmPassword = createPasswordField("reset.confirmPassword", "Xác nhận mật khẩu mới");
        confirmPasswordValidation = new InlineFieldValidation(txtConfirmPassword, () -> {
            String confirmation = new String(txtConfirmPassword.getPassword());
            if (confirmation.isBlank()) {
                return "Vui lòng xác nhận mật khẩu mới!";
            }
            return confirmation.equals(new String(txtNewPassword.getPassword()))
                    ? null : "Mật khẩu xác nhận không khớp!";
        });
        InlineFieldValidation.onChange(txtNewPassword, () -> {
            if (txtConfirmPassword.isEnabled()) {
                confirmPasswordValidation.refreshIfTouched();
            }
        });

        btnSendOtp = createButton("Nhận mã OTP", "#1976D2");
        btnSendOtp.addActionListener(e -> handleSendOtp());
        btnVerifyOtp = createButton("Xác minh OTP", "#1976D2");
        btnVerifyOtp.addActionListener(e -> handleVerifyOtp());
        btnReset = createButton("Đổi mật khẩu", "#00A364");
        btnReset.addActionListener(e -> handleReset());

        addField(mainPanel, gbc, "Email:", txtEmail, emailValidation);
        addRow(mainPanel, gbc, btnSendOtp);
        requestFeedback = new JLabel(" ");
        requestFeedback.setFont(requestFeedback.getFont().deriveFont(12f));
        addRow(mainPanel, gbc, requestFeedback);
        addRow(mainPanel, gbc, new JSeparator());

        addField(mainPanel, gbc, "Mã OTP:", txtOtp, otpValidation);
        addRow(mainPanel, gbc, btnVerifyOtp);
        addRow(mainPanel, gbc, new JSeparator());

        addField(mainPanel, gbc, "Mật khẩu mới:", txtNewPassword, newPasswordValidation);
        addField(mainPanel, gbc, "Xác nhận mật khẩu mới:", txtConfirmPassword, confirmPasswordValidation);
        addRow(mainPanel, gbc, btnReset);

        add(mainPanel);
        refreshControls();
    }

    private static void addField(JPanel panel, GridBagConstraints gbc, String title,
                                 JTextField field, InlineFieldValidation validation) {
        JPanel group = new JPanel(new BorderLayout(0, 3));
        group.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setLabelFor(field);
        group.add(label, BorderLayout.NORTH);
        group.add(field, BorderLayout.CENTER);
        group.add(validation.feedback(), BorderLayout.SOUTH);
        addRow(panel, gbc, group);
    }

    private static void addRow(JPanel panel, GridBagConstraints gbc, JComponent component) {
        panel.add(component, gbc);
        gbc.gridy++;
    }

    private static JButton createButton(String title, String color) {
        JButton button = new JButton(title);
        button.setBackground(Color.decode(color));
        button.setForeground(Color.WHITE);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 14f));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private static JPasswordField createPasswordField(String name, String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setName(name);
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        char defaultEcho = field.getEchoChar();
        JToggleButton eye = new JToggleButton(new FlatSVGIcon("icon/eye_closed.svg", 20, 20));
        eye.setSelectedIcon(new FlatSVGIcon("icon/eye_open.svg", 20, 20));
        eye.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eye.setFocusable(false);
        eye.addActionListener(e -> field.setEchoChar(eye.isSelected() ? (char) 0 : defaultEcho));
        field.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, eye);
        return field;
    }

    private void handleSendOtp() {
        if (!emailValidation.validate()) {
            txtEmail.requestFocusInWindow();
            return;
        }

        String email = txtEmail.getText().trim();
        requestFeedback.setText("Đang gửi yêu cầu...");
        runRequest(() -> passwordResetApiClient.requestOtp(email), () -> {
            requestedEmail = email;
            otpActive = true;
            otpExpiresAt = System.nanoTime() + OTP_VALIDITY_SECONDS * 1_000_000_000L;
            txtOtp.setText("");
            otpValidation.clear();
            requestFeedback.setText("Nếu email có trong hệ thống, mã OTP sẽ được gửi.");
            updateCountdown();
            otpTimer.start();
            refreshControls();
            txtOtp.requestFocusInWindow();
        }, error -> requestFeedback.setText(errorMessage(error, "Không thể gửi mã OTP.")));
    }

    private void handleVerifyOtp() {
        if (!otpValidation.validate()) {
            txtOtp.requestFocusInWindow();
            return;
        }

        String otp = txtOtp.getText().trim();
        btnVerifyOtp.setText("Đang xác minh...");
        runRequest(() -> passwordResetApiClient.verifyOtp(requestedEmail, otp), () -> {
            updateCountdown();
            if (otpActive) {
                otpVerified = true;
                otpValidation.showSuccess("Mã OTP đã được xác minh.");
                refreshControls();
                txtNewPassword.requestFocusInWindow();
            }
        }, error -> {
            if (otpActive) {
                otpValidation.showError(errorMessage(error, "Không thể xác minh mã OTP."));
                txtOtp.requestFocusInWindow();
            }
        });
    }

    private void handleReset() {
        if (!newPasswordValidation.validate()) {
            txtNewPassword.requestFocusInWindow();
            return;
        }
        if (!confirmPasswordValidation.validate()) {
            txtConfirmPassword.requestFocusInWindow();
            return;
        }

        String otp = txtOtp.getText().trim();
        String password = new String(txtNewPassword.getPassword());
        btnReset.setText("Đang đổi mật khẩu...");
        runRequest(() -> passwordResetApiClient.resetPassword(requestedEmail, otp, password), () -> {
            JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }, error -> {
            if (otpActive && error instanceof ApiException api && api.getStatusCode() == 422) {
                resetPasswordFields();
                refreshControls();
                otpValidation.showError(api.getMessage());
                txtOtp.requestFocusInWindow();
            } else if (otpActive) {
                confirmPasswordValidation.showError(errorMessage(error, "Không thể đổi mật khẩu."));
            }
        });
    }

    private void runRequest(Runnable request, Runnable onSuccess, Consumer<Throwable> onError) {
        busy = true;
        refreshControls();
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                request.run();
                return null;
            }

            @Override
            protected void done() {
                if (!isDisplayable()) {
                    return;
                }
                busy = false;
                btnVerifyOtp.setText("Xác minh OTP");
                btnReset.setText("Đổi mật khẩu");
                refreshControls();

                try {
                    get();
                    onSuccess.run();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    onError.accept(ex);
                } catch (ExecutionException ex) {
                    onError.accept(ex.getCause());
                }
            }
        }.execute();
    }

    private static String errorMessage(Throwable error, String fallback) {
        return error instanceof ApiException api ? api.getMessage() : fallback;
    }

    private void updateCountdown() {
        if (!otpActive) {
            return;
        }
        long remaining = otpExpiresAt - System.nanoTime();
        if (remaining > 0) {
            long seconds = (remaining + 999_999_999L) / 1_000_000_000L;
            btnSendOtp.setText("Gửi lại sau " + seconds + "s");
            return;
        }

        otpTimer.stop();
        otpActive = false;
        resetPasswordFields();
        txtOtp.setText("");
        otpValidation.showError("Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới.");
        refreshControls();
    }

    private void resetPasswordFields() {
        otpVerified = false;
        txtNewPassword.setText("");
        txtConfirmPassword.setText("");
        newPasswordValidation.clear();
        confirmPasswordValidation.clear();
    }

    private void refreshControls() {
        txtEmail.setEditable(!busy && requestedEmail == null);
        txtOtp.setEnabled(otpActive);
        txtOtp.setEditable(!busy && !otpVerified);
        txtNewPassword.setEnabled(otpVerified);
        txtNewPassword.setEditable(!busy);
        txtConfirmPassword.setEnabled(otpVerified);
        txtConfirmPassword.setEditable(!busy);
        btnSendOtp.setEnabled(!busy && !otpActive);
        if (!otpActive) {
            btnSendOtp.setText(requestedEmail == null ? "Nhận mã OTP" : "Gửi lại mã OTP");
        }
        btnVerifyOtp.setEnabled(!busy && otpActive && !otpVerified);
        btnReset.setEnabled(!busy && otpVerified);
        getRootPane().setDefaultButton(otpVerified ? btnReset : otpActive ? btnVerifyOtp : btnSendOtp);
    }

    @Override
    public void dispose() {
        otpTimer.stop();
        super.dispose();
    }
}
