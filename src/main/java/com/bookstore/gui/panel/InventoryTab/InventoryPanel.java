package com.bookstore.gui.panel.InventoryTab;

import com.bookstore.bus.BookBUS;
import com.bookstore.dao.BookLotDAO;
import com.bookstore.dto.BookDTO;
import com.bookstore.dto.BookLotDTO;
import com.bookstore.util.AppConstant;
import com.bookstore.util.Refreshable;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InventoryPanel extends JPanel implements Refreshable {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTable lotTable;
    private DefaultTableModel lotTableModel;
    private BookBUS bookBUS = new BookBUS();
    private BookLotDAO bookLotDAO = new BookLotDAO();
    private List<BookDTO> currentList = new ArrayList<>();

    private JLabel lbTotalBooks, lbCategories, lbLowStock;
    private JTextField txtSearch;
    private JComboBox<String> cboStatus;
    private JButton btnResetFilter;

    public InventoryPanel() {
        initUI();
        loadData();
    }

    @Override
    public void refresh() {
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 20));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);

        centerPanel.add(createFilterPanel());
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(createStatsPanel());
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(createTablePanel());

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        JLabel lbTitle = new JLabel("THEO DÕI VÀ QUẢN LÝ TỒN KHO");
        lbTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lbTitle.setForeground(Color.BLACK);
        JPanel titleContainer = new JPanel(new GridLayout(2, 1));
        titleContainer.setBackground(Color.WHITE);
        titleContainer.add(new JLabel());
        titleContainer.add(lbTitle);
        panel.add(titleContainer, BorderLayout.WEST);

        JButton btnLog = new JButton("Biến Động Tồn Kho (Thẻ Kho)");
        btnLog.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnLog.setForeground(Color.WHITE);
        btnLog.setBackground(Color.decode("#1976D2"));
        btnLog.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin: 5,10,5,10; borderWidth: 0;");
        btnLog.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLog.addActionListener(e -> {
            new InventoryLogDialog(SwingUtilities.getWindowAncestor(this)).setVisible(true);
        });
        panel.add(btnLog, BorderLayout.EAST);

        return panel;
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(300, 45));
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm kiếm sách...");
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSVGIcon("icon/search_icon.svg").derive(20,20));
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc: 15;");

        cboStatus = new JComboBox<>(new String[]{"Tất cả trạng thái", "Đủ hàng", "Sắp hết hàng", "Hết hàng"});
        cboStatus.setPreferredSize(new Dimension(200, 45));
        cboStatus.putClientProperty(FlatClientProperties.STYLE, "arc: 15;");

        btnResetFilter = new JButton("Làm mới");
        btnResetFilter.setPreferredSize(new Dimension(100, 45));
        btnResetFilter.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        btnResetFilter.setForeground(Color.WHITE);
        btnResetFilter.putClientProperty(FlatClientProperties.STYLE, "arc: 15; borderWidth: 0;");
        btnResetFilter.setCursor(new Cursor(Cursor.HAND_CURSOR));

        
        JButton btnViewDetail = new JButton("Xem chi tiết");
        btnViewDetail.setPreferredSize(new Dimension(130, 45));
        btnViewDetail.setBackground(Color.decode("#1976D2"));
        btnViewDetail.setForeground(Color.WHITE);
        btnViewDetail.putClientProperty(FlatClientProperties.STYLE, "arc: 15; borderWidth: 0;");
        btnViewDetail.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnViewDetail.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một cuốn sách để xem chi tiết lô!");
                return;
            }
            int modelRow = table.convertRowIndexToModel(row);
            BookDTO selectedBook = (BookDTO) tableModel.getValueAt(modelRow, 0);
            loadLotData(selectedBook.getBookId());
            
            JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Chi tiết lô nhập: " + selectedBook.getBookName(), Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setSize(800, 400);
            dialog.setLocationRelativeTo(this);
            JScrollPane scroll = new JScrollPane(lotTable);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.getViewport().setBackground(Color.WHITE);
            dialog.add(scroll);
            dialog.setVisible(true);
        });

        panel.add(txtSearch);
        panel.add(cboStatus);
        panel.add(btnResetFilter);
        panel.add(btnViewDetail);

        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterData(); }
            public void removeUpdate(DocumentEvent e) { filterData(); }
            public void changedUpdate(DocumentEvent e) { filterData(); }
        };
        txtSearch.getDocument().addDocumentListener(dl);
        cboStatus.addActionListener(e -> filterData());

        btnResetFilter.addActionListener(e -> {
            txtSearch.setText("");
            cboStatus.setSelectedIndex(0);
        });

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 0));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(1000, 120));

        lbTotalBooks = new JLabel("0");
        lbCategories = new JLabel("0");
        lbLowStock = new JLabel("0");

        panel.add(createCard("Tổng số lượng sách", lbTotalBooks, "#E3F2FD", "#2196F3", "book_icon.svg"));
        panel.add(createCard("Loại sách", lbCategories, "#E8F5E9", "#4CAF50", "inventory_icon.svg"));
        panel.add(createCard("Sắp hết hàng", lbLowStock, "#FFEBEE", "#F44336", "stats_icon.svg"));

        return panel;
    }

    private JPanel createCard(String title, JLabel lbValue, String bgIconColor, String iconColor, String iconName) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#EEEEEE"), 1),
                new EmptyBorder(20, 25, 20, 25)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 20;");

        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        leftPanel.setBackground(Color.WHITE);

        JLabel lbTitle = new JLabel(title);
        lbTitle.setForeground(Color.GRAY);
        lbTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        lbValue.setFont(new Font("Segoe UI", Font.BOLD, 32));
        if (title.equals("Sắp hết hàng")) lbValue.setForeground(Color.decode(iconColor));

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
            FlatSVGIcon.ColorFilter filter = new FlatSVGIcon.ColorFilter(c -> Color.decode(iconColor));
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
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 20;");

        String[] columns = {"SÁCH", "DANH MỤC", "TÁC GIẢ", "TỒN KHO", "TRẠNG THÁI"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(75);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setForeground(Color.GRAY);
        table.getTableHeader().setBackground(Color.WHITE);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(Color.decode("#F5F5F5"));
        table.setSelectionForeground(Color.BLACK);
        table.setFocusable(false);
        table.setBorder(BorderFactory.createEmptyBorder());

        table.getColumnModel().getColumn(0).setCellRenderer(new BookInfoRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new CategoryRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new StockRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(300);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    int modelRow = table.convertRowIndexToModel(row);
                    BookDTO selectedBook = (BookDTO) tableModel.getValueAt(modelRow, 0);
                    loadLotData(selectedBook.getBookId());
                } else {
                    lotTableModel.setRowCount(0);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Lot Table
        String[] lotColumns = {"LÔ ID", "NGÀY NHẬP", "GIÁ NHẬP", "GIÁ BÁN", "SỐ LƯỢNG", "HIỆN TẠI", "TRẠNG THÁI"};
        lotTableModel = new DefaultTableModel(lotColumns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        lotTable = new JTable(lotTableModel);
        lotTable.setRowHeight(40);
        lotTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lotTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        lotTable.getTableHeader().setForeground(Color.GRAY);
        lotTable.getTableHeader().setBackground(Color.WHITE);
        lotTable.setShowVerticalLines(false);
        lotTable.setSelectionBackground(Color.decode("#E3F2FD"));
        lotTable.setSelectionForeground(Color.BLACK);
        lotTable.setFocusable(false);
        
        lotTable.getColumnModel().getColumn(6).setCellRenderer(new LotStatusRenderer());

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void loadLotData(int bookId) {
        lotTableModel.setRowCount(0);
        List<BookLotDTO> lots = bookLotDAO.getByBookId(bookId);
        for (BookLotDTO lot : lots) {
            String status = lot.getQuantityRemain() > 0 ? (lot.getQuantityRemain() < 10 ? "SẮP HẾT" : "ĐỦ HÀNG") : "HẾT HÀNG";
            lotTableModel.insertRow(0, new Object[]{
                    lot.getLotId(),
                    lot.getImportDate(),
                    String.format("%,.0f", lot.getImportPrice()),
                    String.format("%,.0f", lot.getSellingPrice()),
                    lot.getQuantityInitial(),
                    lot.getQuantityRemain(),
                    status
            });
        }
    }

    private void loadData() {
        currentList = bookBUS.selectAllBooks();
        filterData();
    }

    private void filterData() {
        tableModel.setRowCount(0);
        String keyword = txtSearch.getText().trim().toLowerCase();
        int statusIdx = cboStatus.getSelectedIndex();

        int sumQty = 0;
        int lowStockCount = 0;
        Set<Integer> uniqueCategories = new HashSet<>();

        for (BookDTO book : currentList) {
            boolean matchKey = keyword.isEmpty() || book.getBookName().toLowerCase().contains(keyword);

            String statusDisplay = "Đủ hàng";
            if (book.getQuantity() == 0) statusDisplay = "HẾT HÀNG";
            else if (book.getQuantity() < 30) statusDisplay = "SẮP HẾT HÀNG";

            boolean matchStatus = true;
            if (statusIdx == 1 && !statusDisplay.equals("Đủ hàng")) matchStatus = false;
            if (statusIdx == 2 && !statusDisplay.equals("SẮP HẾT HÀNG")) matchStatus = false;
            if (statusIdx == 3 && !statusDisplay.equals("HẾT HÀNG")) matchStatus = false;

            if (matchKey && matchStatus) {
                tableModel.addRow(new Object[]{
                        book,
                        book.getCategoryName(),
                        book.getAuthorsName(),
                        book.getQuantity(),
                        statusDisplay
                });
            }

            sumQty += book.getQuantity();
            uniqueCategories.add(book.getCategoryId());
            if (book.getQuantity() > 0 && book.getQuantity() < 30) lowStockCount++;
        }

        lbTotalBooks.setText(String.valueOf(sumQty));
        lbCategories.setText(String.valueOf(uniqueCategories.size()));
        lbLowStock.setText(String.valueOf(lowStockCount));
    }

    static class BookInfoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            if (value instanceof BookDTO book) {
                JLabel lbIcon = new JLabel();
                lbIcon.setPreferredSize(new Dimension(45, 60));
                lbIcon.setOpaque(true);
                lbIcon.setBackground(Color.decode("#EEEEEE"));
                lbIcon.setHorizontalAlignment(SwingConstants.CENTER);

                if (book.getImage() != null && !book.getImage().isEmpty()) {
                    File file = new File("data/book_covers/" + book.getImage());
                    if (file.exists()) {
                        lbIcon.setIcon(new ImageIcon(new ImageIcon(file.getAbsolutePath()).getImage().getScaledInstance(45, 60, Image.SCALE_SMOOTH)));
                    } else { lbIcon.setText("Ảnh"); }
                } else { lbIcon.setText("Ảnh"); }

                JLabel lbName = new JLabel("<html><body style='width: 180px'>" + book.getBookName() + "</body></html>");
                lbName.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lbName.setForeground(isSelected ? table.getSelectionForeground() : Color.BLACK);

                panel.add(lbIcon);
                panel.add(lbName);
            }
            return panel;
        }
    }

    static class CategoryRenderer extends DefaultTableCellRenderer {
        private final JLabel label = new JLabel();
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            label.setText(value != null ? value.toString() : "");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setOpaque(true);
            label.setBackground(Color.decode("#F3F4F6"));
            label.setForeground(Color.decode("#374151"));
            label.setBorder(new EmptyBorder(5, 10, 5, 10));
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            panel.add(label);
            return panel;
        }
    }

    static class StockRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof Integer stock) {
                setFont(new Font("Segoe UI", Font.BOLD, 16));
                if (stock < 30) setForeground(Color.decode("#F44336"));
                else setForeground(Color.BLACK);
            }
            return c;
        }
    }

    static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = new JLabel();
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            String status = value != null ? value.toString() : "";

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            if (status.equals("SẮP HẾT HÀNG") || status.equals("HẾT HÀNG")) {
                label.setText("⚠  " + status);
                label.setForeground(Color.decode("#FF5722"));
                label.setBackground(Color.decode("#FBE9E7"));
            } else {
                label.setText(" " + status + " ");
                label.setForeground(Color.decode("#1B5E20"));
                label.setBackground(Color.decode("#E8F5E9"));
            }
            label.setOpaque(true);
            label.setBorder(new EmptyBorder(5, 10, 5, 10));
            panel.add(label);
            return panel;
        }
    }

    static class LotStatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = new JLabel();
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            String status = value != null ? value.toString() : "";

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            if (status.equals("SẮP HẾT") || status.equals("HẾT HÀNG")) {
                label.setText("⚠  " + status);
                label.setForeground(Color.decode("#FF5722"));
                label.setBackground(Color.decode("#FBE9E7"));
            } else {
                label.setText(" " + status + " ");
                label.setForeground(Color.decode("#1B5E20"));
                label.setBackground(Color.decode("#E8F5E9"));
            }
            label.setOpaque(true);
            label.setBorder(new EmptyBorder(5, 10, 5, 10));
            panel.add(label);
            return panel;
        }
    }
}