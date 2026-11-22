package com.bookstore.gui.panel.InventoryTab;

import com.bookstore.api.client.ApiException;
import com.bookstore.api.client.InventoryApiClient;
import com.bookstore.dto.InventoryLogDTO;
import com.bookstore.util.AppConstant;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InventoryLogDialog extends JDialog {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JDateChooser dchDateFrom, dchDateTo;
    private JComboBox<String> cboAction;
    private JButton btnFilter, btnReset;

    private final InventoryApiClient inventoryApiClient = new InventoryApiClient();
    private List<InventoryLogDTO> currentList = new ArrayList<>();

    public InventoryLogDialog(Window parent) {
        super(parent, "Biến Động Tồn Kho (Thẻ Kho)", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1000, 650);
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

        JPanel pFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pFilter.setBackground(Color.WHITE);
        pFilter.setBorder(new EmptyBorder(0, 10, 10, 10));

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(180, 40));
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm sách, mã phiếu...");
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSVGIcon("icon/search_icon.svg").derive(18, 18));

        JLabel lblFrom = new JLabel("Từ:");
        lblFrom.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));
        dchDateFrom = new JDateChooser();
        dchDateFrom.setDateFormatString("dd/MM/yyyy");
        dchDateFrom.setMinSelectableDate(AppConstant.SYSTEM_MIN_DATE);
        dchDateFrom.setMaxSelectableDate(AppConstant.SYSTEM_MAX_DATE);
        dchDateFrom.setPreferredSize(new Dimension(130, 40));
        dchDateFrom.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 13));

        JLabel lblTo = new JLabel("Đến:");
        lblTo.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));
        dchDateTo = new JDateChooser();
        dchDateTo.setDateFormatString("dd/MM/yyyy");
        dchDateTo.setMinSelectableDate(AppConstant.SYSTEM_MIN_DATE);
        dchDateTo.setMaxSelectableDate(AppConstant.SYSTEM_MAX_DATE);
        dchDateTo.setPreferredSize(new Dimension(130, 40));
        dchDateTo.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 13));

        cboAction = new JComboBox<>(new String[]{"Tất cả thao tác", "Nhập hàng", "Bán hàng", "Hủy/Trả hàng"});
        cboAction.setPreferredSize(new Dimension(140, 40));

        btnFilter = new JButton("Lọc Lịch Sử");
        btnFilter.setPreferredSize(new Dimension(115, 40));
        btnFilter.setBackground(Color.decode("#1976D2")); btnFilter.setForeground(Color.WHITE);
        btnFilter.putClientProperty(FlatClientProperties.STYLE, "arc: 10; borderWidth: 0;");

        btnReset = new JButton("Làm mới");
        btnReset.setPreferredSize(new Dimension(95, 40));
        btnReset.setBackground(Color.decode("#9E9E9E")); btnReset.setForeground(Color.WHITE);
        btnReset.putClientProperty(FlatClientProperties.STYLE, "arc: 10; borderWidth: 0;");

        pFilter.add(txtSearch);
        pFilter.add(lblFrom);
        pFilter.add(dchDateFrom);
        pFilter.add(lblTo);
        pFilter.add(dchDateTo);
        pFilter.add(cboAction);
        pFilter.add(btnFilter);
        pFilter.add(btnReset);

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

        txtSearch.addActionListener(e -> filterData());
        btnFilter.addActionListener(e -> filterData());
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            dchDateFrom.setDate(null);
            dchDateTo.setDate(null);
            cboAction.setSelectedIndex(0);
            filterData();
        });
    }

    private void loadData() {
        try {
            currentList = inventoryApiClient.getInventoryLogs();
            filterData();
        } catch (ApiException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể tải lịch sử tồn kho",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private boolean isDateOutOfBounds(LocalDate date) {
        Date normalized = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        return AppConstant.isDateOutOfBounds(normalized);
    }

    private void filterData() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        int actionIdx = cboAction.getSelectedIndex();
        Date fromDate = dchDateFrom.getDate();
        Date toDate = dchDateTo.getDate();

        LocalDate fromLocal = fromDate != null ? toLocalDate(fromDate) : null;
        LocalDate toLocal = toDate != null ? toLocalDate(toDate) : null;

        if (fromLocal != null && toLocal != null && fromLocal.isAfter(toLocal)) {
            JOptionPane.showMessageDialog(this, "Ngày bắt đầu (Từ ngày) không được lớn hơn ngày kết thúc (Đến ngày)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ((fromLocal != null && isDateOutOfBounds(fromLocal)) || (toLocal != null && isDateOutOfBounds(toLocal))) {
            JOptionPane.showMessageDialog(this, "Khoảng ngày lọc phải nằm trong khoảng từ 01/01/2026 đến 31/12/2099!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        tableModel.setRowCount(0);

        SimpleDateFormat displaySdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

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
            if (fromLocal != null || toLocal != null) {
                if (log.getCreatedDate() == null) {
                    matchDate = false;
                } else {
                    LocalDate logDate = toLocalDate(log.getCreatedDate());
                    if (fromLocal != null && logDate.isBefore(fromLocal)) matchDate = false;
                    if (toLocal != null && logDate.isAfter(toLocal)) matchDate = false;
                }
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
