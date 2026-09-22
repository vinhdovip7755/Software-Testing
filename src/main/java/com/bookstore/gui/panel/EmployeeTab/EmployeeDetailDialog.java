package com.bookstore.gui.panel.EmployeeTab;

import com.bookstore.bus.EmployeeBUS;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.util.AppConstant;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;

public class EmployeeDetailDialog extends JDialog {
    private EmployeeDTO employee;
    private final EmployeeBUS employeeBUS = new EmployeeBUS();

    public EmployeeDetailDialog(JFrame owner, EmployeeDTO employee) {
        super(owner, "Chi Tiết Nhân Viên", true);
        this.employee = employee;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setSize(450, 600);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);

        JLabel lbHeader = new JLabel("HỒ SƠ NHÂN SỰ", SwingConstants.CENTER);
        lbHeader.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 20));
        lbHeader.setForeground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        lbHeader.setBorder(new EmptyBorder(20, 0, 15, 0));
        add(lbHeader, BorderLayout.NORTH);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String birthdayStr = (employee.getBirthday() != null) ? sdf.format(employee.getBirthday()) : "Chưa Cập nhật";
        String dayInStr = (employee.getDayIn() != null) ? sdf.format(employee.getDayIn()) : "Chưa Cập nhật";
        String statusStr = employee.getStatus() == 1 ? "Còn làm việc" : "Đã nghỉ việc";

        double totalSalary = employee.getBaseSalary() * employee.getSalaryFactor();
        String baseSalaryStr = String.format("%,.0f VNĐ", employee.getBaseSalary());
        String totalSalaryStr = String.format("%,.0f VNĐ", totalSalary);

        int billCount = employeeBUS.getBillCountByEmployee(employee.getEmployeeId());

        JPanel pContent = new JPanel(new GridLayout(10, 1, 0, 5));
        pContent.setBackground(Color.WHITE);
        pContent.setBorder(new EmptyBorder(10, 30, 20, 30));

        pContent.add(createInfoRow("Mã nhân viên (ID):", String.valueOf(employee.getEmployeeId())));
        pContent.add(createInfoRow("Họ và tên:", employee.getEmployeeName()));
        pContent.add(createInfoRow("Số điện thoại:", employee.getEmployeePhone()));
        pContent.add(createInfoRow("Ngày sinh:", birthdayStr));
        pContent.add(createInfoRow("Chức vụ:", employee.getRoleName()));
        pContent.add(createInfoRow("Trạng thái:", statusStr));
        pContent.add(createInfoRow("Ngày vào làm:", dayInStr));
        pContent.add(createInfoRow("Lương cơ bản:", baseSalaryStr));
        pContent.add(createInfoRow("Hệ số lương:", String.valueOf(employee.getSalaryFactor())));

        JPanel pTotalSalary = createInfoRow("Tổng lương thực tế:", totalSalaryStr);
        pTotalSalary.getComponent(1).setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 15));
        pTotalSalary.getComponent(1).setForeground(Color.RED);
        pContent.add(pTotalSalary);

        JPanel pExtra = new JPanel(new BorderLayout());
        pExtra.setBackground(Color.decode("#f8f9fa"));
        pExtra.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 30, 10, 30),
                BorderFactory.createLineBorder(Color.decode("#e0e0e0"))
        ));

        JLabel lbBillLabel = new JLabel("Số hóa đơn đã xử lý: ");
        lbBillLabel.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        JLabel lbBillValue = new JLabel(billCount + " đơn hàng");
        lbBillValue.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 16));
        lbBillValue.setForeground(Color.decode(AppConstant.GREEN_COLOR_CODE));

        pExtra.add(lbBillLabel, BorderLayout.WEST);
        pExtra.add(lbBillValue, BorderLayout.EAST);

        JPanel pCenter = new JPanel(new BorderLayout());
        pCenter.setBackground(Color.WHITE);
        pCenter.add(pContent, BorderLayout.CENTER);
        pCenter.add(pExtra, BorderLayout.SOUTH);

        add(pCenter, BorderLayout.CENTER);

        JPanel pBottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pBottom.setBackground(Color.WHITE);
        pBottom.setBorder(new EmptyBorder(15, 0, 20, 0));

        JButton btnClose = new JButton("Đóng hồ sơ");
        btnClose.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnClose.setPreferredSize(new Dimension(140, 40));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        pBottom.add(btnClose);
        add(pBottom, BorderLayout.SOUTH);
    }

    private JPanel createInfoRow(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel lbLabel = new JLabel(label);
        lbLabel.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        lbLabel.setForeground(Color.DARK_GRAY);

        JLabel lbValue = new JLabel(value);
        lbValue.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 15));
        lbValue.setForeground(Color.BLACK);

        panel.add(lbLabel, BorderLayout.WEST);
        panel.add(lbValue, BorderLayout.EAST);

        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#EEEEEE")));
        return panel;
    }
}
