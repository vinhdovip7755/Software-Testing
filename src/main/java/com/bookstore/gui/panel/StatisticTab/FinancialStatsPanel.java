package com.bookstore.gui.panel.StatisticTab;

import com.bookstore.bus.FinancialStatsBUS;
import com.bookstore.dto.FinancialStatsDTO;
import com.bookstore.util.AppConstant;
import com.bookstore.util.Refreshable;
import com.toedter.calendar.JDateChooser;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FinancialStatsPanel extends JPanel implements Refreshable {
    private final FinancialStatsBUS thongKeBus = new FinancialStatsBUS();
    private final NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
    
    private final JDateChooser dchTuNgay = new JDateChooser();
    private final JDateChooser dchDenNgay = new JDateChooser();
    private final JButton btnThongKe = new JButton("Thống kê");

    private final JLabel lblDoanhThuFiltered = createValueLabel();
    private final JLabel lblVonNhapHangFiltered = createValueLabel();
    private final JLabel lblLoiNhuanFiltered = createValueLabel();

    private final JLabel lblDoanhThuTotal = createValueLabel();
    private final JLabel lblVonNhapHangTotal = createValueLabel();
    private final JLabel lblLoiNhuanTotal = createValueLabel();

    private JLabel createValueLabel() {
        JLabel lbl = new JLabel(".....VND", SwingConstants.CENTER);
        lbl.setForeground(new Color(40, 40, 40));
        return lbl;
    }

    private final JLabel lblQuyCaoNhat = new JLabel("Quý doanh thu cao nhất: --");
    private final JLabel lblQuyThapNhat = new JLabel("Quý doanh thu thấp nhất: --");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Thời gian", "Doanh thu", "Lợi nhuận", "Chi phí", "Vốn nhập hàng"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tblChiTiet = new JTable(tableModel);

    private TitledBorder filteredBorder;

    public FinancialStatsPanel() {
        initUI();
        bindEvents();
        // Set default dates
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        dchTuNgay.setDate(cal.getTime());
        dchDenNgay.setDate(new Date());
        loadThongKe();
    }

    @Override
    public void refresh() {
        loadThongKe();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel pnlTop = new JPanel();
        pnlTop.setLayout(new BoxLayout(pnlTop, BoxLayout.Y_AXIS));
        pnlTop.setOpaque(false);

        // 1. Filter Panel
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        pnlFilter.setOpaque(false);
        
        pnlFilter.add(new JLabel("Từ ngày:"));
        dchTuNgay.setPreferredSize(new Dimension(150, 35));
        pnlFilter.add(dchTuNgay);
        pnlFilter.add(new JLabel("Đến ngày:"));
        dchDenNgay.setPreferredSize(new Dimension(150, 35));
        pnlFilter.add(dchDenNgay);
        
        btnThongKe.setPreferredSize(new Dimension(100, 35));
        btnThongKe.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        btnThongKe.setForeground(Color.WHITE);
        btnThongKe.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        pnlFilter.add(btnThongKe);
        
        pnlTop.add(pnlFilter);
        pnlTop.add(Box.createVerticalStrut(15));

        // 2. Filtered Stats Panel
        JPanel pnlFilteredStats = new JPanel(new GridLayout(1, 3, 20, 0));
        pnlFilteredStats.setOpaque(false);
        filteredBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.decode(AppConstant.GREEN_COLOR_CODE), 2),
                "Thống kê từ ngày... đến ngày..."
        );
        filteredBorder.setTitleFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        pnlFilteredStats.setBorder(BorderFactory.createCompoundBorder(
                filteredBorder,
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        pnlFilteredStats.add(createInnerBox("Doanh thu", lblDoanhThuFiltered));
        pnlFilteredStats.add(createInnerBox("Vốn nhập hàng", lblVonNhapHangFiltered));
        pnlFilteredStats.add(createInnerBox("Lợi nhuận", lblLoiNhuanFiltered));

        pnlTop.add(pnlFilteredStats);
        pnlTop.add(Box.createVerticalStrut(15));

        // 3. Total Stats Panel
        JPanel pnlTotalStats = new JPanel(new GridLayout(1, 3, 20, 0));
        pnlTotalStats.setOpaque(false);
        TitledBorder totalBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.decode(AppConstant.GREEN_COLOR_CODE), 2),
                "Thống kê tổng"
        );
        totalBorder.setTitleFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        pnlTotalStats.setBorder(BorderFactory.createCompoundBorder(
                totalBorder,
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        pnlTotalStats.add(createInnerBox("Doanh thu", lblDoanhThuTotal));
        pnlTotalStats.add(createInnerBox("Vốn nhập hàng", lblVonNhapHangTotal));
        pnlTotalStats.add(createInnerBox("Lợi nhuận", lblLoiNhuanTotal));

        pnlTop.add(pnlTotalStats);
        pnlTop.add(Box.createVerticalStrut(15));

        // 4. Quarter Info
        JPanel pnlQuarterInfo = new JPanel(new GridLayout(2, 1, 0, 5));
        pnlQuarterInfo.setOpaque(false);
        Font quarterFont = new Font(AppConstant.FONT_NAME, Font.BOLD, 14);

        lblQuyCaoNhat.setOpaque(true);
        lblQuyCaoNhat.setBackground(new Color(236, 253, 245));
        lblQuyCaoNhat.setForeground(new Color(21, 128, 61));
        lblQuyCaoNhat.setFont(quarterFont);
        lblQuyCaoNhat.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        lblQuyThapNhat.setOpaque(true);
        lblQuyThapNhat.setBackground(new Color(254, 242, 242));
        lblQuyThapNhat.setForeground(new Color(185, 28, 28));
        lblQuyThapNhat.setFont(quarterFont);
        lblQuyThapNhat.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        pnlQuarterInfo.add(lblQuyCaoNhat);
        pnlQuarterInfo.add(lblQuyThapNhat);
        pnlTop.add(pnlQuarterInfo);
        
        add(pnlTop, BorderLayout.NORTH);

        // 5. Table
        tblChiTiet.setRowHeight(35);
        tblChiTiet.setShowGrid(true);
        tblChiTiet.setGridColor(Color.decode("#E0E0E0"));
        tblChiTiet.setSelectionBackground(Color.decode("#d4ffee"));
        tblChiTiet.setSelectionForeground(Color.BLACK);
        tblChiTiet.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));

        JTableHeader header = tblChiTiet.getTableHeader();
        header.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        header.setForeground(Color.WHITE);
        header.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setReorderingAllowed(false);

        tblChiTiet.getColumnModel().getColumn(1).setCellRenderer(new CurrencyCellRenderer());
        tblChiTiet.getColumnModel().getColumn(2).setCellRenderer(new ProfitCellRenderer());
        tblChiTiet.getColumnModel().getColumn(3).setCellRenderer(new CurrencyCellRenderer());
        tblChiTiet.getColumnModel().getColumn(4).setCellRenderer(new CurrencyCellRenderer());

        JScrollPane scrollPane = new JScrollPane(tblChiTiet);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createInnerBox(String title, JLabel lblValue) {
        JPanel box = new JPanel(new BorderLayout());
        box.setOpaque(false);
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.decode(AppConstant.GREEN_COLOR_CODE), 1),
                title
        );
        border.setTitleFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        box.setBorder(BorderFactory.createCompoundBorder(
                border,
                BorderFactory.createEmptyBorder(20, 10, 20, 10)
        ));
        
        lblValue.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 26));
        box.add(lblValue, BorderLayout.CENTER);
        return box;
    }

    private void bindEvents() {
        btnThongKe.addActionListener(e -> loadThongKe());
    }

    private void loadThongKe() {
        Date tuNgay = dchTuNgay.getDate();
        Date denNgay = dchDenNgay.getDate();

        try {
            thongKeBus.validateThongKeTheoNgay(tuNgay, denNgay);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Update title with formatted dates
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        filteredBorder.setTitle("Thống kê từ ngày " + sdf.format(tuNgay) + " đến ngày " + sdf.format(denNgay));
        repaint();

        // 1. Load Filtered Data
        List<FinancialStatsDTO> currentData = thongKeBus.getThongKeTheoNgay(tuNgay, denNgay);
        double fDoanhThu = currentData.stream().mapToDouble(FinancialStatsDTO::getDoanhThu).sum();
        double fChiPhi = currentData.stream().mapToDouble(FinancialStatsDTO::getChiPhi).sum();
        double fVon = currentData.stream().mapToDouble(FinancialStatsDTO::getVonNhapHang).sum();
        double fLoiNhuan = fDoanhThu - fChiPhi;

        lblDoanhThuFiltered.setText(formatCurrency(fDoanhThu));
        lblVonNhapHangFiltered.setText(formatCurrency(fVon));
        lblLoiNhuanFiltered.setText(formatCurrency(fLoiNhuan));
        lblLoiNhuanFiltered.setForeground(fLoiNhuan < 0 ? new Color(198, 40, 40) : new Color(22, 163, 74));

        // 2. Load Total Data
        FinancialStatsDTO totalData = thongKeBus.getThongKeTong();
        lblDoanhThuTotal.setText(formatCurrency(totalData.getDoanhThu()));
        lblVonNhapHangTotal.setText(formatCurrency(totalData.getVonNhapHang()));
        lblLoiNhuanTotal.setText(formatCurrency(totalData.getLoiNhuan()));
        lblLoiNhuanTotal.setForeground(totalData.getLoiNhuan() < 0 ? new Color(198, 40, 40) : new Color(22, 163, 74));

        // 3. Update Quarter Info
        int year = thongKeBus.extractYear(tuNgay);
        FinancialStatsBUS.QuyDoanhThuSummary summary = thongKeBus.getQuyDoanhThuSummary(year);
        if (summary == null) {
            lblQuyCaoNhat.setText("Quý doanh thu cao nhất (" + year + "): Không có dữ liệu");
            lblQuyThapNhat.setText("Quý doanh thu thấp nhất (" + year + "): Không có dữ liệu");
        } else {
            lblQuyCaoNhat.setText("Quý doanh thu cao nhất (" + summary.getNam() + "): Q" + summary.getQuyCaoNhat()
                    + " - " + formatCurrency(summary.getDoanhThuQuyCaoNhat()));
            lblQuyThapNhat.setText("Quý doanh thu thấp nhất (" + summary.getNam() + "): Q" + summary.getQuyThapNhat()
                    + " - " + formatCurrency(summary.getDoanhThuQuyThapNhat()));
        }

        // 4. Update Table
        tableModel.setRowCount(0);
        for (FinancialStatsDTO item : currentData) {
            tableModel.addRow(new Object[]{
                    item.getThoiGian(),
                    item.getDoanhThu(),
                    item.getLoiNhuan(),
                    item.getChiPhi(),
                    item.getVonNhapHang()
            });
        }
    }

    private String formatCurrency(double amount) {
        return currencyFormat.format(amount) + " VNĐ";
    }

    private class CurrencyCellRenderer extends DefaultTableCellRenderer {
        @Override
        protected void setValue(Object value) {
            if (value instanceof Number number) {
                setText(formatCurrency(number.doubleValue()));
            } else {
                super.setValue(value);
            }
            setHorizontalAlignment(SwingConstants.RIGHT);
        }
    }

    private class ProfitCellRenderer extends CurrencyCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected && value instanceof Number number) {
                c.setForeground(number.doubleValue() < 0 ? new Color(198, 40, 40) : new Color(22, 163, 74));
            }
            return c;
        }
    }
}
