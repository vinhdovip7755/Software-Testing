package com.bookstore.gui.panel.PriceTab;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.List;

import com.bookstore.dto.*;
import com.bookstore.bus.*;

public class PromotionPanel extends JPanel {
    private JRadioButton rbTen, rbPhanTram, rbThoiGian;
    private List<PromotionDTO> listPromotions;
    private List<PromotionDTO> listHienThi;

    private PromotionBUS bus = new PromotionBUS();
    private JTable table;
    private JPanel cardPanel;
    private WhiteBoxPanel whiteBox;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private com.toedter.calendar.JDateChooser dchDateMin, dchDateMax;
    private JScrollPane scroll;
    private boolean hasSearched = false;
    private CardLayout searchCard = new CardLayout();

    public PromotionPanel() {

        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(235, 235, 235));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        initHeader();
        loadData();

        Timer statusTimer = new Timer(60000, e -> {
            bus.updateAutoStatus();
            capNhatBang(bus.selectAllPromotions());
        });
        statusTimer.start();
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

        JButton btnAdd = new JButton("+ Thêm Khuyến Mãi") {
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
        gbc.insets = new Insets(0, 0, 0, 60);
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

        add(whiteBox, BorderLayout.CENTER);


    }

    private JPanel createSearchWrapper() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

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
        setupTextFieldStyle(txtSearch, "Tìm kiếm chương trình...");
        cardPanel.add(txtSearch, "NORMAL");

        JPanel dateGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        dateGroup.setOpaque(false);
        dateGroup.setBorder(null);

        dchDateMin = new com.toedter.calendar.JDateChooser();
        dchDateMax = new com.toedter.calendar.JDateChooser();
        dchDateMin.setDateFormatString("yyyy-MM-dd");
        dchDateMax.setDateFormatString("yyyy-MM-dd");
        java.awt.Dimension dateSize = new java.awt.Dimension(130, 30);
        dchDateMin.setPreferredSize(dateSize);
        dchDateMax.setPreferredSize(dateSize);

        dateGroup.add(dchDateMin);
        dateGroup.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(20, 0)));
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

        wrapper.add(iconBox, BorderLayout.WEST);
        wrapper.add(inputBackground, BorderLayout.CENTER);
        return wrapper;
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
                if (column == 6 && row != -1) {

                    String idStr = table.getValueAt(row, 0).toString().substring(2);
                    int id = Integer.parseInt(idStr);

                    PromotionDTO p = null;
                    for (PromotionDTO item : listPromotions) {
                        if (item.getPromotionId() == id) {
                            p = item;
                            break;
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

                JButton btnEdit = new JButton("Sửa");

                btnEdit.setBackground(new Color(240, 173, 78));

                for (JButton b : new JButton[] { btnEdit }) {
                    b.setForeground(Color.WHITE);
                    b.setPreferredSize(new Dimension(50, 30));
                    b.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    p.add(b);
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
        listPromotions = bus.selectAllPromotions();
        capNhatBang(listPromotions);
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
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            String startStr = dchDateMin.getDate() != null ? sdf.format(dchDateMin.getDate()) : "";
            String endStr = dchDateMax.getDate() != null ? sdf.format(dchDateMax.getDate()) : "";
            input = startStr + "|" + endStr;
        } else {
            input = txtSearch.getText().trim();
        }

        if (input.isEmpty() || input.equals("|")) {
            if (hasSearched) {
                capNhatBang(bus.selectAllPromotions());
                hasSearched = false;
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập dữ liệu tìm kiếm!", "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
            }
            return;
        }

        hasSearched = true;
        listHienThi = bus.search(input, type);
        capNhatBang(listHienThi);
    }

    public void capNhatBang(List<PromotionDTO> list) {
        model.setRowCount(0);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (PromotionDTO p : list) {
            String trangThaiStr = (p.getStatus() == 1) ? "Đang chạy" : "Ngừng hoạt động";

            model.addRow(new Object[] {
                    "KM" + p.getPromotionId(),
                    p.getPromotionName(),
                    p.getPercent() + "%",
                    sdf.format(p.getStartDate()),
                    sdf.format(p.getEndDate()),
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
