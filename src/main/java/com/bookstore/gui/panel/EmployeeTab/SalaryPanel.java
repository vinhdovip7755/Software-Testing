package com.bookstore.gui.panel.EmployeeTab;

import com.bookstore.bus.EmployeeBUS;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.util.AppConstant;
import com.bookstore.util.Refreshable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class SalaryPanel extends JPanel implements Refreshable {
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblTotalSalary;
    private EmployeeBUS employeeBUS = new EmployeeBUS();
    private NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    public SalaryPanel() {
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        JLabel title = new JLabel("BẢNG LƯƠNG NHÂN VIÊN THEO THÁNG");
        title.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 22));
        title.setForeground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        headerPanel.add(title);
        add(headerPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Họ tên", "Chức vụ", "Lương cơ bản (VNĐ)", "Hệ số lương", "Tổng lương (VNĐ)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 15));
        table.getTableHeader().setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowVerticalLines(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(250);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(180);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(Color.decode("#F5F5F5"));
        footerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        lblTotalSalary = new JLabel("Tổng quỹ lương tháng: 0 VNĐ");
        lblTotalSalary.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 18));
        lblTotalSalary.setForeground(Color.RED);
        footerPanel.add(lblTotalSalary);
        
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<EmployeeDTO> employees = employeeBUS.getAllEmployees();
        
        double totalPayroll = 0;

        for (EmployeeDTO e : employees) {
            if (e.getStatus() == 1) { // Only active employees
                double baseSalary = e.getBaseSalary();
                double salaryFactor = e.getSalaryFactor();
                double totalSalary = baseSalary * salaryFactor;
                
                totalPayroll += totalSalary;

                Object[] row = {
                    e.getEmployeeId(),
                    e.getEmployeeName(),
                    e.getRoleName(),
                    currencyFormat.format(baseSalary),
                    String.valueOf(salaryFactor),
                    currencyFormat.format(totalSalary)
                };
                tableModel.addRow(row);
            }
        }
        
        lblTotalSalary.setText("Tổng quỹ lương tháng: " + currencyFormat.format(totalPayroll) + " VNĐ");
    }

    @Override
    public void refresh() {
        loadData();
    }
}
