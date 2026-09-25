package com.bookstore.gui.panel.EmployeeTab;

import com.bookstore.bus.EmployeeBUS;
import com.bookstore.bus.RoleBUS;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.RoleDTO;
import com.bookstore.util.AppConstant;
import com.formdev.flatlaf.FlatClientProperties;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.sql.Date;
import java.util.Calendar;
import java.util.List;

public class EmployeeAddDialog extends JDialog {
    private JTextField txtName, txtPhone, txtEmail, txtBaseSalary;
    private JDateChooser dateBirthday, dateDayIn;
    private JComboBox<String> cboRole;
    private JButton btnSave, btnCancel;

    private final EmployeeBUS employeeBUS = new EmployeeBUS();
    private final RoleBUS roleBUS = new RoleBUS();
    private final EmployeePanel parentPanel;
    private List<RoleDTO> roleList;

    public EmployeeAddDialog(JFrame owner, EmployeePanel parentPanel) {
        super(owner, "Thêm Nhân Viên Mới", true);
        this.parentPanel = parentPanel;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 10));
        setSize(450, 650);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);

        JLabel lbHeader = new JLabel("THÊM NHÂN VIÊN MỚI", SwingConstants.CENTER);
        lbHeader.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 18));
        lbHeader.setForeground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        lbHeader.setBorder(new EmptyBorder(20, 0, 10, 0));
        add(lbHeader, BorderLayout.NORTH);

        JPanel pForm = new JPanel(new GridLayout(7, 1, 10, 5));
        pForm.setBorder(new EmptyBorder(0, 30, 0, 30));
        pForm.setOpaque(false);

        txtName = createInput("Nhập họ tên...");
        txtPhone = createInput("Nhập 10 số điện thoại...");
                txtEmail = createInput("Nhập email...");
        txtEmail.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                String email = txtEmail.getText().trim();
                if (!email.isEmpty() && !email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                    txtEmail.putClientProperty(com.formdev.flatlaf.FlatClientProperties.OUTLINE, "error");
                    javax.swing.JOptionPane.showMessageDialog(EmployeeAddDialog.this, "Định dạng Email không hợp lệ!", "Cảnh báo", javax.swing.JOptionPane.WARNING_MESSAGE);
                    txtEmail.requestFocusInWindow();
                } else {
                    txtEmail.putClientProperty(com.formdev.flatlaf.FlatClientProperties.OUTLINE, null);
                }
            }
        });
        txtBaseSalary = createNumericInput("Nhập lương cơ bản (VD: 5000000)...");

        dateBirthday = new JDateChooser();
        dateBirthday.setDateFormatString("dd/MM/yyyy");
        dateBirthday.setPreferredSize(new Dimension(0, 35));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -18);
        java.util.Date maxBirthday = cal.getTime();
        dateBirthday.setMaxSelectableDate(maxBirthday);
        ((com.toedter.calendar.JTextFieldDateEditor) dateBirthday.getDateEditor()).setEditable(false);

        dateDayIn = new JDateChooser();
        dateDayIn.setDate(new java.util.Date());
        dateDayIn.setDateFormatString("dd/MM/yyyy");
        dateDayIn.setPreferredSize(new Dimension(0, 35));
        dateDayIn.setMaxSelectableDate(new java.util.Date());
        ((com.toedter.calendar.JTextFieldDateEditor) dateDayIn.getDateEditor()).setEditable(false);

        cboRole = new JComboBox<>();
        cboRole.setPreferredSize(new Dimension(0, 35));
        loadRoles();

        pForm.add(createFieldPanel("<html>Họ và tên <font color='red'>*</font>:</html>", txtName));
        pForm.add(createFieldPanel("<html>Số điện thoại <font color='red'>*</font>:</html>", txtPhone));
        pForm.add(createFieldPanel("<html>Email <font color='red'>*</font>:</html>", txtEmail));
        pForm.add(createFieldPanel("<html>Ngày sinh <font color='red'>*</font>:</html>", dateBirthday));
        pForm.add(createFieldPanel("<html>Lương cơ bản <font color='red'>*</font>:</html>", txtBaseSalary));
        pForm.add(createFieldPanel("<html>Ngày vào làm <font color='red'>*</font>:</html>", dateDayIn));
        pForm.add(createFieldPanel("<html>Chức vụ <font color='red'>*</font>:</html>", cboRole));

        add(pForm, BorderLayout.CENTER);

        JPanel pButton = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        pButton.setBorder(new EmptyBorder(10, 0, 20, 0));
        pButton.setOpaque(false);

        btnSave = new JButton("Thêm mới");
        btnSave.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnSave.setPreferredSize(new Dimension(140, 40));

        btnCancel = new JButton("Hủy bỏ");
        btnCancel.setBackground(Color.decode("#E0E0E0"));
        btnCancel.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnCancel.setPreferredSize(new Dimension(100, 40));

        pButton.add(btnSave);
        pButton.add(btnCancel);
        add(pButton, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> saveNewEmployee());
    }

    private void loadRoles() {
        roleList = roleBUS.getAllRoles();
        for (RoleDTO role : roleList) {
            cboRole.addItem(role.getRoleName());
        }
    }

    private JTextField createInput(String placeholder) {
        JTextField txt = new JTextField();
        txt.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));
        txt.setPreferredSize(new Dimension(0, 35));
        txt.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        txt.putClientProperty(FlatClientProperties.STYLE, "arc: 5; margin: 4, 10, 4, 10;");
        return txt;
    }

    private JTextField createNumericInput(String placeholder) {
        JTextField txt = createInput(placeholder);
        ((AbstractDocument) txt.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string != null && string.matches("^[0-9]*$")) { super.insertString(fb, offset, string, attr); }
            }
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text != null && text.matches("^[0-9]*$")) { super.replace(fb, offset, length, text, attrs); }
            }
        });
        return txt;
    }

    private JPanel createFieldPanel(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setOpaque(false);
        JLabel lb = new JLabel(label);
        lb.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 13));
        p.add(lb, BorderLayout.NORTH);

        JPanel pWrap = new JPanel(new BorderLayout());
        pWrap.setOpaque(false);
        pWrap.add(comp, BorderLayout.NORTH);
        p.add(pWrap, BorderLayout.CENTER);
        return p;
    }

    private void saveNewEmployee() {
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ và tên không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtName.requestFocus();
            return;
        }
        if (!txtName.getText().trim().matches("^[\\p{L}\\s.'-]+$")) {
            JOptionPane.showMessageDialog(this, "Họ và tên chỉ được chứa chữ cái và khoảng trắng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtName.requestFocus();
            return;
        }
        if (txtPhone.getText().trim().isEmpty() || !txtPhone.getText().trim().matches("^0\\d{9}$")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại phải có 10 số và bắt đầu bằng 0!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtPhone.requestFocus();
            return;
        }
        if (txtEmail.getText().trim().isEmpty() || !txtEmail.getText().trim().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            JOptionPane.showMessageDialog(this, "Email không đúng định dạng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtEmail.requestFocus();
            return;
        }
        if (dateBirthday.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày sinh!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            dateBirthday.requestFocus();
            return;
        }
        if (txtBaseSalary.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lương cơ bản không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtBaseSalary.requestFocus();
            return;
        }
        if (!txtBaseSalary.getText().trim().matches("^\\d+(\\.\\d+)?$")) {
            JOptionPane.showMessageDialog(this, "Lương cơ bản phải là số dương!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtBaseSalary.requestFocus();
            return;
        }
        if (dateDayIn.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày vào làm!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            dateDayIn.requestFocus();
            return;
        }

        try {
            EmployeeDTO temp = new EmployeeDTO();
            temp.setEmployeeName(txtName.getText().trim());
            temp.setEmployeePhone(txtPhone.getText().trim());
            temp.setEmail(txtEmail.getText().trim());

            java.util.Date utilBirthday = dateBirthday.getDate();
            java.util.Date utilDayIn = dateDayIn.getDate();
            if (utilBirthday != null && utilDayIn != null) {
                temp.setBirthday(new Date(utilBirthday.getTime()));
                temp.setDayIn(new Date(utilDayIn.getTime()));
            } else {
                temp.setBirthday(null);
                temp.setDayIn(null);
            }

            String baseSalaryStr = txtBaseSalary.getText().trim();
            temp.setBaseSalary(baseSalaryStr.isEmpty() ? 0 : Double.parseDouble(baseSalaryStr));

            String selectedRole = (String) cboRole.getSelectedItem();
            for (RoleDTO r : roleList) {
                if (r.getRoleName().equals(selectedRole)) {
                    temp.setRoleId(r.getRoleId());
                    break;
                }
            }

            String result = employeeBUS.addEmployee(temp);
            JOptionPane.showMessageDialog(this, result);

            if (result.toLowerCase().contains("thành công") || result.toLowerCase().contains("công")) {
                parentPanel.refresh();
                dispose();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi dữ liệu đầu vào!");
        }
    }
}
