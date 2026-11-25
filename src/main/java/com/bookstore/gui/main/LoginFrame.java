package com.bookstore.gui.main;

import com.bookstore.api.client.AuthApiClient;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.util.AppConstant;
import com.bookstore.util.SharedData;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private InlineFieldValidation emailValidation;
    private InlineFieldValidation passwordValidation;
    private final AuthApiClient authApiClient = new AuthApiClient();

    public LoginFrame() {
        setTitle("Ứng Dụng Quản Lý Bán Sách");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        add(mainPanel);

        JPanel loginCard = new JPanel();
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));
        loginCard.setBackground(Color.WHITE);
        loginCard.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        loginCard.putClientProperty(FlatClientProperties.STYLE, "arc: 20;");

        JLabel lbTitle = new JLabel("Đăng nhập");
        lbTitle.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 28));
        lbTitle.setForeground(Color.BLACK);
        lbTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtUsername = new JTextField();
        txtUsername.setName("login.email");
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Email của bạn");
        styleField(txtUsername);
        emailValidation = new InlineFieldValidation(txtUsername,
                () -> InlineFieldValidation.emailError(txtUsername.getText()));

        txtPassword = new JPasswordField();
        txtPassword.setName("login.password");
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Mật khẩu");
        styleField(txtPassword);
        passwordValidation = new InlineFieldValidation(txtPassword, () ->
                new String(txtPassword.getPassword()).isBlank() ? "Vui lòng nhập mật khẩu!" : null);

        char defaultEcho = txtPassword.getEchoChar();
        JToggleButton btnToggleEye = new JToggleButton();
        btnToggleEye.setIcon(new com.formdev.flatlaf.extras.FlatSVGIcon("icon/eye_closed.svg", 20, 20));
        btnToggleEye.setSelectedIcon(new com.formdev.flatlaf.extras.FlatSVGIcon("icon/eye_open.svg", 20, 20));
        btnToggleEye.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnToggleEye.setFocusable(false);
        btnToggleEye.addActionListener(e -> {
            if (btnToggleEye.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar(defaultEcho);
            }
        });
        txtPassword.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, btnToggleEye);

        btnLogin = new JButton("Đăng Nhập Ngay");
        btnLogin.setName("login.submit");
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setFocusPainted(false);
        btnLogin.putClientProperty(FlatClientProperties.STYLE,
                "background: #00A364;" +
                "foreground: #FFFFFF;" +
                "font: bold 16;" +
                "borderWidth: 0;" +
                "focusWidth: 0;" +
                "innerFocusWidth: 0;" +
                "borderColor: #ff7777;" +
                "focusedBorderColor: #ff7777;" +
                "arc: 10;" +
                "margin: 10,20,10,20");

        JLabel lbForgotPass = new JLabel("<html><u>Quên mật khẩu?</u></html>");
        lbForgotPass.setForeground(Color.BLUE);
        lbForgotPass.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lbForgotPass.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new ForgotPasswordDialog(LoginFrame.this).setVisible(true);
            }
        });

        JPanel forgotPasswordPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        forgotPasswordPanel.setBackground(Color.WHITE);
        forgotPasswordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        forgotPasswordPanel.setMaximumSize(new Dimension(1000, 25));
        forgotPasswordPanel.add(lbForgotPass);

        loginCard.add(lbTitle);
        loginCard.add(Box.createVerticalStrut(30));
        loginCard.add(createInputGroup("Email:", txtUsername, emailValidation));
        loginCard.add(Box.createVerticalStrut(15));
        loginCard.add(createInputGroup("Mật khẩu:", txtPassword, passwordValidation));
        loginCard.add(Box.createVerticalStrut(8));
        loginCard.add(forgotPasswordPanel);
        loginCard.add(Box.createVerticalStrut(20));
        loginCard.add(btnLogin);
        loginCard.setPreferredSize(new Dimension(450, 420));

        mainPanel.add(loginCard);

        btnLogin.addActionListener(e -> handleLogin());
        JRootPane rootPane = this.getRootPane();
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "clickLogin");
        rootPane.getActionMap().put("clickLogin", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnLogin.doClick();
            }
        });
    }

    private JPanel createInputGroup(String labelText, JComponent field, InlineFieldValidation validation) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setMaximumSize(new Dimension(1000, 90));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        label.setLabelFor(field);

        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        panel.add(validation.feedback(), BorderLayout.SOUTH);

        return panel;
    }

    private void styleField(JComponent field) {
        field.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));
        field.putClientProperty(FlatClientProperties.STYLE,
                "arc: 10;" +
                "borderColor: #CCCCCC;" +
                "focusWidth: 1;" +
                "margin: 5,10,5,10;" +
                "showClearButton: true"
        );
    }

    private void handleLogin() {
        if (!emailValidation.validate()) {
            txtUsername.requestFocusInWindow();
            return;
        }

        if (!passwordValidation.validate()) {
            txtPassword.requestFocusInWindow();
            return;
        }

        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        try {
            com.bookstore.api.contract.ApiSessionDTO session = authApiClient.login(username, password);
            EmployeeDTO employee = session.getEmployee();
            if (employee != null) {
                SharedData.currentUser = employee;
                SharedData.userPermissions = session.getPermissions();
                JOptionPane.showMessageDialog(this, "Xin chào " + employee.getEmployeeName() + "!",
                    "Đăng nhập thành công", JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
                new MainFrame().setVisible(true);
            }
        } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
        }
    }
}
