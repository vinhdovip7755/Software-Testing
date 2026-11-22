package com.bookstore.gui.panel.BillTab;
import com.bookstore.util.AppConstant;
import com.bookstore.util.MoneyFormatter;
import com.bookstore.api.client.ApiException;
import com.bookstore.api.client.BillApiClient;
import javax.swing.*;
import com.toedter.calendar.JDateChooser;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import com.bookstore.dto.BillDTO;
import com.bookstore.util.Refreshable;
import java.util.List;


public class BillPanel extends JPanel implements Refreshable {
    private JLabel lbTotalQuantity, lbRevenue;
    private JTable table;
    private DefaultTableModel model;
    private JDateChooser dchFrom;
    private JDateChooser dchTo;
    private JTextField txtSearch;
    private final BillApiClient billApiClient = new BillApiClient();

    private final Color MAIN_GREEN = Color.decode(AppConstant.GREEN_COLOR_CODE);

    public BillPanel(){
        setLayout(new BorderLayout(20,20));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20,20,20,20));

        add(createStatisticPanel(),BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);

        loadBillTable();
        applyTodayFilter();
    }

    @Override
    public void refresh(){
        if (txtSearch != null) {
            txtSearch.setText("");
        }
        Date startOfToday = toStartOfDay(LocalDate.now());
        if (dchFrom != null) {
            dchFrom.setDate(startOfToday);
        }
        if (dchTo != null) {
            dchTo.setDate(startOfToday);
        }
        loadBillTable();
        applyTodayFilter();
    }

    private static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static Date toStartOfDay(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static Date toEndOfDay(LocalDate date) {
        return Date.from(date.atTime(23, 59, 59, 999_000_000).atZone(ZoneId.systemDefault()).toInstant());
    }

    private JPanel createStatisticPanel(){
        JPanel panel = new JPanel(new GridLayout(1,2,20,0));
        panel.setBackground(Color.WHITE);

        JPanel quantityBox = new JPanel(new BorderLayout());
        quantityBox.setBackground(MAIN_GREEN);
        quantityBox.setBorder(new EmptyBorder(15,20,15,20));

        JLabel lbTitle1 = new JLabel("SỐ LƯỢNG");
        lbTitle1.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD,22));
        lbTitle1.setForeground(Color.WHITE);

        lbTotalQuantity = new JLabel("5");
        lbTotalQuantity.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 40));
        lbTotalQuantity.setForeground(Color.WHITE);

        quantityBox.add(lbTitle1,BorderLayout.NORTH);
        quantityBox.add(lbTotalQuantity, BorderLayout.CENTER);

        JPanel revenueBox = new JPanel(new BorderLayout());
        revenueBox.setBackground(MAIN_GREEN);
        revenueBox.setBorder(new EmptyBorder(15,20,15,20));

        JLabel lbTitle2 = new JLabel("DOANH THU");
        lbTitle2.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 22));
        lbTitle2.setForeground(Color.WHITE);

        lbRevenue = new JLabel(MoneyFormatter.toVND(0));
        lbRevenue.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 40));
        lbRevenue.setForeground(Color.WHITE);

        revenueBox.add(lbTitle2, BorderLayout.NORTH);
        revenueBox.add(lbRevenue, BorderLayout.CENTER);

        panel.add(quantityBox);
        panel.add(revenueBox);

        return panel;
    }

    private JPanel createCenterPanel(){
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBackground(Color.WHITE);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,15,5));
        filterPanel.setBackground(Color.WHITE);

        Date initialToday = toStartOfDay(LocalDate.now());
        JLabel lbFrom = new JLabel("Từ:");
        lbFrom.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 16));
        dchFrom = new JDateChooser(initialToday);
        dchFrom.setDateFormatString("dd/MM/yyyy");
        dchFrom.setMinSelectableDate(AppConstant.SYSTEM_MIN_DATE);
        dchFrom.setMaxSelectableDate(AppConstant.SYSTEM_MAX_DATE);
        dchFrom.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 16));
        dchFrom.setPreferredSize(new Dimension(140, 30));

        JLabel lbTo = new JLabel("Đến:");
        lbTo.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 16));
        dchTo = new JDateChooser(initialToday);
        dchTo.setDateFormatString("dd/MM/yyyy");
        dchTo.setMinSelectableDate(AppConstant.SYSTEM_MIN_DATE);
        dchTo.setMaxSelectableDate(AppConstant.SYSTEM_MAX_DATE);
        dchTo.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 16));
        dchTo.setPreferredSize(new Dimension(140, 30));

        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 16));
        txtSearch.setBorder(BorderFactory.createTitledBorder("Tìm Kiếm theo mã hóa đơn, tên khách hàng, tên nhân viên"));

        JButton btnFilter = new JButton("Lọc");
        btnFilter.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD,16));
        btnFilter.setBackground(MAIN_GREEN);
        btnFilter.setForeground(Color.white);

        JButton btnReset = new JButton("Làm mới");
        btnReset.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 16));
        btnReset.setBackground(Color.decode("#5674ff"));
        btnReset.setForeground(Color.WHITE);

        JButton btnViewDetail = new JButton("Xem chi tiết");
        btnViewDetail.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        btnViewDetail.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 16));
        btnViewDetail.setForeground(Color.WHITE);

        filterPanel.add(lbFrom);
        filterPanel.add(dchFrom);
        filterPanel.add(lbTo);
        filterPanel.add(dchTo);
        filterPanel.add(txtSearch);
        filterPanel.add(btnFilter);
        filterPanel.add(btnReset);
        filterPanel.add(btnViewDetail);

        panel.add(filterPanel, BorderLayout.NORTH);

        String[]columns = {"Mã Hóa Đơn", "Ngày Giờ Lập", "Nhân Viên Lập", "Khách Hàng", "Tổng Tiền"};
        model = new DefaultTableModel(columns, 0){
            @Override
            public boolean isCellEditable(int row, int columm){
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0 -> Integer.class;
                    case 1 -> Date.class;
                    case 4 -> Double.class;
                    default -> String.class;
                };
            }
        };
        table = new JTable(model);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for(int i = 0; i < table.getColumnCount(); i++){
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        SimpleDateFormat dateCellFormat = new SimpleDateFormat("HH:mm:ss - dd/MM/yyyy");
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setHorizontalAlignment(JLabel.CENTER);
                if (value instanceof Date d) {
                    setText(dateCellFormat.format(d));
                } else {
                    setText(value == null ? "" : value.toString());
                }
            }
        });

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setShowGrid(true);
        table.setGridColor(Color.BLACK);

        JTableHeader header = table.getTableHeader();
        header.setOpaque(true);
        table.setRowHeight(35);
        table.setShowGrid(true);
        table.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 16));
        table.getTableHeader().setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 16));
        table.getTableHeader().setBackground(MAIN_GREEN);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setResizingAllowed(false);

        table.getTableHeader().setDefaultRenderer(
                new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table, Object value,
                            boolean isSelected, boolean hasFocus,
                            int row, int column) {

                        super.getTableCellRendererComponent(
                                table, value, isSelected, hasFocus, row, column);

                        setBackground(MAIN_GREEN);
                        setForeground(Color.WHITE);
                        setHorizontalAlignment(CENTER);
                        setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 16));

                        return this;
                    }
                }
        );

        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer(){
            @Override
            protected void setValue(Object value){
                setHorizontalAlignment(JLabel.CENTER);

                if(value instanceof Number){
                    setText(MoneyFormatter.toVND(((Number)value).doubleValue()));
                }else{
                    super.setValue(value);
                }
            }
        });

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(
                table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        panel.add(scrollPane, BorderLayout.CENTER);

        btnViewDetail.addActionListener(e->{
            int selectedRow = table.getSelectedRow();

            if(selectedRow == -1){
                JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn");
                return;
            }
            int modelRow = table.convertRowIndexToModel(selectedRow);
            int billId = (int) model.getValueAt(modelRow, 0);

            BillDetailDialog dialog = new BillDetailDialog(billId);
            if (dialog.isLoadedSuccessfully()) {
                dialog.setLocationRelativeTo(this);
                dialog.setVisible(true);
            }
        });

        btnReset.addActionListener(e->{
            txtSearch.setText("");
            Date startOfToday = toStartOfDay(LocalDate.now());
            dchFrom.setDate(startOfToday);
            dchTo.setDate(startOfToday);
            loadBillTable();
            applyTodayFilter();
        });

        btnFilter.addActionListener(e -> {
            Date rawFromDate = dchFrom.getDate();
            Date rawToDate = dchTo.getDate();
            if (rawFromDate == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày bắt đầu (Từ ngày)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (rawToDate == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày kết thúc (Đến ngày)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            LocalDate fromLocal = toLocalDate(rawFromDate);
            LocalDate toLocal = toLocalDate(rawToDate);
            if (fromLocal.isAfter(toLocal)) {
                JOptionPane.showMessageDialog(this, "Ngày bắt đầu (Từ ngày) không được lớn hơn ngày kết thúc (Đến ngày)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Date fromDate = toStartOfDay(fromLocal);
            Date toDate = toEndOfDay(toLocal);
            if (AppConstant.isDateOutOfBounds(fromDate) || AppConstant.isDateOutOfBounds(toDate)) {
                JOptionPane.showMessageDialog(this, "Khoảng ngày hợp lệ phải nằm trong khoảng từ 01/01/2026 đến 31/12/2099!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String keyword = txtSearch.getText().trim().toLowerCase();

            sorter.setRowFilter(new RowFilter<>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    Object dateVal = entry.getValue(1);
                    if (!(dateVal instanceof Date billDate)) {
                        return false;
                    }

                    boolean matchDate = !billDate.before(fromDate) && !billDate.after(toDate);
                    boolean matchSearch = keyword.isEmpty()
                            || entry.getStringValue(0).toLowerCase().contains(keyword)
                            || entry.getStringValue(2).toLowerCase().contains(keyword)
                            || entry.getStringValue(3).toLowerCase().contains(keyword);

                    return matchDate && matchSearch;
                }
            });

            updateStatistics();
        });

        return panel;
    }

    private void updateStatistics(){
        int count = table.getRowCount();
        double total = 0;

        for (int i = 0; i < count; i++){
            int modelRow = table.convertRowIndexToModel(i);
            Object val = model.getValueAt(modelRow, 4);
            if (val instanceof Number number) {
                total += number.doubleValue();
            }
        }

        lbTotalQuantity.setText(String.valueOf(count));
        lbRevenue.setText(MoneyFormatter.toVND(total));
    }

    private void loadBillTable(){
        if(model == null)
            return;

        List<BillDTO> list;
        try {
            list = billApiClient.getAllBills();
        } catch (ApiException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Không thể tải hóa đơn", JOptionPane.ERROR_MESSAGE);
            return;
        }
        model.setRowCount(0);
        list.sort((b1, b2) -> {
            if (b1.getCreatedDate() != null && b2.getCreatedDate() != null) {
                int cmp = b2.getCreatedDate().compareTo(b1.getCreatedDate());
                if (cmp != 0) return cmp;
            }
            return Integer.compare(b2.getBillId(), b1.getBillId());
        });

        for (BillDTO bill : list){
            Date createdDate = bill.getCreatedDate();
            String customerName = bill.getCustomerName() == null ? "Khách lẻ" : bill.getCustomerName();
            model.addRow(new Object[]{
                    bill.getBillId(), createdDate, bill.getEmployeeName(), customerName, bill.getTotalBillPrice()
            });
        }
        updateStatistics();
    }

    @SuppressWarnings("unchecked")
    private void applyTodayFilter(){
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) table.getRowSorter();
        LocalDate today = LocalDate.now();

        sorter.setRowFilter(new RowFilter<>(){
            @Override
            public boolean include (Entry<? extends DefaultTableModel, ? extends Integer> entry){
                Object dateVal = entry.getValue(1);
                if (!(dateVal instanceof Date billDate)) {
                    return false;
                }
                return toLocalDate(billDate).equals(today);
            }
        });
        updateStatistics();
    }
}
