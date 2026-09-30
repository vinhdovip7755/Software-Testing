package com.bookstore.gui.panel.ImportTab;

import com.bookstore.util.AppConstant;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class ImportQuantityDiscountDialog extends JDialog {
    private JTextField txtQty;
    private JTextField txtDiscount;
    private JButton btnOk;
    private JButton btnCancel;
    private boolean confirmed = false;
    private int quantity = 0;
    private double discount = 0;

    public ImportQuantityDiscountDialog(Window owner, String title, int initialQty, double initialDiscount) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        initUI(initialQty, initialDiscount);
    }

    private void initUI(int initialQty, double initialDiscount) {
        setLayout(new BorderLayout(15, 15));
        setSize(440, 270);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);

        JLabel lbTitle = new JLabel(getTitle(), SwingConstants.CENTER);
        lbTitle.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 18));
        lbTitle.setForeground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        lbTitle.setBorder(new EmptyBorder(18, 0, 5, 0));
        add(lbTitle, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 12, 15));
        formPanel.setBorder(new EmptyBorder(10, 30, 10, 30));
        formPanel.setOpaque(false);

        JLabel lbQty = new JLabel("<html>Số lượng nhập <font color='red'>*</font>:</html>");
        lbQty.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        txtQty = new JTextField(initialQty > 0 ? String.valueOf(initialQty) : "");
        txtQty.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập số lượng > 0...");
        txtQty.putClientProperty(FlatClientProperties.STYLE, "arc: 5; margin: 4, 10, 4, 10;");

        JLabel lbDiscount = new JLabel("<html>Chiết khấu NXB (%) <font color='red'>*</font>:</html>");
        lbDiscount.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        String discStr = "0";
        if (initialDiscount > 0) {
            discStr = (initialDiscount == (long) initialDiscount) 
                    ? String.format("%d", (long) initialDiscount) 
                    : String.valueOf(initialDiscount);
        }
        txtDiscount = new JTextField(discStr);
        txtDiscount.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Từ 0 đến 100%...");
        txtDiscount.putClientProperty(FlatClientProperties.STYLE, "arc: 5; margin: 4, 10, 4, 10;");

        formPanel.add(lbQty);
        formPanel.add(txtQty);
        formPanel.add(lbDiscount);
        formPanel.add(txtDiscount);
        add(formPanel, BorderLayout.CENTER);

        JPanel pBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        pBottom.setBackground(Color.WHITE);

        btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.setPreferredSize(new Dimension(100, 35));
        btnCancel.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        btnOk = new JButton("Xác nhận");
        btnOk.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnOk.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        btnOk.setForeground(Color.WHITE);
        btnOk.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOk.setPreferredSize(new Dimension(110, 35));
        btnOk.putClientProperty(FlatClientProperties.STYLE, "hoverBackground: #00A364;");
        btnOk.addActionListener(e -> handleConfirm());

        pBottom.add(btnCancel);
        pBottom.add(btnOk);
        add(pBottom, BorderLayout.SOUTH);

        // Ràng buộc & sự kiện focusLost cho txtQty
        txtQty.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (e.isTemporary()) return;
                Component opposite = e.getOppositeComponent();
                if (opposite == btnCancel) return;

                String val = txtQty.getText().trim();
                if (val.isEmpty()) {
                    txtQty.putClientProperty(FlatClientProperties.OUTLINE, "error");
                    JOptionPane.showMessageDialog(ImportQuantityDiscountDialog.this,
                            "Số lượng nhập không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    txtQty.requestFocusInWindow();
                } else {
                    try {
                        int q = Integer.parseInt(val);
                        if (q <= 0) {
                            txtQty.putClientProperty(FlatClientProperties.OUTLINE, "error");
                            JOptionPane.showMessageDialog(ImportQuantityDiscountDialog.this,
                                    "Số lượng nhập phải là số nguyên dương lớn hơn 0!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                            txtQty.requestFocusInWindow();
                        } else {
                            txtQty.putClientProperty(FlatClientProperties.OUTLINE, null);
                        }
                    } catch (NumberFormatException ex) {
                        txtQty.putClientProperty(FlatClientProperties.OUTLINE, "error");
                        JOptionPane.showMessageDialog(ImportQuantityDiscountDialog.this,
                                "Số lượng nhập không hợp lệ! Vui lòng chỉ nhập số nguyên.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                        txtQty.requestFocusInWindow();
                    }
                }
            }
        });

        // Ràng buộc & sự kiện focusLost cho txtDiscount
        txtDiscount.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (e.isTemporary()) return;
                Component opposite = e.getOppositeComponent();
                if (opposite == btnCancel) return;

                String val = txtDiscount.getText().trim();
                if (val.isEmpty()) {
                    txtDiscount.putClientProperty(FlatClientProperties.OUTLINE, "error");
                    JOptionPane.showMessageDialog(ImportQuantityDiscountDialog.this,
                            "Chiết khấu NXB không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    txtDiscount.requestFocusInWindow();
                } else {
                    try {
                        double d = Double.parseDouble(val);
                        if (d < 0 || d > 100) {
                            txtDiscount.putClientProperty(FlatClientProperties.OUTLINE, "error");
                            JOptionPane.showMessageDialog(ImportQuantityDiscountDialog.this,
                                    "Chiết khấu NXB phải nằm trong khoảng từ 0% đến 100%!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                            txtDiscount.requestFocusInWindow();
                        } else {
                            txtDiscount.putClientProperty(FlatClientProperties.OUTLINE, null);
                        }
                    } catch (NumberFormatException ex) {
                        txtDiscount.putClientProperty(FlatClientProperties.OUTLINE, "error");
                        JOptionPane.showMessageDialog(ImportQuantityDiscountDialog.this,
                                "Chiết khấu không hợp lệ! Vui lòng nhập số hợp lệ.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                        txtDiscount.requestFocusInWindow();
                    }
                }
            }
        });
    }

    private void handleConfirm() {
        String qtyStr = txtQty.getText().trim();
        if (qtyStr.isEmpty()) {
            txtQty.putClientProperty(FlatClientProperties.OUTLINE, "error");
            JOptionPane.showMessageDialog(this, "Số lượng nhập không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtQty.requestFocus();
            return;
        }

        try {
            quantity = Integer.parseInt(qtyStr);
            if (quantity <= 0) {
                txtQty.putClientProperty(FlatClientProperties.OUTLINE, "error");
                JOptionPane.showMessageDialog(this, "Số lượng nhập phải là số nguyên dương lớn hơn 0!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                txtQty.requestFocus();
                return;
            }
        } catch (NumberFormatException ex) {
            txtQty.putClientProperty(FlatClientProperties.OUTLINE, "error");
            JOptionPane.showMessageDialog(this, "Số lượng nhập không hợp lệ! Vui lòng chỉ nhập số nguyên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtQty.requestFocus();
            return;
        }

        String discountStr = txtDiscount.getText().trim();
        if (discountStr.isEmpty()) {
            txtDiscount.putClientProperty(FlatClientProperties.OUTLINE, "error");
            JOptionPane.showMessageDialog(this, "Chiết khấu NXB không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtDiscount.requestFocus();
            return;
        }

        try {
            discount = Double.parseDouble(discountStr);
            if (discount < 0 || discount > 100) {
                txtDiscount.putClientProperty(FlatClientProperties.OUTLINE, "error");
                JOptionPane.showMessageDialog(this, "Chiết khấu NXB phải nằm trong khoảng từ 0% đến 100%!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                txtDiscount.requestFocus();
                return;
            }
        } catch (NumberFormatException ex) {
            txtDiscount.putClientProperty(FlatClientProperties.OUTLINE, "error");
            JOptionPane.showMessageDialog(this, "Chiết khấu không hợp lệ! Vui lòng nhập số từ 0 đến 100.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtDiscount.requestFocus();
            return;
        }

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getDiscount() {
        return discount;
    }
}
