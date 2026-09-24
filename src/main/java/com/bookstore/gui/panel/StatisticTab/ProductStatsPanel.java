package com.bookstore.gui.panel.StatisticTab;

import com.bookstore.bus.ProductStatsBUS;
import com.bookstore.dto.ProductStatsDTO;
import com.bookstore.util.AppConstant;
import com.bookstore.util.Refreshable;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;


public class ProductStatsPanel extends JPanel implements Refreshable{

    private final JComboBox<String> cboThongKeTheo = new JComboBox<>(new String[]{"Ngày", "Tháng", "Quý", "Năm"});

    private final JDateChooser dchTuNgay = new JDateChooser(new Date());
    private final JDateChooser dchDenNgay = new JDateChooser(new Date());

    private final JButton btnThongKe = new JButton("Thống kê");

    private final JLabel lblTopBanChay = new JLabel("Top 3 bán chạy: --");
    private final JLabel lblTopBanIt = new JLabel("Top 3 bán ít: --");

    private final ProductStatsBUS bus= new ProductStatsBUS();

    private List<ProductStatsDTO> currentData = new ArrayList<>();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Thời gian", "Mã sách", "Tên sách", "Số lượng bán"},0
    ){
        @Override
        public boolean isCellEditable(int row, int column){
            return false;
        }
    };

    private final JTable table = new JTable(tableModel);

    public ProductStatsPanel(){
        initUI();
        bindEvents();
        loadThongKe();
    }

    @Override
    public void refresh(){
        loadThongKe();
    }

    private void initUI(){
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        add(createFilterPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
    }







    private JPanel createFilterPanel(){
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15,10));

        panel.setPreferredSize(new Dimension(0,70));
        Font filterFont = new Font(AppConstant.FONT_NAME, Font.PLAIN,14);

        dchTuNgay.setDateFormatString("dd/MM/yyyy");
        dchDenNgay.setDateFormatString("dd/MM/yyyy");

        cboThongKeTheo.setPreferredSize(new Dimension(120,30));
        dchTuNgay.setPreferredSize(new Dimension(120,30));
        dchDenNgay.setPreferredSize(new Dimension(120,30));
        btnThongKe.setPreferredSize(new Dimension(110,30));

        cboThongKeTheo.setFont(filterFont);
        dchTuNgay.setFont(filterFont);
        dchDenNgay.setFont(filterFont);
        btnThongKe.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnThongKe.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        btnThongKe.setForeground(Color.WHITE);

        panel.add(new JLabel("Thống kê theo:"));
        panel.add(cboThongKeTheo);
        panel.add(new JLabel("Từ ngày:"));
        panel.add(dchTuNgay);
        panel.add(new JLabel("Đến ngày:"));
        panel.add(dchDenNgay);
        panel.add(btnThongKe);

        return panel;
    }







    private JPanel createMainPanel(){
        JPanel panel = new JPanel(new BorderLayout(10,10));

        JPanel topPanel = new JPanel(new GridLayout(1,2,10,0));

        lblTopBanChay.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 16));
        lblTopBanChay.setBorder(BorderFactory.createEmptyBorder(10,15,10,15));
        lblTopBanChay.setOpaque(true);
        lblTopBanChay.setBackground(new Color(220,252,231));

        lblTopBanIt.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD,16));
        lblTopBanIt.setBorder(BorderFactory.createEmptyBorder(10,15,10,15));
        lblTopBanIt.setOpaque(true);
        lblTopBanIt.setBackground(new Color(254,242,242));

        topPanel.add(lblTopBanChay);
        topPanel.add(lblTopBanIt);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(createTablePanel(), BorderLayout.CENTER);

        return panel;
    }









    private JScrollPane createTablePanel(){
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0,0));

        JTableHeader header = table.getTableHeader();
        header.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        header.setForeground(Color.WHITE);
        header.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 13));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        return scroll;
    }









    private void bindEvents(){
        btnThongKe.addActionListener(e-> loadThongKe());
    }

    private void loadThongKe(){
        Date tuNgay = dchTuNgay.getDate();
        Date denNgay = dchDenNgay.getDate();

        if(tuNgay == null || denNgay == null){
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày");
            return;
        }

        if(tuNgay.after(denNgay)){
            JOptionPane.showMessageDialog(this, "Từ ngày không được lớn hơn đến ngày");
            return;
        }

        int year = Integer.parseInt(new SimpleDateFormat("yyyy").format(denNgay));

        String type = (String) cboThongKeTheo.getSelectedItem();

        if("Tháng".equals(type)){
            currentData = bus.getThongKeTheoThang(year);
        }else if("Quý".equals(type)){
            currentData = bus.getThongKeTheoQuy(year);
        }else if("Năm".equals(type)){
            currentData = bus.getThongKeTheoNam();
        }else{
            currentData = bus.getThongKeTheoNgay(new java.sql.Date(tuNgay.getTime()), new java.sql.Date(denNgay.getTime())
            );
        }

        updateTable();
        updateTopProducts(year);
    }








    private void updateTable(){
        tableModel.setRowCount(0);
        for(ProductStatsDTO item : currentData){

            tableModel.addRow(new Object[]{
                    item.getThoiGian(),
                    item.getBookId(),
                    item.getTenSach(),
                    item.getSoLuongBan()
            });
        }
    }

    private void updateTopProducts(int year){

        Date date = dchTuNgay.getDate();
        int month = Integer.parseInt(new SimpleDateFormat("MM").format(date));
        int quarter = (month - 1) / 3 + 1;

        List<ProductStatsDTO> topBanChay = bus.getTop3BanChayTheoQuy(year, quarter);
        List<ProductStatsDTO> topBanIt = bus.getTop3BanItTheoQuy(year, quarter);

        StringBuilder banChay = new StringBuilder("<html><b>Top 3 bán chạy:</b><br>");
        StringBuilder banIt = new StringBuilder("<html><b>Top 3 bán ít:</b><br>");

        for(ProductStatsDTO p : topBanChay){
            banChay.append("- ")
                    .append(p.getTenSach())
                    .append(" (")
                    .append(p.getSoLuongBan())
                    .append(")")
                    .append("<br>");
        }

        for(ProductStatsDTO p : topBanIt){
            banIt.append("- ")
                    .append(p.getTenSach())
                    .append(" (")
                    .append(p.getSoLuongBan())
                    .append(")")
                    .append("<br>");
        }

        banChay.append("</html>");
        banIt.append("</html");

        lblTopBanChay.setText(banChay.toString());
        lblTopBanIt.setText(banIt.toString());
    }
}