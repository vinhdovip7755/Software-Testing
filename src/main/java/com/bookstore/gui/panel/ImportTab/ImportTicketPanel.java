package com.bookstore.gui.panel.ImportTab;

import com.bookstore.bus.ImportBUS;
import com.bookstore.dto.ImportTicketDTO;
import com.bookstore.util.AppConstant;
import com.bookstore.util.MoneyFormatter;
import com.bookstore.util.Refreshable;
import com.bookstore.util.SharedData;
import com.bookstore.util.PermissionUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ImportTicketPanel extends JPanel implements Refreshable {
    private ImportBUS importBUS = new ImportBUS();
    private JTable table;
    private DefaultTableModel tableModel;
    private List<ImportTicketDTO> currentList = new ArrayList<>();

    private JTextField txtSearch, txtDateFrom, txtDateTo;
    private JComboBox<String> cboStatusFilter;
    private JButton btnResetFilter;
    private JCheckBox chkSelectAll;

    private JLabel lbTotalCount, lbCompletedCount, lbPendingCount, lbCanceledCount;

    public ImportTicketPanel() {
        initUI();
        loadData();
    }

    @Override
    public void refresh() { loadData(); }

    private void initUI() {
        setLayout(new BorderLayout(0, 20));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel topPanel = new JPanel(new BorderLayout(0, 15));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(createHeader(), BorderLayout.NORTH);

        JPanel midTop = new JPanel(new BorderLayout());
        midTop.setBackground(Color.WHITE);
        midTop.add(createFilterPanel(), BorderLayout.NORTH);
        midTop.add(createStatsPanel(), BorderLayout.CENTER);

        topPanel.add(midTop, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createBottomActionPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setBackground(Color.WHITE);
        JLabel lbTitle = new JLabel("QUẢN LÝ PHIẾU NHẬP");
        lbTitle.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 28));
        lbTitle.setForeground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        JLabel lbSubtitle = new JLabel("Danh sách các phiếu nhập hàng từ nhà cung cấp");
        lbSubtitle.setForeground(Color.GRAY);
        titlePanel.add(lbTitle); titlePanel.add(lbSubtitle);

        panel.add(titlePanel, BorderLayout.WEST);
        return panel;
    }

    private JPanel createFilterPanel() {
        JPanel pFilter = new JPanel(new GridLayout(1, 4, 15, 0));
        pFilter.setBackground(Color.WHITE);

        txtSearch = new JTextField();
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm Mã PN, Tên NCC, Người Lập...");
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSVGIcon("icon/search_icon.svg").derive(20,20));

        cboStatusFilter = new JComboBox<>(new String[]{"Tất cả trạng thái", "Đang chờ duyệt", "Đã hoàn thành", "Đã hủy"});

        JPanel pDate = new JPanel(new GridLayout(1, 2, 5, 0)); pDate.setBackground(Color.WHITE);
        txtDateFrom = new JTextField(); txtDateFrom.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Từ ngày (dd/mm/yyyy)");
        txtDateTo = new JTextField(); txtDateTo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Đến ngày");
        pDate.add(txtDateFrom); pDate.add(txtDateTo);

        btnResetFilter = new JButton("Làm mới bộ lọc");
        btnResetFilter.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        btnResetFilter.setForeground(Color.WHITE);
        btnResetFilter.putClientProperty(FlatClientProperties.STYLE, "arc: 10; hoverBackground: #00A364;");
        btnResetFilter.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pFilter.add(txtSearch);
        pFilter.add(cboStatusFilter);
        pFilter.add(pDate);
        pFilter.add(btnResetFilter);

        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterData(); }
            public void removeUpdate(DocumentEvent e) { filterData(); }
            public void changedUpdate(DocumentEvent e) { filterData(); }
        };
        txtSearch.getDocument().addDocumentListener(dl);
        txtDateFrom.getDocument().addDocumentListener(dl);
        txtDateTo.getDocument().addDocumentListener(dl);
        cboStatusFilter.addActionListener(e -> filterData());

        btnResetFilter.addActionListener(e -> {
            txtSearch.setText(""); txtDateFrom.setText(""); txtDateTo.setText("");
            cboStatusFilter.setSelectedIndex(0);
        });

        return pFilter;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 0, 0, 0));

        lbTotalCount = new JLabel("0");
        lbCompletedCount = new JLabel("0");
        lbPendingCount = new JLabel("0");
        lbCanceledCount = new JLabel("0");

        panel.add(createStatCard("Tổng phiếu nhập", lbTotalCount, "#F5F5F5", "#424242", "bill_icon.svg"));
        panel.add(createStatCard("Đã hoàn thành", lbCompletedCount, "#E8F5E9", "#2E7D32", "complete_icon.svg"));
        panel.add(createStatCard("Đang chờ", lbPendingCount, "#FFF3E0", "#EF6C00", "pending_icon.svg"));
        panel.add(createStatCard("Đã hủy", lbCanceledCount, "#FFEBEE", "#C62828", "cancel_icon.svg"));

        return panel;
    }

    private JPanel createStatCard(String title, JLabel lbValue, String bgIconColor, String iconColor, String iconName) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#EEEEEE"), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 20;");

        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        leftPanel.setBackground(Color.WHITE);

        JLabel lbTitle = new JLabel(title);
        lbTitle.setForeground(Color.GRAY);
        lbTitle.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));

        lbValue.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 28));
        lbValue.setForeground(Color.decode(iconColor));

        leftPanel.add(lbTitle);
        leftPanel.add(lbValue);

        JLabel lbIcon = new JLabel();
        lbIcon.setPreferredSize(new Dimension(50, 50));
        lbIcon.setOpaque(true);
        lbIcon.setBackground(Color.decode(bgIconColor));
        lbIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lbIcon.putClientProperty(FlatClientProperties.STYLE, "arc: 50;");
        try {
            FlatSVGIcon svgIcon = new FlatSVGIcon("icon/" + iconName);
            FlatSVGIcon.ColorFilter filter = new FlatSVGIcon.ColorFilter(oldColor -> Color.decode(iconColor));
            svgIcon.setColorFilter(filter);
            lbIcon.setIcon(svgIcon.derive(24, 24));
        } catch (Exception e) {}

        card.add(leftPanel, BorderLayout.CENTER);
        card.add(lbIcon, BorderLayout.EAST);

        return card;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(Color.decode("#EEEEEE")));

        String[] columns = {"", "MÃ PHIẾU", "NHÀ CUNG CẤP", "NGÀY NHẬP", "NGƯỜI LẬP", "TỔNG TIỀN", "TRẠNG THÁI", "THAO TÁC"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 0;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class;
                return super.getColumnClass(columnIndex);
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(50);
        table.getTableHeader().setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 12));
        table.getTableHeader().setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(Color.decode("#d4ffee"));
        table.setSelectionForeground(Color.BLACK);

        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(0).setMinWidth(40);

        table.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(new ActionRenderer());

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (table.columnAtPoint(e.getPoint()) == 7) {
                    String idStr = table.getValueAt(table.rowAtPoint(e.getPoint()), 1).toString().replace("PN", "");
                    int id = Integer.parseInt(idStr);
                    ImportTicketDTO selected = currentList.stream().filter(t -> t.getImportID() == id).findFirst().orElse(null);
                    if(selected != null) {
                        int currentRoleId = 1;
                        if (SharedData.currentUser != null) {
                            currentRoleId = SharedData.currentUser.getRoleId();
                        }
                        new ImportDetailDialog(SwingUtilities.getWindowAncestor(ImportTicketPanel.this), selected, currentRoleId).setVisible(true);
                        refresh();
                    }
                }
            }
        });
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBottomActionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel pLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pLeft.setBackground(Color.WHITE);

        chkSelectAll = new JCheckBox("Chọn tất cả");
        chkSelectAll.setBackground(Color.WHITE);
        chkSelectAll.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 15));
        chkSelectAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chkSelectAll.addActionListener(e -> {
            boolean isSelected = chkSelectAll.isSelected();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                int status = (int) tableModel.getValueAt(i, 6);
                if (status == 1) {
                    tableModel.setValueAt(isSelected, i, 0);
                } else {
                    tableModel.setValueAt(false, i, 0);
                }
            }
        });
        pLeft.add(chkSelectAll);

        JPanel pRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pRight.setBackground(Color.WHITE);

        JButton btnCancelSelected = new JButton("Hủy các phiếu đã chọn");
        btnCancelSelected.setBackground(Color.decode("#D32F2F"));
        btnCancelSelected.setForeground(Color.WHITE);
        btnCancelSelected.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnCancelSelected.putClientProperty(FlatClientProperties.STYLE, "arc: 10; borderWidth: 0;");
        btnCancelSelected.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnApproveSelected = new JButton("Duyệt các phiếu đã chọn");
        btnApproveSelected.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        btnApproveSelected.setForeground(Color.WHITE);
        btnApproveSelected.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnApproveSelected.putClientProperty(FlatClientProperties.STYLE, "arc: 10; borderWidth: 0;");
        btnApproveSelected.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (PermissionUtil.hasActionPermission("MANAGE_IMPORT_TICKET")) {
            pRight.add(btnCancelSelected);
            pRight.add(btnApproveSelected);
        }

        panel.add(pLeft, BorderLayout.WEST);
        panel.add(pRight, BorderLayout.EAST);

        btnApproveSelected.addActionListener(e -> {
            processMultipleTickets(true);
        });

        btnCancelSelected.addActionListener(e -> {
            processMultipleTickets(false);
        });

        return panel;
    }

    private void processMultipleTickets(boolean isApproveAction) {
        List<Integer> validIds = new ArrayList<>();
        int totalSelected = 0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            boolean isChecked = (boolean) tableModel.getValueAt(i, 0);
            if (isChecked) {
                totalSelected++;
                int status = (int) tableModel.getValueAt(i, 6);
                if (status == 1) {
                    String idStr = tableModel.getValueAt(i, 1).toString().replace("PN", "");
                    validIds.add(Integer.parseInt(idStr));
                }
            }
        }

        if (totalSelected == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng tick chọn ít nhất 1 phiếu!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (validIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Các phiếu bạn chọn đều đã hoàn thành hoặc đã bị hủy. Chỉ có thể xử lý phiếu Đang chờ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String actionName = isApproveAction ? "DUYỆT" : "HỦY";
        String msg = "Bạn đã chọn " + totalSelected + " phiếu, nhưng chỉ có " + validIds.size() + " phiếu hợp lệ (Đang chờ).\n\nBạn có chắc chắn muốn " + actionName + " " + validIds.size() + " phiếu này?";

        if (JOptionPane.showConfirmDialog(this, msg, "Xácầnhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            int approverId = SharedData.currentUser != null ? SharedData.currentUser.getEmployeeId() : 1;
            int successCount = 0;

            for (int id : validIds) {
                if (isApproveAction) {
                    if (importBUS.approveImport(id, approverId)) successCount++;
                } else {
                    if (importBUS.cancelImport(id, approverId)) successCount++;
                }
            }
            JOptionPane.showMessageDialog(this, "Đã " + actionName.toLowerCase() + " thành công " + successCount + "/" + validIds.size() + " phiếu hợp lệ.");
            refresh();
        }
    }

    private void loadData() {
        currentList = importBUS.getAllImports();
        filterData();
    }

    private void filterData() {
        if (chkSelectAll != null) chkSelectAll.setSelected(false);
        tableModel.setRowCount(0);

        String keyword = txtSearch.getText().trim().toLowerCase();
        int statusIdx = cboStatusFilter.getSelectedIndex();
        int targetStatus = (statusIdx == 1) ? 1 : (statusIdx == 2) ? 2 : (statusIdx == 3) ? 0 : -1;

        String dateFromStr = txtDateFrom.getText().trim();
        String dateToStr = txtDateTo.getText().trim();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);

        java.util.Date fromDate = null;
        java.util.Date toDate = null;

        try {
            if (!dateFromStr.isEmpty()) fromDate = sdf.parse(dateFromStr);
            if (!dateToStr.isEmpty()) {
                toDate = sdf.parse(dateToStr);
                toDate.setTime(toDate.getTime() + (24 * 60 * 60 * 1000) - 1);
            }
        } catch (Exception ignored) {}

        int total = 0, completed = 0, pending = 0, canceled = 0;

        for (ImportTicketDTO dto : currentList) {
            String pnStr = "PN" + String.format("%03d", dto.getImportID());
            String dateStr = sdf.format(dto.getCreatedDate());

            boolean matchKey = keyword.isEmpty() || pnStr.toLowerCase().contains(keyword)
                    || dto.getSupplierName().toLowerCase().contains(keyword)
                    || dto.getEmployeeName().toLowerCase().contains(keyword);
            boolean matchStatus = (targetStatus == -1) || (dto.getStatus() == targetStatus);

            boolean matchDate = true;
            java.util.Date createdDate = dto.getCreatedDate();

            if (fromDate != null && createdDate.before(fromDate)) matchDate = false;
            if (toDate != null && createdDate.after(toDate)) matchDate = false;

            if (matchKey && matchStatus && matchDate) {
                tableModel.addRow(new Object[]{
                        false,
                        pnStr,
                        dto.getSupplierName(),
                        dateStr,
                        dto.getEmployeeName(),
                        MoneyFormatter.toVND(dto.getTotalPrice()),
                        dto.getStatus(),
                        "Chi tiết"
                });
            }

            total++;
            if (dto.getStatus() == 2) completed++;
            else if (dto.getStatus() == 1) pending++;
            else canceled++;
        }

        if (lbTotalCount != null) {
            lbTotalCount.setText(String.valueOf(total));
            lbCompletedCount.setText(String.valueOf(completed));
            lbPendingCount.setText(String.valueOf(pending));
            lbCanceledCount.setText(String.valueOf(canceled));
        }
    }

    class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = new JLabel(); label.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 12)); label.setOpaque(true); label.setHorizontalAlignment(JLabel.CENTER);
            int status = Integer.parseInt(value.toString());
            if (status == 2) { label.setText("Hoàn thành"); label.setBackground(Color.decode("#E8F5E9")); label.setForeground(Color.decode("#2E7D32")); }
            else if (status == 1) { label.setText("Đang chờ"); label.setBackground(Color.decode("#FFF3E0")); label.setForeground(Color.decode("#EF6C00")); }
            else { label.setText("Đã hủy"); label.setBackground(Color.decode("#FFEBEE")); label.setForeground(Color.decode("#C62828")); }

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            label.setBorder(new EmptyBorder(5, 10, 5, 10)); panel.add(label); return panel;
        }
    }

    class ActionRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = new JLabel("◉ " + value.toString()); label.setForeground(Color.decode("#2196F3")); label.setCursor(new Cursor(Cursor.HAND_CURSOR));
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            panel.add(label); return panel;
        }
    }
}
