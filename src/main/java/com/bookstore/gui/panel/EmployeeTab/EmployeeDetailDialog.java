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
        int createdImportCount = employeeBUS.getCreatedImportCountByEmployee(employee.getEmployeeId());
        int approvedImportCount = employeeBUS.getApprovedImportCountByEmployee(employee.getEmployeeId());

        JPanel pContent = new JPanel(new GridLayout(0, 1, 0, 5));
        pContent.setBackground(Color.WHITE);
        pContent.setBorder(new EmptyBorder(10, 30, 20, 30));

        pContent.add(createInfoRow("Mã nhân viên (ID):", String.valueOf(employee.getEmployeeId())));
        pContent.add(createInfoRow("Họ và tên:", employee.getEmployeeName()));
        pContent.add(createInfoRow("Số điện thoại:", employee.getEmployeePhone()));
        pContent.add(createInfoRow("Email:", employee.getEmail()));
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

        JPanel pExtra = new JPanel(new GridLayout(3, 1, 0, 8));
        pExtra.setBackground(Color.decode("#f8f9fa"));
        pExtra.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 30, 10, 30),
                BorderFactory.createLineBorder(Color.decode("#e0e0e0"))
        ));

        JPanel pBill = new JPanel(new BorderLayout());
        pBill.setOpaque(false);
        JLabel lbBillLabel = new JLabel("Số hóa đơn bán hàng đã tạo: ");
        lbBillLabel.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        JLabel lbBillValue = new JLabel(billCount + " hóa đơn");
        lbBillValue.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 15));
        lbBillValue.setForeground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        pBill.add(lbBillLabel, BorderLayout.WEST);
        pBill.add(lbBillValue, BorderLayout.EAST);

        JPanel pCreated = new JPanel(new BorderLayout());
        pCreated.setOpaque(false);
        JLabel lbCreatedLabel = new JLabel("Số đơn nhập hàng đã tạo: ");
        lbCreatedLabel.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        JLabel lbCreatedValue = new JLabel(createdImportCount + " đơn");
        lbCreatedValue.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 15));
        lbCreatedValue.setForeground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        pCreated.add(lbCreatedLabel, BorderLayout.WEST);
        pCreated.add(lbCreatedValue, BorderLayout.EAST);

        JPanel pApproved = new JPanel(new BorderLayout());
        pApproved.setOpaque(false);
        JLabel lbApprovedLabel = new JLabel("Số đơn nhập hàng đã duyệt: ");
        lbApprovedLabel.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        JLabel lbApprovedValue = new JLabel(approvedImportCount + " đơn");
        lbApprovedValue.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 15));
        lbApprovedValue.setForeground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        pApproved.add(lbApprovedLabel, BorderLayout.WEST);
        pApproved.add(lbApprovedValue, BorderLayout.EAST);

        pExtra.add(pBill);
        pExtra.add(pCreated);
        pExtra.add(pApproved);

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
