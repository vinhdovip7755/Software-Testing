package com.bookstore.gui.panel.SellingTab;

import com.bookstore.bus.CustomerBUS;
import com.bookstore.dto.CustomerDTO;
import com.bookstore.util.AppConstant;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

public class CustomerEditDialog extends JDialog {
    private JTextField txtName;
    private JTextField txtPhone;
    private JButton btnSave;
    private JButton btnCancel;
    private JLabel lbHeader;

    private CustomerDTO customer;
    private CustomerBUS customerBUS;
    private CustomerPanel parentPanel;

    private boolean isEditMode;
    private boolean success = false;

    public CustomerEditDialog(Window owner, CustomerPanel parentPanel, CustomerDTO customer) {
        this(owner, parentPanel, customer, null);
    }

    public CustomerEditDialog(Window owner, CustomerPanel parentPanel, CustomerDTO customer, String initialPhone) {
        super(owner, customer == null ? "Thêm Khách Hàng Mới" : "Chỉnh Sửa Khách Hàng", ModalityType.APPLICATION_MODAL);

        this.parentPanel = parentPanel;
        this.customer = customer;
        this.isEditMode = (customer != null);
        this.customerBUS = new CustomerBUS();

        initUI();

        if (isEditMode) {
            fillData();
        } else if (initialPhone != null && !initialPhone.trim().isEmpty()) {
            txtPhone.setText(initialPhone.trim());
            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowOpened(java.awt.event.WindowEvent e) {
                    txtName.requestFocusInWindow();
                }
            });
        }
    }

    public boolean isSuccess() {
        return success;
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        setSize(400, 350);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);

        String headerText = isEditMode ? "Cập nhật KHÁCH HÀNG" : "THÊM KHÁCH HÀNG MỚI";
        lbHeader = new JLabel(headerText, SwingConstants.CENTER);
        lbHeader.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 18));
        lbHeader.setForeground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        lbHeader.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(lbHeader, BorderLayout.NORTH);

        JPanel pForm = new JPanel(new GridLayout(2, 1, 10, 15));
        pForm.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));
        pForm.setOpaque(false);

        txtName = createInput("Nhập họ và tên...");
        txtPhone = createInput("Nhập số điện thoại (10 số)...");

        pForm.add(createFieldPanel("<html>Họ và tên <font color='red'>*</font>:</html>", txtName));
        pForm.add(createFieldPanel("<html>Số điện thoại <font color='red'>*</font>:</html>", txtPhone));
        add(pForm, BorderLayout.CENTER);

        JPanel pButton = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        pButton.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        pButton.setOpaque(false);

        btnSave = new JButton(isEditMode ? "Lưu thay đổi" : "Thêm mới");
        btnSave.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnSave.setPreferredSize(new Dimension(140, 40));
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnCancel = new JButton("Hủy bỏ");
        btnCancel.setBackground(Color.decode("#ff3131"));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnCancel.setPreferredSize(new Dimension(100, 40));
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pButton.add(btnSave);
        pButton.add(btnCancel);
        add(pButton, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> saveCustomer());
        txtName.addActionListener(e -> saveCustomer());
        txtPhone.addActionListener(e -> saveCustomer());
    }

    private JPanel createFieldPanel(String labelText, JTextField textField) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        p.add(label, BorderLayout.NORTH);

        JPanel pWrapper = new JPanel(new BorderLayout());
        pWrapper.setOpaque(false);
        pWrapper.add(textField, BorderLayout.NORTH);

        p.add(pWrapper, BorderLayout.CENTER);
        return p;
    }

    private JTextField createInput(String placeholder) {
        JTextField txt = new JTextField();
        txt.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));
        txt.setPreferredSize(new Dimension(0, 38));
        txt.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        txt.putClientProperty(FlatClientProperties.STYLE, "arc: 5; margin: 4, 10, 4, 10;");
        return txt;
    }

    private void fillData() {
        txtName.setText(customer.getCustomerName());
        txtPhone.setText(customer.getCustomerPhone());
    }

    private void saveCustomer() {

        String newName = txtName.getText().trim();
        String newPhone = txtPhone.getText().trim();

        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ và tên không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtName.requestFocus();
            return;
        }

        if (!newName.matches("^(?=.*\\p{L})[\\p{L}\\s.'\\u2019\\u2018\\u0060\\u02BB\\u2013-]+$")) {
            JOptionPane.showMessageDialog(this, "Họ và tên không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtName.requestFocus();
            return;
        }

        if (newPhone.isEmpty() || !newPhone.matches("^0\\d{9}$")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ (10 số, bắt đầu bằng 0)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtPhone.requestFocus();
            return;
        }


        CustomerDTO temp = new CustomerDTO();
        temp.setCustomerName(newName);
        temp.setCustomerPhone(newPhone);

        String result = "";

        if (isEditMode) {
            temp.setCustomerId(customer.getCustomerId());
            result = customerBUS.updateCustomerInfo(temp);
        } else {
            result = customerBUS.insertCustomer(temp);
        }

        JOptionPane.showMessageDialog(this, result);

        if (result.toLowerCase().contains("thành công") || result.toLowerCase().contains("công")) {
            this.success = true;
            if (parentPanel != null) {
                parentPanel.refresh();
            }
            dispose();
        }
    }
}
