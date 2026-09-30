package com.bookstore.gui.panel.BillTab;
import com.bookstore.util.AppConstant;
import com.bookstore.util.MoneyFormatter;

import javax.swing.*;
import com.toedter.calendar.JDateChooser;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import com.bookstore.bus.BillBUS;
import com.bookstore.dto.BillDTO;
import com.bookstore.util.Refreshable;
import java.util.List;


public class BillPanel extends JPanel implements Refreshable {
    private JLabel lbTotalQuantity, lbRevenue;
    private JTable table;
    private DefaultTableModel model;
    private JDateChooser dchFrom;
    private JDateChooser dchTo;

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
        loadBillTable();
        applyTodayFilter();
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

        JLabel lbFrom = new JLabel("Từ:");
        lbFrom.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 16));
        dchFrom = new JDateChooser(new Date());
        dchFrom.setDateFormatString("dd/MM/yyyy");
        dchFrom.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 16));
        dchFrom.setPreferredSize(new Dimension(140, 30));

        JLabel lbTo = new JLabel("Đến:");
        lbTo.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 16));
        dchTo = new JDateChooser(new Date());
        dchTo.setDateFormatString("dd/MM/yyyy");
        dchTo.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 16));
        dchTo.setPreferredSize(new Dimension(140, 30));

        JTextField txtSearch = new JTextField(20);
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
        };
        table = new JTable(model);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for(int i = 0; i < table.getColumnCount(); i++){
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

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
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        });

        btnReset.addActionListener(e->{
            Date today = new Date();
            dchFrom.setDate(today);
            dchTo.setDate(today);
            applyTodayFilter();
        });

        btnFilter.addActionListener(e -> {
            Date rawFromDate = dchFrom.getDate();
            Date rawToDate = dchTo.getDate();
            String keyword = txtSearch.getText().trim().toLowerCase();

            Calendar calFrom = Calendar.getInstance();
            calFrom.setTime(rawFromDate);
            calFrom.set(Calendar.HOUR_OF_DAY, 0);
            calFrom.set(Calendar.MINUTE, 0);
            calFrom.set(Calendar.SECOND, 0);
            calFrom.set(Calendar.MILLISECOND, 0);
            Date fromDate = calFrom.getTime();

            Calendar calTo = Calendar.getInstance();
            calTo.setTime(rawToDate);
            calTo.set(Calendar.HOUR_OF_DAY, 23);
            calTo.set(Calendar.MINUTE, 59);
            calTo.set(Calendar.SECOND, 59);
            calTo.set(Calendar.MILLISECOND, 999);
            Date toDate = calTo.getTime();

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss - dd/MM/yyyy");

            sorter.setRowFilter(new RowFilter<>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    try {
                        String dateStr = entry.getStringValue(1);
                        Date billDate = sdf.parse(dateStr);

                        boolean matchDate = !billDate.before(fromDate) && !billDate.after(toDate);
                        boolean matchSearch = keyword.isEmpty()
                                || entry.getStringValue(0).toLowerCase().contains(keyword)
                                || entry.getStringValue(2).toLowerCase().contains(keyword)
                                || entry.getStringValue(3).toLowerCase().contains(keyword);

                        return matchDate && matchSearch;
                    } catch (ParseException ex) {
                        return true;
                    }
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
            total += (double) model.getValueAt(modelRow, 4);
        }

        lbTotalQuantity.setText(String.valueOf(count));
        lbRevenue.setText(MoneyFormatter.toVND(total));
    }

    private void loadBillTable(){
        if(model == null)
            return;
        model.setRowCount(0);

        BillBUS billBUS = new BillBUS();
        List<BillDTO> list = billBUS.getAllBills();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss - dd/MM/yyyy");

        for (BillDTO bill : list){
            String formattedDate = "";
            if(bill.getCreatedDate() != null){
                formattedDate = sdf.format(bill.getCreatedDate());
            }

            String customerName = bill.getCustomerName() == null ? "Khách lẻ" : bill.getCustomerName();
            model.addRow(new Object[]{
                    bill.getBillId(), formattedDate, bill.getEmployeeName(), customerName, bill.getTotalBillPrice()
            });
        }
        updateStatistics();
    }

    private void applyTodayFilter(){
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) table.getRowSorter();
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss - dd/MM/yyyy");

        sorter.setRowFilter(new RowFilter<>(){
            @Override
            public boolean include (Entry<? extends DefaultTableModel, ? extends Integer> entry){
                try{

                    String dateStr = entry.getStringValue(1);
                    Date billDate = sdf.parse(dateStr);
                    Calendar c1 = Calendar.getInstance();
                    Calendar c2 = Calendar.getInstance();

                    c1.setTime(today);
                    c2.setTime(billDate);

                    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
                }catch (Exception e){
                    return false;
                }
            }
        });
        updateStatistics();
    }
}
