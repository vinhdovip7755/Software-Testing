package com.bookstore.gui.panel.PriceTab;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import com.bookstore.dto.*;
import com.bookstore.api.client.ApiException;
import com.bookstore.api.client.PromotionApiClient;
import com.bookstore.util.AppConstant;
import com.bookstore.util.PermissionUtil;
import com.bookstore.util.Refreshable;

public class PromotionPanel extends JPanel implements Refreshable {
    private JRadioButton rbTen, rbPhanTram, rbThoiGian;
    private List<PromotionDTO> listPromotions;
    private List<PromotionDTO> listHienThi;

    private final PromotionApiClient apiClient = new PromotionApiClient();
    private JTable table;
    private JPanel cardPanel;
    private WhiteBoxPanel whiteBox;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private com.toedter.calendar.JDateChooser dchDateMin, dchDateMax;
    private JButton btnAdd;
    private JButton btnSearch;
    private JScrollPane scroll;
    private boolean hasSearched = false;
    private CardLayout searchCard = new CardLayout();
    private final Timer statusTimer;

    public PromotionPanel() {

        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(235, 235, 235));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        initHeader();
        loadData();

        statusTimer = new Timer(60000, e -> {
            if (isShowing() && !hasVisibleModalDialog()) {
                loadData();
            }
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (statusTimer != null && !statusTimer.isRunning()) {
            statusTimer.start();
        }
    }

    @Override
    public void removeNotify() {
        if (statusTimer != null && statusTimer.isRunning()) {
            statusTimer.stop();
        }
        super.removeNotify();
    }

    private boolean hasVisibleModalDialog() {
        for (Window w : Window.getWindows()) {
            if (w instanceof Dialog d && d.isModal() && d.isVisible()) {
                return true;
            }
        }
        return false;
    }

    class WhiteBoxPanel extends JPanel {
        public WhiteBoxPanel() {
            setOpaque(false);
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            g2.dispose();
        }
    }

    private void initHeader() {
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setOpaque(false);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JPanel filterBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBox.setOpaque(false);
        rbTen = new JRadioButton("Tên", true);
        rbPhanTram = new JRadioButton("Phần trăm");
        rbThoiGian = new JRadioButton("Thời gian");

        rbTen.addActionListener(e -> searchCard.show(cardPanel, "NORMAL"));
        rbPhanTram.addActionListener(e -> searchCard.show(cardPanel, "NORMAL"));
        rbThoiGian.addActionListener(e -> searchCard.show(cardPanel, "DATE"));

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbTen);
        bg.add(rbPhanTram);
        bg.add(rbThoiGian);

        filterBox.add(new JLabel("Tìm theo:"));
        filterBox.add(rbTen);
        filterBox.add(rbPhanTram);
        filterBox.add(rbThoiGian);

        btnAdd = new JButton("+ Thêm Khuyến Mãi") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isArmed()) {
                    g2.setColor(getBackground().darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(getBackground().brighter());
                } else {
                    g2.setColor(getBackground());
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };
        btnAdd.addActionListener(e -> {
            if (!PermissionUtil.hasActionPermission("MANAGE_PROMOTION")) {
                return;
            }
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(PromotionPanel.this);
            PromotionDialog dialog = new PromotionDialog(parentFrame, "Thêm Khuyến Mãi Mới", null);
            dialog.setVisible(true);
            loadData();
        });

        btnAdd.setContentAreaFilled(false);
        btnAdd.setBorderPainted(false);
        btnAdd.setFocusPainted(false);
        btnAdd.setOpaque(false);

        btnAdd.setPreferredSize(new Dimension(180, 42));
        btnAdd.setBackground(new Color(35, 90, 180));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));

        GridBagConstraints gbc = new GridBagConstraints();


        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 20);
        controlPanel.add(filterBox, gbc);


        gbc.gridx = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 0);
        controlPanel.add(createSearchWrapper(), gbc);


        gbc.gridx = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        controlPanel.add(spacer, gbc);


        gbc.gridx = 3;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        controlPanel.add(btnAdd, gbc);

        whiteBox = new WhiteBoxPanel();
        whiteBox.setLayout(new BorderLayout(0, 15));
        whiteBox.add(controlPanel, BorderLayout.NORTH);

        initTable();
        applyPermissions();

        add(whiteBox, BorderLayout.CENTER);


    }

    private void applyPermissions() {
        boolean canManagePromotion = PermissionUtil.hasActionPermission("MANAGE_PROMOTION");
        if (btnAdd != null) {
            btnAdd.setVisible(canManagePromotion);
            btnAdd.setEnabled(canManagePromotion);
        }
        if (table != null) {
            table.repaint();
        }
    }

    private JPanel createSearchWrapper() {
        JPanel wrapper = new JPanel(new BorderLayout(8, 0));
        wrapper.setOpaque(false);

        JPanel searchInputContainer = new JPanel(new BorderLayout());
        searchInputContainer.setOpaque(false);

        JPanel iconBox = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(180, 195, 180));
                g2.fillRoundRect(0, 0, getWidth() + 20, getHeight(), 25, 25);
                g2.dispose();
            }
        };
        iconBox.setPreferredSize(new Dimension(50, 42));
        iconBox.setOpaque(false);
        java.net.URL imgURL = getClass().getResource("/icon/search_icon.svg");
        if (imgURL != null) {
            iconBox.add(new JLabel(new com.formdev.flatlaf.extras.FlatSVGIcon("icon/search_icon.svg", 18, 18)));
        }

        cardPanel = new JPanel(searchCard);
        cardPanel.setOpaque(false);

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(240, 36));
        setupTextFieldStyle(txtSearch, "Tìm kiếm chương trình...");
        cardPanel.add(txtSearch, "NORMAL");

        JPanel dateGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        dateGroup.setOpaque(false);
        dateGroup.setBorder(null);

        dchDateMin = new com.toedter.calendar.JDateChooser();
        dchDateMax = new com.toedter.calendar.JDateChooser();
        dchDateMin.setDateFormatString("dd/MM/yyyy");
        dchDateMax.setDateFormatString("dd/MM/yyyy");
        dchDateMin.setMinSelectableDate(AppConstant.SYSTEM_MIN_DATE);
        dchDateMin.setMaxSelectableDate(AppConstant.SYSTEM_MAX_DATE);
        dchDateMax.setMinSelectableDate(AppConstant.SYSTEM_MIN_DATE);
        dchDateMax.setMaxSelectableDate(AppConstant.SYSTEM_MAX_DATE);
        java.awt.Dimension dateSize = new java.awt.Dimension(130, 30);
        dchDateMin.setPreferredSize(dateSize);
        dchDateMax.setPreferredSize(dateSize);

        PropertyChangeListener dateChangeListener = evt -> {
            if ("date".equals(evt.getPropertyName()) && rbThoiGian.isSelected()) {
                triggerAutoDateSearchIfValid();
            }
        };
        dchDateMin.addPropertyChangeListener("date", dateChangeListener);
        dchDateMax.addPropertyChangeListener("date", dateChangeListener);

        dateGroup.add(dchDateMin);
        dateGroup.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(12, 0)));
        dateGroup.add(dchDateMax);

        cardPanel.add(dateGroup, "DATE");

        JPanel inputBackground = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(235, 235, 235));
                g2.fillRoundRect(-20, 0, getWidth() + 20, getHeight(), 25, 25);
                g2.dispose();
            }
        };
        inputBackground.setOpaque(false);
        inputBackground.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 15));

        inputBackground.add(cardPanel, BorderLayout.CENTER);

        searchInputContainer.add(iconBox, BorderLayout.WEST);
        searchInputContainer.add(inputBackground, BorderLayout.CENTER);

        btnSearch = new JButton("Tìm kiếm") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isArmed()) {
                    g2.setColor(getBackground().darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(getBackground().brighter());
                } else {
                    g2.setColor(getBackground());
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btnSearch.setContentAreaFilled(false);
        btnSearch.setBorderPainted(false);
        btnSearch.setFocusPainted(false);
        btnSearch.setOpaque(false);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSearch.setPreferredSize(new Dimension(100, 42));
        btnSearch.setBackground(new Color(17, 71, 50));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSearch.addActionListener(e -> SearchProcessing());

        wrapper.add(searchInputContainer, BorderLayout.CENTER);
        wrapper.add(btnSearch, BorderLayout.EAST);
        return wrapper;
    }

    private void triggerAutoDateSearchIfValid() {
        Date minDate = dchDateMin.getDate();
        Date maxDate = dchDateMax.getDate();
        if (minDate == null && maxDate == null) {
            if (hasSearched) {
                loadData();
                hasSearched = false;
            }
            return;
        }
        LocalDate minLocal = minDate != null ? toLocalDate(minDate) : null;
        LocalDate maxLocal = maxDate != null ? toLocalDate(maxDate) : null;
        if (minLocal != null && maxLocal != null && minLocal.isAfter(maxLocal)) {
            return;
        }
        Date normMin = minLocal != null ? toStartOfDay(minLocal) : null;
        Date normMax = maxLocal != null ? toEndOfDay(maxLocal) : null;
        if ((normMin != null && AppConstant.isDateOutOfBounds(normMin))
                || (normMax != null && AppConstant.isDateOutOfBounds(normMax))) {
            return;
        }
        SearchProcessing();
    }

    private void initTable() {
        String[] cols = { "ID", "Tên chương trình", "Phần trăm", "Ngày bắt đầu", "Ngày kết thúc", "Trạng thái",
                "Thao tác" };
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);

        scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                        " Bảng Khuyến Mãi ",
                        TitledBorder.CENTER,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 14),
                        new Color(17, 71, 50)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        table.addMouseListener(new MouseAdapter() {
            private int lastSelectedRow = -1;

            @Override
            public void mousePressed(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != -1 && row == lastSelectedRow) {
                    table.clearSelection();
                    lastSelectedRow = -1;
                } else {
                    lastSelectedRow = row;
                }

                int column = table.columnAtPoint(e.getPoint());
                if (column == 6 && row != -1 && PermissionUtil.hasActionPermission("MANAGE_PROMOTION")) {
                    int modelRow = table.convertRowIndexToModel(row);
                    String idStr = model.getValueAt(modelRow, 0).toString().substring(2);
                    int id = Integer.parseInt(idStr);

                    PromotionDTO p = null;
                    if (listPromotions != null) {
                        for (PromotionDTO item : listPromotions) {
                            if (item.getPromotionId() == id) {
                                p = item;
                                break;
                            }
                        }
                    }

                    if (p != null) {
                        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(PromotionPanel.this);
                        PromotionDialog dialog = new PromotionDialog(parentFrame, "Chỉnh Sửa Khuyến Mãi", p);
                        dialog.setVisible(true);
                        loadData();
                    }
                }
            }
        });

        table.setRowHeight(50);
        table.setShowGrid(false);
        table.setGridColor(new Color(5, 30, 20));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setBackground(new Color(225, 230, 225));
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setBorder(null);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                c.setHorizontalAlignment(SwingConstants.CENTER);
                c.setOpaque(true);

                if (isSelected) {
                    c.setBackground(new Color(17, 71, 50));
                    c.setForeground(Color.WHITE);
                    c.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.WHITE));
                } else {
                    c.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(150, 155, 150)));
                    if (row % 2 == 0) {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    } else {
                        c.setBackground(new Color(180, 200, 180));
                        c.setForeground(Color.BLACK);
                    }
                }
                return c;
            }
        });

        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));
                p.setOpaque(true);

                if (isSelected) {
                    p.setBackground(new Color(17, 71, 50));
                    p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.WHITE));
                } else {
                    p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(150, 155, 150)));
                    if (row % 2 == 0) {
                        p.setBackground(Color.WHITE);
                    } else {
                        p.setBackground(new Color(180, 200, 180));
                    }
                }

                if (PermissionUtil.hasActionPermission("MANAGE_PROMOTION")) {
                    JButton btnEdit = new JButton("Sửa");
                    btnEdit.setBackground(new Color(240, 173, 78));
                    btnEdit.setForeground(Color.WHITE);
                    btnEdit.setPreferredSize(new Dimension(50, 30));
                    btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    p.add(btnEdit);
                }
                return p;
            }
        });

        if (whiteBox != null && scroll != null) {
            whiteBox.add(scroll, BorderLayout.CENTER);
            whiteBox.revalidate();
            whiteBox.repaint();
        }

    }

    public void loadData() {
        try {
            listPromotions = apiClient.getAllPromotions();
            capNhatBang(listPromotions);
        } catch (ApiException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Không thể tải khuyến mãi", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void refresh() {
        applyPermissions();
        loadData();
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

    private void SearchProcessing() {
        String input = "";
        String type = "Tên";
        if (rbPhanTram.isSelected()) {
            type = "Phần trăm";
            input = txtSearch.getText().trim();
            if (!input.isEmpty() && !input.matches("^[0-9.]+$")) {
                JOptionPane.showMessageDialog(this, "Phần trăm phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else if (rbThoiGian.isSelected()) {
            type = "Thời gian";
            Date minDate = dchDateMin.getDate();
            Date maxDate = dchDateMax.getDate();
            LocalDate minLocal = minDate != null ? toLocalDate(minDate) : null;
            LocalDate maxLocal = maxDate != null ? toLocalDate(maxDate) : null;
            if (minLocal != null && maxLocal != null && minLocal.isAfter(maxLocal)) {
                JOptionPane.showMessageDialog(this, "Ngày bắt đầu (Từ ngày) không được lớn hơn ngày kết thúc (Đến ngày)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Date normMin = minLocal != null ? toStartOfDay(minLocal) : null;
            Date normMax = maxLocal != null ? toEndOfDay(maxLocal) : null;
            if ((normMin != null && AppConstant.isDateOutOfBounds(normMin))
                    || (normMax != null && AppConstant.isDateOutOfBounds(normMax))) {
                JOptionPane.showMessageDialog(this, "Khoảng ngày tìm kiếm phải nằm trong khoảng từ 01/01/2026 đến 31/12/2099!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String startStr = minLocal != null ? minLocal.format(dtf) : "";
            String endStr = maxLocal != null ? maxLocal.format(dtf) : "";
            input = startStr + "|" + endStr;
        } else {
            input = txtSearch.getText().trim();
        }

        if (input.isEmpty() || input.equals("|")) {
            if (hasSearched) {
                loadData();
                hasSearched = false;
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập dữ liệu tìm kiếm!", "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
            }
            return;
        }

        hasSearched = true;
        try {
            listHienThi = apiClient.searchPromotions(input, type);
            capNhatBang(listHienThi);
        } catch (ApiException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Không thể tìm khuyến mãi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void capNhatBang(List<PromotionDTO> list) {
        model.setRowCount(0);
        if (list == null) {
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        for (PromotionDTO p : list) {
            String trangThaiStr = (p.getStatus() == 1) ? "Đang chạy" : "Ngừng hoạt động";
            String startStr = p.getStartDate() != null ? sdf.format(p.getStartDate()) : "";
            String endStr = p.getEndDate() != null ? sdf.format(p.getEndDate()) : "";

            model.addRow(new Object[] {
                    "KM" + p.getPromotionId(),
                    p.getPromotionName(),
                    p.getPercent() + "%",
                    startStr,
                    endStr,
                    trangThaiStr,
                    ""
            });
        }
    }

    private void setupTextFieldStyle(JTextField field, String placeholder) {
        field.setOpaque(false);
        field.setBackground(new Color(0, 0, 0, 0));
        field.setBorder(null);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        field.addActionListener(e -> SearchProcessing());
        field.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                field.repaint();
            }
        });

        field.setUI(new javax.swing.plaf.basic.BasicTextFieldUI() {
            @Override
            protected void paintSafely(Graphics g) {
                super.paintSafely(g);
                if (field.getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(150, 150, 150));
                    g2.setFont(field.getFont().deriveFont(Font.ITALIC));

                    FontMetrics fm = g2.getFontMetrics();
                    int y = (field.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(placeholder, 5, y);
                    g2.dispose();
                }
            }
        });
    }
}
