package com.bookstore.gui.panel.ImportTab;

import com.bookstore.bus.ImportBUS;
import com.bookstore.dto.ImportDetailDTO;
import com.bookstore.dto.ImportTicketDTO;
import com.bookstore.util.AppConstant;
import com.bookstore.util.MoneyFormatter;
import com.bookstore.util.SharedData;
import com.bookstore.util.PermissionUtil;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class ImportDetailDialog extends JDialog {
    private JTable table;
    private DefaultTableModel tableModel;
    private ImportBUS importBUS = new ImportBUS();
    private ImportTicketDTO currentTicket;
    private int roleId;

    public ImportDetailDialog(Window parent, ImportTicketDTO ticket, int roleId) {
        super(parent, "Chi Tiết Phiếu Nhập Hàng", ModalityType.APPLICATION_MODAL);
        this.currentTicket = ticket;
        this.roleId = roleId;
        setSize(850, 600);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
        loadDetails();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        mainPanel.setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new GridLayout(3, 2, 15, 10));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Thông Tin Chung"),
                new EmptyBorder(10, 15, 10, 15)
        ));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String statusStr = currentTicket.getStatus() == 1 ? "Đang chờ duyệt" : (currentTicket.getStatus() == 2 ? "Đã hoàn thành" : "Đã hủy");

        String approver = currentTicket.getApproverName();
        if (currentTicket.getStatus() == 1) {
            approver = "Chưa có (Đang chờ)";
        } else if (approver == null || approver.trim().isEmpty()) {
            approver = "Hệ thống / Admin";
        }

        topPanel.add(createLabelInfo("Mã Phiếu:", "PN" + String.format("%03d", currentTicket.getImportID())));
        topPanel.add(createLabelInfo("Ngày Tạo:", sdf.format(currentTicket.getCreatedDate())));
        topPanel.add(createLabelInfo("Nhà Cung Cấp:", currentTicket.getSupplierName()));
        topPanel.add(createLabelInfo("Người Lập:", currentTicket.getEmployeeName()));
        topPanel.add(createLabelInfo("Trạng Thái:", statusStr));
        topPanel.add(createLabelInfo("Người Duyệt:", approver));

        mainPanel.add(topPanel, BorderLayout.NORTH);

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(BorderFactory.createLineBorder(Color.decode("#EEEEEE")));
        tableContainer.putClientProperty(FlatClientProperties.STYLE, "arc: 15;");

        String[] cols = {"Mã Sách", "Sách", "Đơn Giá", "Số Lượng", "Thành Tiền"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 13));
        table.getTableHeader().setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setShowVerticalLines(false);

        table.setSelectionBackground(Color.decode("#d4ffee"));
        table.setSelectionForeground(Color.BLACK);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        table.getColumnModel().getColumn(1).setPreferredWidth(250);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(tableContainer, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        JLabel lbTotal = new JLabel("Tổng Tiền: " + MoneyFormatter.toVND(currentTicket.getTotalPrice()));
        lbTotal.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 20));
        lbTotal.setForeground(Color.decode("#D32F2F"));
        bottomPanel.add(lbTotal, BorderLayout.WEST);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actionPanel.setBackground(Color.WHITE);

        if (currentTicket.getStatus() == 1) {
            if (PermissionUtil.hasActionPermission("MANAGE_IMPORT_TICKET")) {
                JButton btnCancel = new JButton("Hủy Phiếu");
                btnCancel.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
                btnCancel.setForeground(Color.WHITE);
                btnCancel.setBackground(Color.decode("#D32F2F"));
                btnCancel.putClientProperty(FlatClientProperties.STYLE, "arc: 10; borderWidth: 0;");
                actionPanel.add(btnCancel);

                JButton btnApprove = new JButton("Duyệt Nhập Kho");
                btnApprove.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
                btnApprove.setForeground(Color.WHITE);
                btnApprove.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
                btnApprove.putClientProperty(FlatClientProperties.STYLE, "arc: 10; borderWidth: 0;");
                actionPanel.add(btnApprove);

                btnApprove.addActionListener(e -> {
                    if (JOptionPane.showConfirmDialog(this, "Xác nhận DUYỆT phiếu này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        int approverId = 1;
                        if (SharedData.currentUser != null) {
                            approverId = SharedData.currentUser.getEmployeeId();
                        }
                        if (importBUS.approveImport(currentTicket.getImportID(), approverId)) {
                            JOptionPane.showMessageDialog(this, "Duyệt thành công!");
                            this.dispose();
                        }
                    }
                });

                btnCancel.addActionListener(e -> {
                    if (JOptionPane.showConfirmDialog(this, "Bạn muốn HỦY phiếu nhập này?", "Cảnh báo", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        int approverId = 1;
                        if (SharedData.currentUser != null) {
                            approverId = SharedData.currentUser.getEmployeeId();
                        }
                        if (importBUS.cancelImport(currentTicket.getImportID(), approverId)) {
                            JOptionPane.showMessageDialog(this, "Đã hủy phiếu!");
                            this.dispose();
                        }
                    }
                });
            }
        }
        bottomPanel.add(actionPanel, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private JPanel createLabelInfo(String title, String value) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)); p.setBackground(Color.WHITE);
        JLabel lblTitle = new JLabel(title); lblTitle.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14)); lblTitle.setForeground(Color.GRAY);
        JLabel lblValue = new JLabel(value); lblValue.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 15)); lblValue.setForeground(Color.BLACK);
        p.add(lblTitle); p.add(lblValue);
        return p;
    }

    private void loadDetails() {
        List<ImportDetailDTO> details = importBUS.getDetailsByImportId(currentTicket.getImportID());
        for (ImportDetailDTO d : details) {
            double lineTotal = d.getQuantity() * d.getPrice();
            tableModel.insertRow(0, new Object[]{
                    d.getBookID(),
                    d.getBookName(),
                    MoneyFormatter.toVND(d.getPrice()),
                    d.getQuantity(),
                    MoneyFormatter.toVND(lineTotal)
            });
        }
    }
}