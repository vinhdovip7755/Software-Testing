package com.bookstore.gui.panel.AccountTab;

import com.bookstore.api.client.ApiException;
import com.bookstore.api.client.ProfileApiClient;
import com.bookstore.api.contract.ProfileDTO;
import com.bookstore.api.contract.ProfileUpdateRequestDTO;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.util.SharedData;
import com.bookstore.util.AppConstant;
import com.bookstore.util.Refreshable;
import com.bookstore.gui.main.MainFrame;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.Locale;

public class ProfilePanel extends JPanel implements Refreshable {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtName, txtPhone, txtSalary, txtRole, txtDayIn;
    private JDateChooser dchBirthday;
    private JButton btnUpdate;
    private final ProfileApiClient profileApiClient = new ProfileApiClient();

    public ProfilePanel() {
        initUI();
        loadData();
    }

    @Override
    public void refresh() {
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.decode("#F5F7FA"));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Hồ Sơ Cá Nhân");
        titleLabel.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setIcon(new FlatSVGIcon("icon/account_icon.svg").derive(32, 32));
        titleLabel.setIconTextGap(15);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.decode("#F5F7FA"));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.putClientProperty(FlatClientProperties.STYLE, "arc: 20;");
        formCard.setBorder(new EmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblInfoNS = new JLabel("Thông tin nhân sự");
        lblInfoNS.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 18));
        lblInfoNS.setForeground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        formCard.add(lblInfoNS, gbc);

        gbc.gridy++; gbc.gridwidth = 1;
        txtName = new JTextField();
        formCard.add(createInputGroup("Họ và tên", txtName), gbc);

        gbc.gridx = 1;
        txtRole = new JTextField(); txtRole.setEditable(false);
        formCard.add(createInputGroup("Vai trò", txtRole), gbc);
        
        gbc.gridx = 0; gbc.gridy++;
        txtSalary = new JTextField(); txtSalary.setEditable(false);
        formCard.add(createInputGroup("Lương cơ bản", txtSalary), gbc);
        
        gbc.gridx = 1;
        txtDayIn = new JTextField(); txtDayIn.setEditable(false);
        formCard.add(createInputGroup("Ngày vào làm", txtDayIn), gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 10, 10, 10);
        JLabel lblInfo = new JLabel("Thông tin cá nhân");
        lblInfo.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 18));
        lblInfo.setForeground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        formCard.add(lblInfo, gbc);

        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridy++; gbc.gridwidth = 1;
        txtPhone = new JTextField();
        formCard.add(createInputGroup("Số điện thoại", txtPhone), gbc);

        gbc.gridx = 1;
        dchBirthday = new JDateChooser();
        dchBirthday.setDateFormatString("dd/MM/yyyy");
        dchBirthday.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 15));
        dchBirthday.setPreferredSize(new Dimension(300, 40));
        JPanel pnlDate = new JPanel(new BorderLayout(0, 5));
        pnlDate.setBackground(Color.WHITE);
        JLabel lblDate = new JLabel("Ngày sinh");
        lblDate.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        lblDate.setForeground(Color.DARK_GRAY);
        pnlDate.add(lblDate, BorderLayout.NORTH);
        pnlDate.add(dchBirthday, BorderLayout.CENTER);
        formCard.add(pnlDate, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 10, 10, 10);
        JLabel lblAcc = new JLabel("Cài đặt tài khoản");
        lblAcc.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 18));
        lblAcc.setForeground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        formCard.add(lblAcc, gbc);

        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridy++; gbc.gridwidth = 1;
        txtUsername = new JTextField();
        formCard.add(createInputGroup("Email", txtUsername), gbc);

        gbc.gridx = 1;
        txtPassword = new JPasswordField();
        formCard.add(createInputGroup("Mật khẩu mới (Để trống nếu không đổi)", txtPassword), gbc);

        contentPanel.add(formCard);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        footerPanel.setBackground(Color.decode("#F5F7FA"));

        btnUpdate = new JButton("Lưu Thay Đổi");
        btnUpdate.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 16));
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        btnUpdate.setPreferredSize(new Dimension(200, 45));
        btnUpdate.putClientProperty(FlatClientProperties.STYLE, "arc: 10; hoverBackground: #00A364;");
        btnUpdate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUpdate.setIcon(new FlatSVGIcon("icon/edit_icon.svg").derive(20, 20));
        btnUpdate.setIconTextGap(10);
        btnUpdate.addActionListener(this::handleUpdate);

        footerPanel.add(btnUpdate);

        add(headerPanel, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel createInputGroup(String labelText, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        label.setForeground(Color.DARK_GRAY);
        field.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 15));
        field.setPreferredSize(new Dimension(300, 40));
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 8; margin: 0,10,0,10;");
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private void loadData() {
        try {
            ProfileDTO profile = profileApiClient.getProfile();
            EmployeeDTO emp = profile.employee();
            SharedData.currentUser = emp;
            txtName.setText(emp.getEmployeeName());
            txtPhone.setText(emp.getEmployeePhone());
            if (emp.getBirthday() != null) {
                dchBirthday.setDate(emp.getBirthday());
            }
            
            NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
            txtSalary.setText(currencyFormat.format(emp.getBaseSalary()) + " VNĐ");
            
            if (emp.getDayIn() != null) {
                txtDayIn.setText(new java.text.SimpleDateFormat("dd/MM/yyyy").format(emp.getDayIn()));
            }
            
            txtRole.setText(emp.getRoleName());
            txtUsername.setText(profile.username());
        } catch (ApiException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi API", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate(ActionEvent e) {
        if (SharedData.currentUser != null) {
            EmployeeDTO emp = SharedData.currentUser;
            
            String newName = txtName.getText().trim();
            String newPhone = txtPhone.getText().trim();
            
            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên nhân viên không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                txtName.requestFocus();
                return;
            }
            if (!newName.matches("^(?=.*\\p{L})[\\p{L}\\s.'\\u2019\\u2018\\u0060\\u02BB\\u2013-]+$")) {
                JOptionPane.showMessageDialog(this, "Tên nhân viên không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                txtName.requestFocus();
                return;
            }
            if (newPhone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Số điện thoại không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                txtPhone.requestFocus();
                return;
            }
            if (!newPhone.matches("^0\\d{9}$")) {
                JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ (Phải có 10 chữ số và bắt đầu bằng số 0)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                txtPhone.requestFocus();
                return;
            }
            
            String newUsername = txtUsername.getText().trim();
            if (!newUsername.isEmpty() && !newUsername.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                JOptionPane.showMessageDialog(this, "Email không đúng định dạng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                txtUsername.requestFocus();
                return;
            }

            String newPass = new String(txtPassword.getPassword()).trim();
            if (dchBirthday.getDate() != null) {
                java.sql.Date birthday = new java.sql.Date(dchBirthday.getDate().getTime());
                updateProfile(newName, newPhone, birthday, newUsername, newPass);
            } else {
                updateProfile(newName, newPhone, null, newUsername, newPass);
            }
        }
    }

    private void updateProfile(String name, String phone, java.sql.Date birthday, String username, String password) {
        try {
            ProfileDTO updated = profileApiClient.updateProfile(
                    new ProfileUpdateRequestDTO(name, phone, birthday, username, password)
            );
            SharedData.currentUser = updated.employee();
            if (MainFrame.getInstance() != null) {
                MainFrame.getInstance().updateUserInfo();
            }
            JOptionPane.showMessageDialog(this, "Cập nhật thông tin thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            txtPassword.setText("");
            txtUsername.setText(updated.username());
        } catch (ApiException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi API", JOptionPane.ERROR_MESSAGE);
        }
    }
}
