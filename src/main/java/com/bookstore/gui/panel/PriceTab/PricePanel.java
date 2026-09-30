package com.bookstore.gui.panel.PriceTab;

import com.bookstore.util.AppConstant;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import com.bookstore.bus.*;
import com.bookstore.dto.*;
import com.bookstore.util.Refreshable;

public class PricePanel extends JPanel implements Refreshable {
    private List<PriceDTO> listPrices;
    public List<PriceDTO> listHienThi;
    public PriceBUS bus = new PriceBUS();

    private JRadioButton rbPrice, rbName, rbAuthor, rbCategory;
    private JPanel searchCard, tableCard, inputCards;
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JTextField txtMinPrice, txtMaxPrice;
    private boolean hasSearched = false;

    public PricePanel() {
        setLayout(new BorderLayout(0, 25));
        setBackground(new Color(235, 235, 235));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        initSearchCard();
        initTableUI();
        refresh();
    }

    @Override
    public void refresh() {
        this.listPrices = bus.getPriceDTOs();
        this.listHienThi = this.listPrices;
        capNhatBang(listHienThi);
    }

    private void initSearchCard() {
        searchCard = new JPanel();
        searchCard.setLayout(new BoxLayout(searchCard, BoxLayout.Y_AXIS));
        searchCard.setBackground(Color.WHITE);

        searchCard.putClientProperty(FlatClientProperties.STYLE, "arc: 35");
        searchCard.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JPanel selectionBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        selectionBox.setOpaque(false);
        selectionBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        rbPrice = new JRadioButton("Khoảng giá");
        rbAuthor = new JRadioButton("Tên tác giả");
        rbName = new JRadioButton("Tên sách", true);
        rbCategory = new JRadioButton("Thể loại", true);

        ButtonGroup group = new ButtonGroup();
        group.add(rbPrice);
        group.add(rbAuthor);
        group.add(rbName);
        group.add(rbCategory);
        selectionBox.add(new JLabel("Tìm kiếm:"));
        selectionBox.add(rbPrice);
        selectionBox.add(rbAuthor);
        selectionBox.add(rbName);
        selectionBox.add(rbCategory);

        txtSearch = new JTextField();
        JButton btnSearch = new JButton("Tìm kiếm");

        JPanel searchWrapper = new JPanel(new BorderLayout());
        searchWrapper.setBackground(Color.WHITE);

        searchWrapper.setBorder(new javax.swing.border.Border() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220, 220, 220));
                g2.drawRoundRect(x, y, width - 1, height - 1, 37, 37);
                g2.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(1, 1, 1, 1);
            }

            @Override
            public boolean isBorderOpaque() {
                return false;
            }
        });

        searchWrapper.setOpaque(false);
        searchWrapper.setMaximumSize(new Dimension(800, 45));

        JPanel leftBox = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(225, 230, 225));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 37, 37);
                g2.dispose();
            }
        };
        leftBox.setPreferredSize(new Dimension(150, 45));
        leftBox.setOpaque(false);
        leftBox.setBorder(new javax.swing.border.Border() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(200, 210, 200));
                g2.drawRoundRect(x, y, width - 1, height - 1, 37, 37);
                g2.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(1, 1, 1, 1);
            }

            @Override
            public boolean isBorderOpaque() {
                return false;
            }
        });
        leftBox.add(new JLabel("| Nhập "));

        txtSearch = new JTextField();
        setupSearchBorderStyle(txtSearch, "Tìm kiếm sách...", true);

        JPanel rangePanel = new JPanel(new GridLayout(1, 2, 5, 0));
        rangePanel.setOpaque(false);
        txtMinPrice = new JTextField();
        txtMaxPrice = new JTextField();
        setupSearchBorderStyle(txtMinPrice, "Giá từ...", false);
        setupSearchBorderStyle(txtMaxPrice, "đến...", false);
        rangePanel.add(txtMinPrice);
        rangePanel.add(txtMaxPrice);

        inputCards = new JPanel(new CardLayout());
        inputCards.setOpaque(false);
        inputCards.add(txtSearch, "NORMAL");
        inputCards.add(rangePanel, "RANGE");

        rbPrice.addActionListener(e -> ((CardLayout) inputCards.getLayout()).show(inputCards, "RANGE"));
        ActionListener showNormal = e -> ((CardLayout) inputCards.getLayout()).show(inputCards, "NORMAL");
        rbAuthor.addActionListener(showNormal);
        rbName.addActionListener(showNormal);
        rbCategory.addActionListener(showNormal);

        txtMinPrice.addActionListener(e -> btnSearch.doClick());
        txtMaxPrice.addActionListener(e -> btnSearch.doClick());

        txtSearch.revalidate();
        txtSearch.repaint();
        txtSearch.addActionListener(e -> btnSearch.doClick());

        btnSearch.setBackground(new Color(17, 71, 50));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setPreferredSize(new Dimension(130, 45));
        btnSearch.setContentAreaFilled(false);
        btnSearch.setFocusPainted(false);
        btnSearch.setBorder(new javax.swing.border.Border() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 100));
                g2.drawRoundRect(x, y, width - 1, height - 1, 37, 37);
                g2.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(1, 1, 1, 1);
            }

            @Override
            public boolean isBorderOpaque() {
                return false;
            }
        });

        btnSearch.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c.getBackground());
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 37, 37);
                g2.dispose();
                super.paint(g, c);
            }
        });

        searchWrapper.add(leftBox, BorderLayout.WEST);
        searchWrapper.add(inputCards, BorderLayout.CENTER);
        searchWrapper.add(btnSearch, BorderLayout.EAST);

        JPanel searchBoxContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchBoxContainer.setOpaque(false);
        searchBoxContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchBoxContainer.setMaximumSize(new Dimension(500, 45));
        searchBoxContainer.add(searchWrapper);

        searchCard.add(selectionBox);
        searchCard.add(Box.createVerticalStrut(10));
        searchCard.add(searchBoxContainer);

        btnSearch.addActionListener(e -> SearchProcessing());
    }

    private void SearchProcessing() {
        String type = "Tên";
        if (rbPrice.isSelected())
            type = "Giá";
        else if (rbAuthor.isSelected())
            type = "Tác giả";
        else if (rbCategory.isSelected())
            type = "Thể loại";

        String input;

        if (type.equals("Giá")) {
            String minStr = txtMinPrice.getText().trim();
            String maxStr = txtMaxPrice.getText().trim();

            if (minStr.isEmpty() && maxStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập khoảng giá cần tìm!", "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!minStr.isEmpty() && !minStr.matches("^\\d+(\\.\\d+)?$")) {
                JOptionPane.showMessageDialog(this, "Giá tối thiểu phải là số không âm hợp lệ!", "Lỗi nhập liệu",
                        JOptionPane.ERROR_MESSAGE);
                txtMinPrice.requestFocus();
                return;
            }
            if (!maxStr.isEmpty() && !maxStr.matches("^\\d+(\\.\\d+)?$")) {
                JOptionPane.showMessageDialog(this, "Giá tối đa phải là số không âm hợp lệ!", "Lỗi nhập liệu",
                        JOptionPane.ERROR_MESSAGE);
                txtMaxPrice.requestFocus();
                return;
            }

            if (!minStr.isEmpty() && !maxStr.isEmpty()) {
                double minVal = Double.parseDouble(minStr);
                double maxVal = Double.parseDouble(maxStr);
                if (minVal > maxVal) {
                    JOptionPane.showMessageDialog(this, "Giá tối thiểu không được lớn hơn giá tối đa!", "Lỗi nhập liệu",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            input = minStr + "-" + maxStr;
        } else {
            input = txtSearch.getText().trim();

            if (input.isEmpty() || input.equals("Tìm kiếm chương trình...")) {
                if (hasSearched) {
                    refresh();
                } else {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập từ khóa tìm kiếm!", "Thông báo",
                            JOptionPane.WARNING_MESSAGE);
                }
                return;
            }
            if (input.matches("^[0-9]+$")) {
                JOptionPane.showMessageDialog(this, type + " không được nhập số!", "Lỗi nhập liệu",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        this.listHienThi = bus.timKiemSach(listPrices, input, type);
        hasSearched = true;

        if (listHienThi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy kết quả phù hợp!", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        capNhatBang(listHienThi);
    }

    private void initTableUI() {
        tableCard = new JPanel(new BorderLayout(0, 15));
        tableCard.setOpaque(false);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        JButton btnLoiNhuan = new JButton("Chỉnh lợi nhuận chung");
        btnLoiNhuan.setVisible(false);
        btnLoiNhuan.setPreferredSize(new Dimension(220, 45));
        btnLoiNhuan.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        btnLoiNhuan.setForeground(Color.WHITE);
        btnLoiNhuan.setFocusPainted(false);
        btnLoiNhuan.setBorderPainted(false);
        btnLoiNhuan.setContentAreaFilled(false);

        btnLoiNhuan.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c.getBackground());
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 25, 25);
                g2.dispose();
                super.paint(g, c);
            }
        });

        btnLoiNhuan.addActionListener(e -> showEditAllBooksProfitDialog());
        toolbar.add(btnLoiNhuan, BorderLayout.WEST);

        JPanel whiteBox = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        whiteBox.setOpaque(false);
        whiteBox.setBackground(Color.WHITE);
        whiteBox.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] columns = { "Tên sách", "Tác giả", "Thể loại", "Giá vốn trung bình", "Lợi nhuận", "Giá bán hiện tại" };
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        setupTableStyle(table);
        JScrollPane scroll1 = new JScrollPane(table);
        scroll1.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Danh sách định giá hệ thống"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        scroll1.getViewport().setBackground(Color.WHITE);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
if(true) return;
                int row = table.getSelectedRow();
                if (row == -1)
                    return;

                if (e.getClickCount() == 2) {
                    table.clearSelection();
                    table.repaint();
                    return;
                }

                
            }
        });

        whiteBox.add(scroll1);
        tableCard.add(toolbar, BorderLayout.NORTH);
        tableCard.add(whiteBox, BorderLayout.CENTER);

        add(searchCard, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
    }

    private void capNhatBang(List<PriceDTO> list) {
        if (model == null)
            return;
        model.setRowCount(0);
        if (list == null || list.isEmpty())
            return;

        for (PriceDTO p : list) {
            model.addRow(new Object[] {
                    p.getBookName(),
                    p.getAuthorName() != null ? p.getAuthorName() : "Nhiều tác giả",
                    p.getCategoryName() != null ? p.getCategoryName() : "Khác",
                    String.format("%,.0f đ", p.getBasePrice()),
                    String.format("%.1f %%", p.getProfitRate() * 100),
                    String.format("%,.0f đ", p.getSellingPrice())
            });
        }
    }

    private void setupTableStyle(JTable t) {
        t.setShowGrid(true);
        Color vachKe = new Color(200, 210, 200);
        t.setGridColor(vachKe);
        t.setIntercellSpacing(new Dimension(1, 1));
        t.setRowHeight(45);

        t.getTableHeader().setBackground(new Color(225, 230, 225));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.getTableHeader().setReorderingAllowed(false);

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {

                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);

                if (isSelected) {
                    label.setBackground(new Color(17, 71, 50));
                    label.setForeground(Color.WHITE);
                } else {
                    label.setForeground(Color.BLACK);
                    if (row % 2 != 0) {
                        label.setBackground(new Color(180, 200, 180));
                    } else {
                        label.setBackground(Color.WHITE);
                    }
                }
                return label;
            }
        });

        
    }

    private void showEditPriceDialog(PriceDTO p) {
        double currentPercent = p.getProfitRate() * 100;
        String res = JOptionPane.showInputDialog(this,
                "Mức giá vốn hiện tại: " + String.format("%,.0f", p.getBasePrice()) + " VNĐ\nNhập % lợi nhuận mới:",
                currentPercent);

        if (res == null || res.trim().isEmpty())
            return;

        if (!res.trim().matches("^\\d+(\\.\\d+)?$")) {
            JOptionPane.showMessageDialog(this, "Tỷ suất lợi nhuận phải là số không âm hợp lệ!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double newPercent = Double.parseDouble(res.trim());
            double newProfitRate = newPercent / 100.0;
            double predictedPrice = p.getBasePrice() * (1 + newProfitRate);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Tỷ suất lợi nhuận: " + newPercent + "%\n" +
                            "Giá bán mới sẽ là: " + String.format("%,.0f", predictedPrice) + " VNĐ\n" +
                            "Xác nhận áp dụng mức giá này?",
                    "Chốt giá bán",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = bus.createNewPrice(p.getBookId(), p.getBasePrice(), newProfitRate, predictedPrice);

                if (success) {
                    JOptionPane.showMessageDialog(this, "Đã Cập nhật giá bán thành công!");
                    refresh();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi: Không thể lưu giá mới vào Database!");
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Vui lòng chỉ nhập số hợp lệ!");
        }
    }

    private void showEditAllBooksProfitDialog() {
        if (listHienThi == null || listHienThi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có sách nào trong danh sách hiện tại!");
            return;
        }

        String res = JOptionPane.showInputDialog(this,
                "Nhập % lợi nhuận áp dụng chung cho " + listHienThi.size() + " cuốn sách:");
        if (res != null && !res.trim().isEmpty()) {
            if (!res.trim().matches("^\\d+(\\.\\d+)?$")) {
                JOptionPane.showMessageDialog(this, "Tỷ suất lợi nhuận phải là số không âm hợp lệ!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                double newPercent = Double.parseDouble(res.trim());
                double newProfitRate = newPercent / 100.0;

                if (bus.updateBulkPrice(listHienThi, newProfitRate)) {
                    JOptionPane.showMessageDialog(this, "Đã chốt giá hàng loạt thành công!");
                    refresh();
                } else {
                    JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi Cập nhật giá hàng loạt!");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi: Vui lòng nhập số hợp lệ!");
            }
        }
    }

    private void setupSearchBorderStyle(JTextField field, String placeholder, boolean showIcon) {
        field.setOpaque(false);
        final FlatSVGIcon searchIcon = showIcon ? new FlatSVGIcon("icon/search_icon.svg", 18, 18) : null;
        int leftPadding = showIcon ? 35 : 15;

        field.setBorder(new javax.swing.border.EmptyBorder(0, leftPadding, 0, 10) {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (showIcon && searchIcon != null) {
                    searchIcon.paintIcon(c, g2, x + 10, (height - 18) / 2);
                }

                if (field.getText().isEmpty()) {
                    g2.setColor(Color.GRAY);
                    g2.setFont(c.getFont().deriveFont(Font.ITALIC));
                    g2.drawString(placeholder, x + leftPadding, (height + g2.getFontMetrics().getAscent()) / 2 - 2);
                }
                g2.dispose();
            }
        });
    }
}
