package com.bookstore.gui.panel.SellingTab;

import com.bookstore.bus.MembershipRankBUS;
import com.bookstore.dto.MembershipRankDTO;
import com.bookstore.util.AppConstant;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MembershipRankDialog extends JDialog {
    private JTextField txtName;
    private JTextField txtMinPoint;
    private JTextField txtDiscount;
    private JButton btnSave, btnCancel;

    private MembershipRankBUS rankBUS = new MembershipRankBUS();
    private MembershipRankDTO rankDTO;

    public MembershipRankDialog(Frame parent, MembershipRankDTO rankDTO) {
        super(parent, rankDTO == null ? "Thêm Hạng Thành Viên" : "Sửa Hạng Thành Viên", true);
        this.rankDTO = rankDTO;
        initUI();
        setData();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setSize(400, 300);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel pnlMain = new JPanel(new GridLayout(3, 2, 10, 20));
        pnlMain.setBorder(new EmptyBorder(20, 20, 20, 20));
        pnlMain.setBackground(Color.WHITE);

        pnlMain.add(new JLabel("Tên hạng:"));
        txtName = new JTextField();
        txtName.putClientProperty(FlatClientProperties.STYLE, "arc: 5;");
        pnlMain.add(txtName);

        pnlMain.add(new JLabel("Điểm tối thiểu:"));
        txtMinPoint = new JTextField();
        txtMinPoint.putClientProperty(FlatClientProperties.STYLE, "arc: 5;");
        pnlMain.add(txtMinPoint);

        pnlMain.add(new JLabel("Giảm giá (%):"));
        txtDiscount = new JTextField();
        txtDiscount.putClientProperty(FlatClientProperties.STYLE, "arc: 5;");
        pnlMain.add(txtDiscount);

        add(pnlMain, BorderLayout.CENTER);

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlBottom.setBackground(Color.WHITE);
        pnlBottom.setBorder(new EmptyBorder(0, 0, 10, 10));

        btnCancel = new JButton("Hủy");
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.setPreferredSize(new Dimension(100, 35));

        btnSave = new JButton(rankDTO == null ? "Thêm mới" : "Lưu thay đổi");
        btnSave.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        btnSave.setForeground(Color.WHITE);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.setPreferredSize(new Dimension(120, 35));
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc: 5; borderWidth: 0;");

        pnlBottom.add(btnCancel);
        pnlBottom.add(btnSave);

        add(pnlBottom, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> saveRank());
    }

    private void setData() {
        if (rankDTO != null) {
            txtName.setText(rankDTO.getRankName());
            txtMinPoint.setText(String.valueOf(rankDTO.getMinPoint()));
            txtDiscount.setText(String.valueOf(rankDTO.getDiscountPercent()));
        }
    }

    private void saveRank() {
        String name = txtName.getText().trim();
        String minPointStr = txtMinPoint.getText().trim();
        String discountStr = txtDiscount.getText().trim();

        if (name.isEmpty() || minPointStr.isEmpty() || discountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int minPoint;
        double discount;

        try {
            minPoint = Integer.parseInt(minPointStr);
            if (minPoint < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Điểm tối thiểu phải là số nguyên dương!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            discount = Double.parseDouble(discountStr);
            if (discount < 0 || discount > 100) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Phần trăm giảm giá phải là số từ 0 đến 100!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        MembershipRankDTO dto = new MembershipRankDTO();
        dto.setRankName(name);
        dto.setMinPoint(minPoint);
        dto.setDiscountPercent(discount);

        if (rankDTO == null) {
            boolean success = rankBUS.addRank(dto);
            if (success) {
                JOptionPane.showMessageDialog(this, "Thêm hạng thành viên thành công!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại. Tên hạng có thể đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            dto.setRankId(rankDTO.getRankId());
            boolean success = rankBUS.updateRank(dto);
            if (success) {
                JOptionPane.showMessageDialog(this, "Cập nhật hạng thành viên thành công!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại. Vui lòng kiểm tra lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
