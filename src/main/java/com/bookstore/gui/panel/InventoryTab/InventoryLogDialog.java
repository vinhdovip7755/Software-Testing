package com.bookstore.gui.panel.InventoryTab;

import com.bookstore.bus.InventoryLogBUS;
import com.bookstore.dto.InventoryLogDTO;
import com.bookstore.util.AppConstant;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class InventoryLogDialog extends JDialog {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch, txtDateFrom, txtDateTo;
    private JComboBox<String> cboAction;
    private JButton btnFilter, btnReset;

    private InventoryLogBUS logBUS = new InventoryLogBUS();
    private List<InventoryLogDTO> currentList = new ArrayList<>();

    public InventoryLogDialog(Window parent) {
        super(parent, "Biến Động Tồn Kho (Thẻ Kho)", ModalityType.APPLICATION_MODAL);
        setSize(950, 650);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel pHeader = new JPanel(new BorderLayout());
        pHeader.setBackground(Color.WHITE);
        pHeader.setBorder(new EmptyBorder(15, 20, 10, 20));
        JLabel lbTitle = new JLabel("LỊCH SỬ BIẾN ĐỘNG TỒN KHO");
        lbTitle.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 22));
        lbTitle.setForeground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        pHeader.add(lbTitle, BorderLayout.WEST);

        JPanel pFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pFilter.setBackground(Color.WHITE);
        pFilter.setBorder(new EmptyBorder(0, 10, 10, 10));

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(200, 40));
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm sách, mã phiếu...");
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSVGIcon("icon/search_icon.svg").derive(18, 18));

        txtDateFrom = new JTextField(); txtDateFrom.setPreferredSize(new Dimension(130, 40));
        txtDateFrom.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Từ ngày (dd/mm/yyyy)");
        txtDateTo = new JTextField(); txtDateTo.setPreferredSize(new Dimension(130, 40));
        txtDateTo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Đến ngày");

        cboAction = new JComboBox<>(new String[]{"Tất cả thao tác", "Nhập hàng", "Bán hàng", "Hủy/Trả hàng"});
        cboAction.setPreferredSize(new Dimension(150, 40));

        btnFilter = new JButton("Lọc Lịch Sử");
        btnFilter.setPreferredSize(new Dimension(120, 40));
        btnFilter.setBackground(Color.decode("#1976D2")); btnFilter.setForeground(Color.WHITE);
        btnFilter.putClientProperty(FlatClientProperties.STYLE, "arc: 10; borderWidth: 0;");

        btnReset = new JButton("Làm mới");
        btnReset.setPreferredSize(new Dimension(100, 40));
        btnReset.setBackground(Color.decode("#9E9E9E")); btnReset.setForeground(Color.WHITE);
        btnReset.putClientProperty(FlatClientProperties.STYLE, "arc: 10; borderWidth: 0;");

        pFilter.add(txtSearch); pFilter.add(txtDateFrom); pFilter.add(txtDateTo);
        pFilter.add(cboAction); pFilter.add(btnFilter); pFilter.add(btnReset);

        String[] columns = {"THỜI GIAN", "TÊN SÁCH", "THAO TÁC", "BIẾN ĐỘNG", "TỒN KHO CÒN", "MÃ THAM CHIẾU"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.getTableHeader().setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 12));
        table.getTableHeader().setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getColumnModel().getColumn(3).setCellRenderer(new ChangeQtyRenderer());
        table.getColumnModel().getColumn(1).setPreferredWidth(250);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Color.decode("#EEEEEE")));
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel pCenter = new JPanel(new BorderLayout());
        pCenter.setBorder(new EmptyBorder(0, 20, 20, 20));
        pCenter.setBackground(Color.WHITE);
        pCenter.add(pFilter, BorderLayout.NORTH);
        pCenter.add(scroll, BorderLayout.CENTER);

        add(pHeader, BorderLayout.NORTH);
        add(pCenter, BorderLayout.CENTER);

        btnFilter.addActionListener(e -> filterData());
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            txtDateFrom.setText("");
            txtDateTo.setText("");
            cboAction.setSelectedIndex(0);
            filterData();
        });
    }

    private void loadData() {
        currentList = logBUS.getAll();
        if (currentList == null) {
            currentList = new ArrayList<>();
        }
        filterData();
    }

    private void filterData() {
        System.out.println("Tổng số dòng log lấy từ DB: " + (currentList != null ? currentList.size() : 0));

        tableModel.setRowCount(0);

        String keyword = txtSearch.getText().trim().toLowerCase();
        int actionIdx = cboAction.getSelectedIndex();
        String dateFromStr = txtDateFrom.getText().trim();
        String dateToStr = txtDateTo.getText().trim();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat displaySdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        sdf.setLenient(false);

        java.util.Date fromDate = null;
        java.util.Date toDate = null;

        try {
            if (!dateFromStr.isEmpty()) {
                fromDate = sdf.parse(dateFromStr);
                System.out.println("Parse Từ ngày thành công: " + fromDate);
            }
            if (!dateToStr.isEmpty()) {
                toDate = sdf.parse(dateToStr);
                toDate.setTime(toDate.getTime() + (24 * 60 * 60 * 1000) - 1);
                System.out.println("Parse Đến ngày thành công: " + toDate);
            }
        } catch (Exception e) {
            System.out.println("LỖI PARSE NGÀY: Bạn đã nhập sửai định dạng!");
            txtDateFrom.setText("");
            txtDateTo.setText("");
        }

        if (currentList == null) return;

        for (InventoryLogDTO log : currentList) {

            String bName = log.getBookName() == null ? "" : log.getBookName().toLowerCase();
            String rId = String.valueOf(log.getReferenceId());
            boolean matchKey = keyword.isEmpty() || bName.contains(keyword) || rId.contains(keyword);

            boolean matchAction = true;
            String logAction = log.getAction() == null ? "" : log.getAction().toLowerCase();
            if (actionIdx == 1) matchAction = logAction.contains("nhập");
            else if (actionIdx == 2) matchAction = logAction.contains("bán");
            else if (actionIdx == 3) matchAction = logAction.contains("hủy");

            boolean matchDate = true;
            if (log.getCreatedDate() != null) {
                if (fromDate != null && log.getCreatedDate().before(fromDate)) matchDate = false;
                if (toDate != null && log.getCreatedDate().after(toDate)) matchDate = false;
            }

            if (matchKey && matchAction && matchDate) {
                tableModel.addRow(new Object[]{
                        log.getCreatedDate() == null ? "-" : displaySdf.format(log.getCreatedDate()),
                        log.getBookName() == null ? "-" : log.getBookName(),
                        log.getAction() == null ? "-" : log.getAction(),
                        log.getChangeQuantity(),
                        log.getRemainQuantity(),
                        log.getReferenceId() == 0 ? "-" : log.getReferenceId()
                });
            }
        }
    }

    class ChangeQtyRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value != null) {
                try {
                    int qty = Integer.parseInt(value.toString());
                    label.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    if (qty > 0) {
                        label.setText("+" + qty);
                        label.setForeground(Color.decode("#2E7D32"));
                    } else {
                        label.setText(String.valueOf(qty));
                        label.setForeground(Color.decode("#D32F2F"));
                    }
                } catch (NumberFormatException e) {
                    label.setText("0");
                }
            }
            return label;
        }
    }
}
