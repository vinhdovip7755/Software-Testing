    package com.bookstore.gui.main;

    import com.bookstore.dto.PermissionDTO;
    import com.bookstore.gui.panel.StatisticTab.StatisticsTabbedPane;
    import com.bookstore.gui.panel.AccountTab.AccountTabbedPane;
    import com.bookstore.gui.panel.BillTab.BillPanel;
    import com.bookstore.gui.panel.EmployeeTab.EmployeePanel;
    import com.bookstore.gui.panel.ImportTab.ImportTabbedPane;
    import com.bookstore.gui.panel.InventoryTab.InventoryPanel;
    import com.bookstore.gui.panel.PriceTab.PriceTabbedPane;
    import com.bookstore.gui.panel.ProductTab.ProductTabbedPane;
    import com.bookstore.gui.panel.SellingTab.SellingTabbedPane;
    import com.bookstore.util.AppConstant;
    import com.bookstore.util.Refreshable;
    import com.bookstore.util.SharedData;
    import com.formdev.flatlaf.FlatClientProperties;
    import com.formdev.flatlaf.extras.FlatSVGIcon;
    import javax.swing.*;
    import java.awt.*;
    import java.util.HashMap;
    import java.util.Map;

    public class MainFrame extends JFrame {
        private CardLayout cardLayout;
        private JPanel mainContentPanel, sidebarPanel;
        private JButton btnLogout, btnSelling, btnProduct, btnPrice, btnImport, btnInventory, btnBill, btnEmployee, btnStats, btnAccount;
        private JPanel welcomePanel;
        private Map<JButton, String[]> menuMap = new HashMap<>();

        public MainFrame() {
            initUI();
            applyMenuPermissions();
        }

        private void initUI() {
            setTitle("Ứng Dụng Quản Lý Bán Sách");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(1300, 800);
            setMinimumSize(new Dimension(1100, 700));
            setLocationRelativeTo(null);
            setLayout(new BorderLayout());

            sidebarPanel = createSidebar();
            JScrollPane sidebarScrollPane = new JScrollPane(sidebarPanel);
            sidebarScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            sidebarScrollPane.setBorder(null);
            sidebarScrollPane.getVerticalScrollBar().setUnitIncrement(20);
            add(sidebarScrollPane, BorderLayout.WEST);

            cardLayout = new CardLayout();
            mainContentPanel = new JPanel(cardLayout);
            mainContentPanel.setBackground(Color.WHITE);

            welcomePanel = createWelcomePanel();
            mainContentPanel.add(welcomePanel, "WELCOME");
            mainContentPanel.add(new SellingTabbedPane(), "SELLING");
            mainContentPanel.add(new ProductTabbedPane(), "PRODUCT");
            mainContentPanel.add(new PriceTabbedPane(), "PRICE");
            mainContentPanel.add(new ImportTabbedPane(), "IMPORT");
            mainContentPanel.add(new InventoryPanel(), "INVENTORY");
            mainContentPanel.add(new BillPanel(), "BILL");
            mainContentPanel.add(new EmployeePanel(), "EMPLOYEE");
            mainContentPanel.add(new StatisticsTabbedPane(), "STATS");
            mainContentPanel.add(new AccountTabbedPane(), "ACCOUNT");
        mainContentPanel.add(new com.bookstore.gui.panel.AccountTab.ProfilePanel(), "PROFILE");

            add(mainContentPanel, BorderLayout.CENTER);
            cardLayout.show(mainContentPanel, "WELCOME");
        }

        private JPanel createWelcomePanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(Color.WHITE);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(10, 0, 20, 0);

            JLabel lbWelcome1 = new JLabel("Chào Mừng Trở Lại Hệ Thống");
            lbWelcome1.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 40));
            lbWelcome1.setForeground(Color.decode(AppConstant.GREEN_COLOR_CODE));
            panel.add(lbWelcome1, gbc);

            gbc.gridy++;

            String username = SharedData.currentUser.getEmployeeName();
            JLabel lbWelcome2 = new JLabel("Xin chào, " + username + "!");
            lbWelcome2.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 28));
            lbWelcome2.setForeground(Color.GRAY);
            panel.add(lbWelcome2, gbc);
            return panel;
        }

        private JPanel createSidebar() {
            JPanel sidebar = new JPanel() {
                public Dimension getPreferredSize() {
                    return new Dimension(180, super.getPreferredSize().height);
                }
            };
            sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
            sidebar.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
            sidebar.setBorder(BorderFactory.createEmptyBorder(20, 5, 20, 5));

            JLabel lbHeader1 = new JLabel("Quản Lý");
            lbHeader1.setForeground(Color.WHITE);
            lbHeader1.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 30));
            lbHeader1.setAlignmentX(Component.CENTER_ALIGNMENT);
            lbHeader1.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel lbHeader2 = new JLabel("Bán Sách");
            lbHeader2.setForeground(Color.WHITE);
            lbHeader2.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 30));
            lbHeader2.setAlignmentX(Component.CENTER_ALIGNMENT);
            lbHeader2.setHorizontalAlignment(SwingConstants.CENTER);

            sidebar.add(lbHeader1);
            sidebar.add(lbHeader2);

            sidebar.add(Box.createVerticalStrut(10));

            String currentUserName = SharedData.currentUser.getEmployeeName();
            JLabel lbUserInfo = new JLabel("<html><div style='text-align: center; width: 120px;'>Chào: " + currentUserName + "</div></html>");
            lbUserInfo.setForeground(new Color(255, 255, 204));
            lbUserInfo.setFont(new Font(AppConstant.FONT_NAME, Font.ITALIC, 15));
            lbUserInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
            lbUserInfo.setHorizontalAlignment(SwingConstants.CENTER);
                    lbUserInfo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lbUserInfo.setToolTipText("Nhấn để xem/sửa Hồ sơ cá nhân");
        lbUserInfo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                switchTab("PROFILE");
            }
        });
        sidebar.add(lbUserInfo);
            sidebar.add(Box.createVerticalStrut(20));

            JSeparator separator1 = new JSeparator();
            separator1.setMaximumSize(new Dimension(160, 1));
            separator1.setForeground(Color.GRAY);
            separator1.setAlignmentX(Component.CENTER_ALIGNMENT);
            sidebar.add(separator1);

            btnSelling = createMenuButton("Bán Hàng", "cart_icon.svg");
            btnProduct = createMenuButton("Sản Phẩm", "book_icon.svg");
            btnPrice = createMenuButton("Giá Bán", "coin_icon.svg");
            btnImport = createMenuButton("Phiếu Nhập", "import_icon.svg");
            btnInventory = createMenuButton("Tồn Kho", "inventory_icon.svg");
            btnBill = createMenuButton("Hóa Đơn", "bill_icon.svg");
            btnEmployee = createMenuButton("Nhân Viên", "employee_icon.svg");
            btnStats = createMenuButton("Thống Kê", "stats_icon.svg");
            btnAccount = createMenuButton("Tài Khoản", "account_icon.svg");

            menuMap.put(btnSelling, new String[]{"MANAGE_SELLING", "MANAGE_CUSTOMER"});
            menuMap.put(btnProduct, new String[]{"MANAGE_PRODUCT", "MANAGE_AUTHOR", "MANAGE_CATEGORY", "MANAGE_SUPPLIER"});
            menuMap.put(btnPrice, new String[]{"MANAGE_PRICE", "MANAGE_PROMOTION"});
            menuMap.put(btnImport, new String[]{"MANAGE_IMPORT_TICKET", "MANAGE_IMPORT"});
            menuMap.put(btnInventory, new String[]{"MANAGE_INVENTORY"});
            menuMap.put(btnBill, new String[]{"MANAGE_BILL"});
            menuMap.put(btnEmployee, new String[]{"MANAGE_EMPLOYEE"});
            menuMap.put(btnStats, new String[]{"STATISTICS"});
            

            btnLogout = new JButton("Đăng xuất");
            btnLogout.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 16));
            btnLogout.setForeground(Color.WHITE);
            btnLogout.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
            btnLogout.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            btnLogout.setMaximumSize(new Dimension(200, 70));
            btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
            try {
                FlatSVGIcon svgIcon = new FlatSVGIcon("icon/logout_icon.svg");
                FlatSVGIcon.ColorFilter colorFilter = new FlatSVGIcon.ColorFilter(color -> Color.WHITE);
                svgIcon.setColorFilter(colorFilter);
                btnLogout.setIcon(svgIcon.derive(24, 24));
                btnLogout.setIconTextGap(15);

            } catch (Exception e) {
                System.err.println("Lỗi load icon SVG: logout_icon.svg");
            }
            btnLogout.setHorizontalAlignment(SwingConstants.LEFT);
            btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnLogout.setFocusPainted(false);
            btnLogout.putClientProperty(FlatClientProperties.STYLE,
                    "hoverBackground: #ff4f4f;"
            );

            btnSelling.addActionListener(e -> switchTab("SELLING"));
            btnProduct.addActionListener(e -> switchTab("PRODUCT"));
            btnPrice.addActionListener(e -> switchTab("PRICE"));
            btnImport.addActionListener(e -> switchTab("IMPORT"));
            btnInventory.addActionListener(e -> switchTab("INVENTORY"));
            btnBill.addActionListener(e -> switchTab("BILL"));
            btnEmployee.addActionListener(e -> switchTab("EMPLOYEE"));
            btnStats.addActionListener(e -> switchTab("STATS"));
            btnAccount.addActionListener(e -> switchTab("ACCOUNT"));
            btnLogout.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Bạn có chắc chắn muốn đăng xuất?",
                        "Xác nhận đăng xuất", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    this.dispose();
                    new LoginFrame().setVisible(true);
                }
            });

            sidebar.add(btnSelling);
            sidebar.add(Box.createVerticalStrut(10));
            sidebar.add(btnProduct);
            sidebar.add(Box.createVerticalStrut(10));
            sidebar.add(btnPrice);
            sidebar.add(Box.createVerticalStrut(10));
            sidebar.add(btnImport);
            sidebar.add(Box.createVerticalStrut(10));
            sidebar.add(btnInventory);
            sidebar.add(Box.createVerticalStrut(10));
            sidebar.add(btnBill);
            sidebar.add(Box.createVerticalStrut(10));
            sidebar.add(btnEmployee);
            sidebar.add(Box.createVerticalStrut(10));
            sidebar.add(btnStats);
            sidebar.add(Box.createVerticalStrut(10));
            sidebar.add(btnAccount);

            sidebar.add(Box.createVerticalGlue());
            JSeparator separator2 = new JSeparator();
            separator2.setMaximumSize(new Dimension(160, 1));
            separator2.setForeground(Color.GRAY);
            separator2.setAlignmentX(Component.CENTER_ALIGNMENT);
            sidebar.add(separator2);
            sidebar.add(btnLogout);

            return sidebar;
        }

        private JButton createMenuButton(String text, String iconName) {
            JButton btn = new JButton(text);
            btn.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 15));
            btn.setForeground(Color.WHITE);
            btn.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
            btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            btn.setMaximumSize(new Dimension(200, 70));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            try {
                FlatSVGIcon svgIcon = new FlatSVGIcon("icon/" + iconName);
                FlatSVGIcon.ColorFilter colorFilter = new FlatSVGIcon.ColorFilter(color -> Color.WHITE);
                svgIcon.setColorFilter(colorFilter);
                btn.setIcon(svgIcon.derive(24, 24));
                btn.setIconTextGap(15);

            } catch (Exception e) {
                System.err.println("Lỗi load icon SVG: " + iconName);
            }
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setFocusPainted(false);

            btn.putClientProperty(FlatClientProperties.STYLE,
                    "hoverBackground: #00A364;"
            );
            return btn;
        }

        private void switchTab(String tabName) {
            cardLayout.show(mainContentPanel, tabName);

            for (Component comp : mainContentPanel.getComponents()) {
                if (comp.isVisible()) {
                    if (comp instanceof Refreshable r) {
                        r.refresh();
                    }
                    else if (comp instanceof JTabbedPane tabbedPane) {
                        Component selectedTab = tabbedPane.getSelectedComponent();
                        if (selectedTab instanceof Refreshable r) {
                            r.refresh();
                        }
                    }
                    break;
                }
            }
        }

        private void applyMenuPermissions() {
            for (Map.Entry<JButton, String[]> entry : menuMap.entrySet()) {
                JButton btn = entry.getKey();
                String[] actionCodes = entry.getValue();



                boolean isVisible = false;

                for (String code : actionCodes) {
                    PermissionDTO permission = SharedData.userPermissions.get(code);
                    if (permission != null && permission.isView()) {
                        isVisible = true;
                        break;
                    }
                }

                if (btn == btnAccount && SharedData.currentUser != null) {
                    isVisible = true;
                }
                btn.setVisible(isVisible);
            }
        }
    }