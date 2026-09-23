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

public class EmployeeEditDialog extends JDialog {
    private JTextField txtName, txtPhone, txtBaseSalary, txtSalaryFactor;
    private JDateChooser dateChooser;
    private JComboBox<String> cboRole;
    private JCheckBox chkStatus;
    private JButton btnSave, btnCancel;

    private EmployeeDTO employee;
    private EmployeeBUS employeeBUS = new EmployeeBUS();
    private RoleBUS roleBUS = new RoleBUS();
    private EmployeePanel parentPanel;
    private List<RoleDTO> roleList;

    public EmployeeEditDialog(JFrame owner, EmployeePanel parentPanel, EmployeeDTO employee) {
        super(owner, "Chỉnh Sửa Nhân Viên", true);
        this.parentPanel = parentPanel;
        this.employee = employee;
        initUI();
        fillData();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 10));
        setSize(450, 700);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);

        JLabel lbHeader = new JLabel("Cập nhật NHÂN VIÊN", SwingConstants.CENTER);
        lbHeader.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 18));
        lbHeader.setForeground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        lbHeader.setBorder(new EmptyBorder(20, 0, 10, 0));
        add(lbHeader, BorderLayout.NORTH);

        JPanel pForm = new JPanel(new GridLayout(7, 1, 10, 5));
        pForm.setBorder(new EmptyBorder(0, 30, 0, 30));
        pForm.setOpaque(false);

        txtName = createInput("Nhập họ tên...");
        txtPhone = createInput("Nhập 10 số điện thoại...");
        txtBaseSalary = createNumericInput("Ví dụ: 5000000");
        txtSalaryFactor = createNumericInput("Ví dụ: 1.5");

        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd/MM/yyyy");
        dateChooser.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));
        dateChooser.setPreferredSize(new Dimension(0, 38));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -18);
        java.util.Date maxBirthday = cal.getTime();
        dateChooser.setMaxSelectableDate(maxBirthday);
        ((com.toedter.calendar.JTextFieldDateEditor) dateChooser.getDateEditor()).setEditable(false);


        cboRole = new JComboBox<>();
        cboRole.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));
        cboRole.setPreferredSize(new Dimension(0, 38));
        loadRoles();

        chkStatus = new JCheckBox("Còn làm việc");
        chkStatus.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        chkStatus.setOpaque(false);

        pForm.add(createFieldPanel("Họ và tên:", txtName));
        pForm.add(createFieldPanel("Số điện thoại:", txtPhone));
        pForm.add(createFieldPanel("Ngày sinh:", dateChooser));
        pForm.add(createFieldPanel("Lương cơ bản:", txtBaseSalary));
        pForm.add(createFieldPanel("Hệ số lương:", txtSalaryFactor));
        pForm.add(createFieldPanel("Chức vụ:", cboRole));

        JPanel pStatus = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pStatus.setOpaque(false);
        pStatus.add(chkStatus);
        pForm.add(pStatus);

        add(pForm, BorderLayout.CENTER);

        JPanel pButton = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        pButton.setBorder(new EmptyBorder(10, 0, 20, 0));
        pButton.setOpaque(false);

        btnSave = new JButton("Lưu thay đổi");
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
        btnSave.addActionListener(e -> saveEmployee());
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
                if (string != null && string.matches("^[0-9.]*$")) { super.insertString(fb, offset, string, attr); }
            }
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text != null && text.matches("^[0-9.]*$")) { super.replace(fb, offset, length, text, attrs); }
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

    private void fillData() {
        if (employee != null) {
            txtName.setText(employee.getEmployeeName());
            txtName.setEditable(false);
            txtName.setBackground(Color.decode("#F5F5F5"));
            txtName.setToolTipText("Không được sửa thông tin cá nhân (Họ tên)");

            txtPhone.setText(employee.getEmployeePhone());
            txtPhone.setEditable(false);
            txtPhone.setBackground(Color.decode("#F5F5F5"));
            txtPhone.setToolTipText("Không được sửa thông tin cá nhân (Số điện thoại)");

            dateChooser.setDate(employee.getBirthday());
            dateChooser.setEnabled(false);
            dateChooser.setToolTipText("Không được sửa thông tin cá nhân (Ngày sinh)");

            txtBaseSalary.setText(String.format("%.0f", employee.getBaseSalary()));
            txtSalaryFactor.setText(String.valueOf(employee.getSalaryFactor()));

            cboRole.setSelectedItem(employee.getRoleName());
            chkStatus.setSelected(employee.getStatus() == 1);
        }
    }

    private void saveEmployee() {
        try {
            EmployeeDTO temp = new EmployeeDTO();
            temp.setEmployeeId(employee.getEmployeeId());
            temp.setEmployeeName(txtName.getText().trim());
            temp.setEmployeePhone(txtPhone.getText().trim());

            java.util.Date utilDate = dateChooser.getDate();
            if (utilDate != null) {
                temp.setBirthday(new Date(utilDate.getTime()));
            } else {
                temp.setBirthday(null);
            }

            String baseSalaryStr = txtBaseSalary.getText().trim();
            temp.setBaseSalary(baseSalaryStr.isEmpty() ? 0 : Double.parseDouble(baseSalaryStr));

            String sửalaryFactorStr = txtSalaryFactor.getText().trim();
            temp.setSalaryFactor(sửalaryFactorStr.isEmpty() ? 1 : Double.parseDouble(sửalaryFactorStr));

            temp.setStatus(chkStatus.isSelected() ? 1 : 0);

            String selectedRole = (String) cboRole.getSelectedItem();
            for (RoleDTO r : roleList) {
                if (r.getRoleName().equals(selectedRole)) {
                    temp.setRoleId(r.getRoleId());
                    break;
                }
            }

                        String result = employeeBUS.updateEmployee(temp);
            if ("OK".equals(result) || result.toLowerCase().contains("thành công") || result.toLowerCase().contains("công")) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                parentPanel.refresh();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, result);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Lương hoặc hệ số lương không hợp lệ (không nhập quá nhiều dấu chấm)!");
        }
    }
}
