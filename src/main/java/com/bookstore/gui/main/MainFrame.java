    package com.bookstore.gui.main;

    import com.bookstore.api.client.ApiException;
    import com.bookstore.api.client.AuthApiClient;
    import com.bookstore.gui.panel.StatisticTab.StatisticsTabbedPane;
    import com.bookstore.gui.panel.AccountTab.AccountTabbedPane;
    import com.bookstore.gui.panel.AccountTab.ProfilePanel;
    import com.bookstore.gui.panel.BillTab.BillPanel;
    import com.bookstore.gui.panel.EmployeeTab.EmployeePanel;
    import com.bookstore.gui.panel.ImportTab.ImportTabbedPane;
    import com.bookstore.gui.panel.InventoryTab.InventoryPanel;
    import com.bookstore.gui.panel.PriceTab.PriceTabbedPane;
    import com.bookstore.gui.panel.ProductTab.ProductTabbedPane;
    import com.bookstore.gui.panel.SellingTab.SellingTabbedPane;
    import com.bookstore.util.AppConstant;
    import com.bookstore.util.PermissionUtil;
    import com.bookstore.util.Refreshable;
    import com.bookstore.util.SharedData;
    import com.formdev.flatlaf.FlatClientProperties;
    import com.formdev.flatlaf.extras.FlatSVGIcon;
    import javax.swing.*;
    import java.awt.*;
    import java.util.HashMap;
    import java.util.LinkedHashMap;
    import java.util.Map;
    import java.util.function.BooleanSupplier;
    import java.util.function.Supplier;

    public class MainFrame extends JFrame {
        private record TabSpec(BooleanSupplier allowed, Supplier<Component> factory) {}

        private CardLayout cardLayout;
        private JPanel mainContentPanel, sidebarPanel;
        private JButton btnLogout, btnSelling, btnProduct, btnPrice, btnImport, btnInventory, btnBill, btnEmployee, btnStats, btnAccount;
        private JPanel welcomePanel;
        private final Map<JButton, String> menuMap = new LinkedHashMap<>();
        private final Map<String, TabSpec> tabSpecs = new HashMap<>();
        private final Map<String, Component> createdTabs = new HashMap<>();
        private JLabel lbUserInfo;
        private JLabel lbWelcome2;
        private static MainFrame instance;

        public static MainFrame getInstance() {
            return instance;
        }

        public MainFrame() {
            instance = this;
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

            registerTabs();

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

            add(mainContentPanel, BorderLayout.CENTER);
            cardLayout.show(mainContentPanel, "WELCOME");
        }

        private void registerTabs() {
            registerTab("SELLING", () -> hasAnyViewPermission("MANAGE_SELLING", "MANAGE_CUSTOMER"), SellingTabbedPane::new);
            registerTab("PRODUCT", () -> hasAnyViewPermission("MANAGE_PRODUCT", "MANAGE_AUTHOR", "MANAGE_CATEGORY", "MANAGE_SUPPLIER"), ProductTabbedPane::new);
            registerTab("PRICE", () -> hasAnyViewPermission("MANAGE_PRICE", "MANAGE_PROMOTION"), PriceTabbedPane::new);
            registerTab("IMPORT", () -> hasAnyViewPermission("MANAGE_IMPORT_TICKET", "MANAGE_IMPORT"), ImportTabbedPane::new);
            registerTab("INVENTORY", () -> hasAnyViewPermission("MANAGE_INVENTORY"), InventoryPanel::new);
            registerTab("BILL", () -> hasAnyViewPermission("MANAGE_BILL"), BillPanel::new);
            registerTab("EMPLOYEE", () -> hasAnyViewPermission("MANAGE_EMPLOYEE"), EmployeePanel::new);
            registerTab("STATS", () -> hasAnyViewPermission("STATISTICS"), StatisticsTabbedPane::new);
            registerTab("ACCOUNT", () -> SharedData.currentUser != null, AccountTabbedPane::new);
            registerTab("PROFILE", () -> SharedData.currentUser != null, ProfilePanel::new);
        }

        private void registerTab(String tabName, BooleanSupplier allowed, Supplier<Component> factory) {
            tabSpecs.put(tabName, new TabSpec(allowed, factory));
        }

        private static boolean hasAnyViewPermission(String... actionCodes) {
            for (String code : actionCodes) {
                if (PermissionUtil.hasViewPermission(code)) {
                    return true;
                }
            }
            return false;
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

            String username = SharedData.currentUser != null ? SharedData.currentUser.getEmployeeName() : "";
            lbWelcome2 = new JLabel("Xin chào, " + username + "!");
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

            String currentUserName = SharedData.currentUser != null ? SharedData.currentUser.getEmployeeName() : "";
            lbUserInfo = new JLabel("<html><div style='text-align: center; width: 120px;'>Chào: " + currentUserName + "</div></html>");
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

            bindMenuButton(btnSelling, "SELLING");
            bindMenuButton(btnProduct, "PRODUCT");
            bindMenuButton(btnPrice, "PRICE");
            bindMenuButton(btnImport, "IMPORT");
            bindMenuButton(btnInventory, "INVENTORY");
            bindMenuButton(btnBill, "BILL");
            bindMenuButton(btnEmployee, "EMPLOYEE");
            bindMenuButton(btnStats, "STATS");
            bindMenuButton(btnAccount, "ACCOUNT");

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

            btnLogout.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Bạn có chắc chắn muốn đăng xuất?",
                        "Xác nhận đăng xuất", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        new AuthApiClient().logout();
                    } catch (ApiException apiException) {
                        JOptionPane.showMessageDialog(this,
                                "Đã đăng xuất khỏi ứng dụng nhưng không thể thu hồi phiên API: " + apiException.getMessage(),
                                "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    }
                    SharedData.currentUser = null;
                    SharedData.userPermissions = Map.of();
                    clearContentPanels();
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

        private void bindMenuButton(JButton btn, String tabKey) {
            menuMap.put(btn, tabKey);
            btn.addActionListener(e -> switchTab(tabKey));
        }

        private void clearContentPanels() {
            if (mainContentPanel != null) {
                mainContentPanel.removeAll();
            }
            createdTabs.clear();
        }

        @Override
        public void dispose() {
            clearContentPanels();
            if (instance == this) {
                instance = null;
            }
            super.dispose();
        }

        private void switchTab(String tabName) {
            if ("WELCOME".equals(tabName)) {
                cardLayout.show(mainContentPanel, "WELCOME");
                return;
            }

            TabSpec spec = tabSpecs.get(tabName);
            if (spec == null || !spec.allowed().getAsBoolean()) {
                return;
            }

            Component comp = createdTabs.get(tabName);
            boolean newlyCreated = false;
            if (comp == null) {
                comp = spec.factory().get();
                if (comp == null) {
                    return;
                }
                createdTabs.put(tabName, comp);
                mainContentPanel.add(comp, tabName);
                newlyCreated = true;
            }

            cardLayout.show(mainContentPanel, tabName);
            mainContentPanel.revalidate();
            mainContentPanel.repaint();

            if (!newlyCreated) {
                if (comp instanceof Refreshable r) {
                    r.refresh();
                } else if (comp instanceof JTabbedPane tabbedPane) {
                    Component selectedTab = tabbedPane.getSelectedComponent();
                    if (selectedTab instanceof Refreshable r) {
                        r.refresh();
                    }
                }
            }
        }

        private void applyMenuPermissions() {
            for (Map.Entry<JButton, String> entry : menuMap.entrySet()) {
                TabSpec spec = tabSpecs.get(entry.getValue());
                boolean isVisible = spec != null && spec.allowed().getAsBoolean();
                entry.getKey().setVisible(isVisible);
            }
        }

        public void updateUserInfo() {
            if (SharedData.currentUser != null) {
                String currentUserName = SharedData.currentUser.getEmployeeName();
                if (lbUserInfo != null) {
                    lbUserInfo.setText("<html><div style='text-align: center; width: 120px;'>Chào: " + currentUserName + "</div></html>");
                }
                if (lbWelcome2 != null) {
                    lbWelcome2.setText("Xin chào, " + currentUserName + "!");
                }
            }
        }
    }