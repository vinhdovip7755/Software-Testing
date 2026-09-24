package com.bookstore.gui.panel.StatisticTab;

import com.bookstore.bus.CustomerBUS;
import com.bookstore.bus.CustomerStatsBUS;
import com.bookstore.dto.CustomerHistoryDTO;
import com.bookstore.dto.CustomerStatsDTO;
import com.bookstore.util.AppConstant;
import com.bookstore.util.MoneyFormatter;
import com.bookstore.util.Refreshable;
import com.formdev.flatlaf.FlatClientProperties;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CustomerStatsPanel extends JPanel implements Refreshable {
    private JDateChooser dcFrom, dcTo;
    private JButton btnFilterDate, btnViewHistory;
    private JList<CustomerStatsDTO> listTopQuantity, listTopSpent;
    private DefaultListModel<CustomerStatsDTO> modelTopQuantity, modelTopSpent;
    private JTable tblHistory;
    private DefaultTableModel modelHistory;

    private CustomerStatsBUS customerStatsBUS = new CustomerStatsBUS();

    private final String DARK_GREEN = AppConstant.GREEN_COLOR_CODE;
    private final String RED_PINK = "#ff3131";

    public CustomerStatsPanel() {
        initUI();
    }

    @Override
    public void refresh() {

    }

    public void initUI() {
        setLayout(new BorderLayout(5,5));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        addEvents();
    }

    private JPanel createTopPanel() {
        JPanel pnlTop = new JPanel();
        pnlTop.setLayout(new BoxLayout(pnlTop, BoxLayout.Y_AXIS));
        pnlTop.setOpaque(false);

        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlFilter.setOpaque(false);

        JLabel lblFrom = new JLabel("Từ:");
        lblFrom.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));
        dcFrom = new JDateChooser();
        dcFrom.setDateFormatString("dd/MM/yyyy");
        dcFrom.setPreferredSize(new Dimension(150, 35));

        JLabel lblTo = new JLabel("Đến:");
        lblTo.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));
        dcTo = new JDateChooser();
        dcTo.setDateFormatString("dd/MM/yyyy");
        dcTo.setPreferredSize(new Dimension(150, 35));

        btnFilterDate = new JButton("Lọc ngày");
        btnFilterDate.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnFilterDate.setForeground(Color.WHITE);
        btnFilterDate.setBackground(Color.decode(RED_PINK));
        btnFilterDate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFilterDate.setPreferredSize(new Dimension(120, 35));
        btnFilterDate.putClientProperty(FlatClientProperties.STYLE, "arc: 5; borderWidth: 0;");

        pnlFilter.add(lblFrom);
        pnlFilter.add(dcFrom);
        pnlFilter.add(lblTo);
        pnlFilter.add(dcTo);
        pnlFilter.add(btnFilterDate);

        JPanel pnlAction = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        pnlAction.setOpaque(false);

        btnViewHistory = new JButton("Xem lịch sử mua hàng của khách này");
        btnViewHistory.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnViewHistory.setForeground(Color.WHITE);
        btnViewHistory.setBackground(Color.decode(DARK_GREEN));
        btnViewHistory.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnViewHistory.setPreferredSize(new Dimension(300, 35));
        btnViewHistory.putClientProperty(FlatClientProperties.STYLE, "arc: 5; borderWidth: 0;");

        pnlAction.add(btnViewHistory);

        JPanel pnlTop3Container = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlTop3Container.setOpaque(false);
        pnlTop3Container.setBorder(new EmptyBorder(0, 15, 10, 15));

        modelTopQuantity = new DefaultListModel<>();
        modelTopSpent = new DefaultListModel<>();
        listTopQuantity = new JList<>(modelTopQuantity);
        listTopSpent = new JList<>(modelTopSpent);

        listTopQuantity.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof CustomerStatsDTO stat) {
                    setText("- " + stat.getCustomerName() + " - " + stat.getRankName() + " - " + stat.getTotalQuantity() + " sách");
                }
                return this;
            }
        });

        listTopSpent.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof CustomerStatsDTO stat) {
                    setText("- " + stat.getCustomerName() + " - " + stat.getRankName() + " - " + MoneyFormatter.toVND(stat.getTotalSpent()));
                }
                return this;
            }
        });

        listTopQuantity.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listTopQuantity.getSelectedIndex() != -1) {
                listTopSpent.clearSelection();
            }
        });

        listTopSpent.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listTopSpent.getSelectedIndex() != -1) {
                listTopQuantity.clearSelection();
            }
        });

        pnlTop3Container.add(createTop3Box("3 khách hàng mua số lượng nhiều nhất", listTopQuantity));
        pnlTop3Container.add(createTop3Box("3 khách hàng chi nhiều nhất", listTopSpent));

        pnlTop.add(pnlFilter);
        pnlTop.add(pnlAction);
        pnlTop.add(pnlTop3Container);

        return pnlTop;
    }

    private JPanel createTop3Box(String title, JList<CustomerStatsDTO> list) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createLineBorder(Color.decode(DARK_GREEN), 2));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setOpaque(true);
        lblTitle.setBackground(Color.decode(DARK_GREEN));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 16));
        lblTitle.setBorder(new EmptyBorder(10, 15, 10, 15));

        list.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 15));
        list.setForeground(Color.DARK_GRAY);
        list.setFixedCellHeight(35);
        list.setSelectionBackground(Color.decode("#d4ffee"));
        list.setSelectionForeground(Color.BLACK);
        list.setBorder(new EmptyBorder(5, 10, 5, 10));

        box.add(lblTitle, BorderLayout.NORTH);
        box.add(list, BorderLayout.CENTER);

        box.setPreferredSize(new Dimension(0, 160));
        return box;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 15, 0, 15));

        String[] headers = {"Mã HĐ", "Ngày giờ mua", "Tên khách", "Điện thoại", "SL mua", "Tổng hóa đơn"};
        modelHistory = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblHistory = new JTable(modelHistory);

        JTableHeader header = tblHistory.getTableHeader();
        header.setBackground(Color.decode(DARK_GREEN));
        header.setForeground(Color.WHITE);
        header.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setReorderingAllowed(false);

        tblHistory.setRowHeight(35);
        tblHistory.setShowGrid(true);
        tblHistory.setGridColor(Color.decode("#E0E0E0"));
        tblHistory.setSelectionBackground(Color.decode("#d4ffee"));
        tblHistory.setSelectionForeground(Color.BLACK);
        tblHistory.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));
        tblHistory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblHistory.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane scrollPane = new JScrollPane(tblHistory);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.decode(DARK_GREEN), 2));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void addEvents() {
        btnFilterDate.addActionListener(e -> {
            Date dFrom = dcFrom.getDate();
            Date dTo = dcTo.getDate();

            if (dFrom == null || dTo == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn đầy đủ ngày 'Từ' và 'Đến'!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (dFrom.after(dTo)) {
                JOptionPane.showMessageDialog(this, "Ngày 'Từ' không được lớn hơn ngày 'Đến'!", "Lỗi logic", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Calendar calFrom = Calendar.getInstance();
            calFrom.setTime(dFrom);
            calFrom.set(Calendar.HOUR_OF_DAY, 0);
            calFrom.set(Calendar.MINUTE, 0);
            calFrom.set(Calendar.SECOND, 0);
            Timestamp tsFrom = new Timestamp(calFrom.getTimeInMillis());

            Calendar calTo = java.util.Calendar.getInstance();
            calTo.setTime(dTo);
            calTo.set(Calendar.HOUR_OF_DAY, 23);
            calTo.set(Calendar.MINUTE, 59);
            calTo.set(Calendar.SECOND, 59);
            Timestamp tsTo = new Timestamp(calTo.getTimeInMillis());

            java.util.List<CustomerStatsDTO> topQtyList = customerStatsBUS.getTopCustomersByQuantity(tsFrom, tsTo);
            java.util.List<CustomerStatsDTO> topSpentList = customerStatsBUS.getTopCustomersBySpending(tsFrom, tsTo);

            modelTopQuantity.clear();
            for (CustomerStatsDTO dto : topQtyList) {
                modelTopQuantity.addElement(dto);
            }

            modelTopSpent.clear();
            for (CustomerStatsDTO dto : topSpentList) {
                modelTopSpent.addElement(dto);
            }

            modelHistory.setRowCount(0);

            if (topQtyList.isEmpty() && topSpentList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có giao dịch nào trong khoảng thời gian này.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnViewHistory.addActionListener(e -> {
            CustomerStatsDTO selectedCustomer = null;
            if (listTopQuantity.getSelectedIndex() != -1) {
                selectedCustomer = listTopQuantity.getSelectedValue();
            } else if (listTopSpent.getSelectedIndex() != -1) {
                selectedCustomer = listTopSpent.getSelectedValue();
            }

            if (selectedCustomer == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một khách hàng từ danh sách Top 3!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Date dFrom = dcFrom.getDate();
            Date dTo = dcTo.getDate();

            if (dFrom == null || dTo == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn đầy đủ ngày 'Từ' và 'Đến'!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (dFrom.after(dTo)) {
                JOptionPane.showMessageDialog(this, "Ngày 'Từ' không được lớn hơn ngày 'Đến'!", "Lỗi logic", JOptionPane.ERROR_MESSAGE);
                return;
            }

            java.util.Calendar calFrom = java.util.Calendar.getInstance();
            calFrom.setTime(dFrom);
            calFrom.set(Calendar.HOUR_OF_DAY, 0);
            calFrom.set(Calendar.MINUTE, 0);
            calFrom.set(Calendar.SECOND, 0);
            Timestamp tsFrom = new java.sql.Timestamp(calFrom.getTimeInMillis());

            Calendar calTo = Calendar.getInstance();
            calTo.setTime(dTo);
            calTo.set(Calendar.HOUR_OF_DAY, 23);
            calTo.set(Calendar.MINUTE, 59);
            calTo.set(Calendar.SECOND, 59);
            Timestamp tsTo = new Timestamp(calTo.getTimeInMillis());

            java.util.List<CustomerHistoryDTO> historyList = customerStatsBUS.getCustomerHistory(selectedCustomer.getCustomerId(), tsFrom, tsTo);

            modelHistory.setRowCount(0);
            java.text.SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            for (CustomerHistoryDTO h : historyList) {
                modelHistory.addRow(new Object[]{
                        h.getBillId(),
                        sdf.format(h.getCreatedDate()),
                        h.getCustomerName(),
                        h.getCustomerPhone(),
                        h.getTotalQuantity(),
                        MoneyFormatter.toVND(h.getTotalPrice())
                });
            }

            if (historyList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu hóa đơn chi tiết!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }
}
