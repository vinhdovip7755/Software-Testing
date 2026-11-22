package com.bookstore.gui.panel.AccountTab;

import javax.swing.*;
import javax.swing.table.*;
import java.util.List;

import com.bookstore.api.client.AccountApiClient;
import com.bookstore.api.client.ApiException;
import com.bookstore.api.contract.AccountViewDTO;
import com.bookstore.api.contract.AdminAccountUpdateRequestDTO;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.util.PermissionUtil;
import com.bookstore.util.Refreshable;
import com.bookstore.util.SharedData;

import java.awt.*;

public class AccountPanel extends JPanel implements Refreshable {
    private List<AccountViewDTO> listAccountGoc;
    private DefaultTableModel model;
    private JTable table;
    private JTextField txtSearch;
    private JButton btnAdd;
    private final AccountApiClient accountApiClient = new AccountApiClient();
    private DefaultTableModel adminModel;
    private String currentAdminUsername = "";

    public AccountPanel() {
        setLayout(new GridBagLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;

        WhiteBoxPanel box1 = new WhiteBoxPanel();
        box1.setLayout(new BorderLayout());
        initAdminSection(box1);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.25;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 20, 0);
        add(box1, gbc);

        WhiteBoxPanel box2 = new WhiteBoxPanel();
        box2.setLayout(new BorderLayout(0, 15));
        initTableSection(box2);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 0.75;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        add(box2, gbc);

        reloadAccounts();
    }

    @Override
    public void refresh() {
        applyPermissions();
        reloadAccounts();
    }

    private void applyPermissions() {
        boolean canManageAccount = PermissionUtil.hasActionPermission("MANAGE_ACCOUNT");
        if (btnAdd != null) {
            btnAdd.setVisible(canManageAccount);
            btnAdd.setEnabled(canManageAccount);
        }
        if (table != null && table.getColumnModel().getColumnCount() > 5) {
            TableColumn actionCol = table.getColumnModel().getColumn(5);
            if (canManageAccount) {
                actionCol.setMinWidth(60);
                actionCol.setMaxWidth(140);
                actionCol.setPreferredWidth(90);
            } else {
                actionCol.setMinWidth(0);
                actionCol.setMaxWidth(0);
                actionCol.setPreferredWidth(0);
            }
        }
    }

    private Frame getOwnerFrame() {
        Window window = SwingUtilities.getWindowAncestor(this);
        return (window instanceof Frame frame) ? frame : null;
    }

    private void initAdminSection(WhiteBoxPanel box) {
        JPanel adminPanel = new JPanel(new BorderLayout(0, 10));
        adminPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(17, 71, 50)),
                        "Tài khoản Admin", 0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(17, 71, 50)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        adminPanel.setOpaque(false);

        String[] cols = { "Email", "Password" };
        adminModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable adminTable = new JTable(adminModel);
        adminTable.setRowHeight(40);
        adminTable.setShowGrid(true);
        adminTable.setGridColor(Color.GRAY);
        adminTable.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = adminTable.getTableHeader();
        header.setBackground(Color.BLACK);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));


        adminTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setBackground(Color.WHITE);
                l.setForeground(Color.BLACK);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.GRAY));
                return l;
            }
        });

        JButton btnEdit = new JButton("Sửa tài khoản");
        btnEdit.setBackground(new Color(17, 71, 50));
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setFocusPainted(false);
        btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnEdit.addActionListener(e -> showEditAdminDialog());

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnP.setOpaque(false);
        

        JScrollPane scrollPane = new JScrollPane(adminTable);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY));
        scrollPane.setPreferredSize(new Dimension(0, 60));

        adminPanel.add(scrollPane, BorderLayout.CENTER);
        adminPanel.add(btnP, BorderLayout.SOUTH);

        box.add(adminPanel, BorderLayout.NORTH);
    }

    private void initTableSection(WhiteBoxPanel box) {
        box.removeAll();
        box.setLayout(new BorderLayout(0, 15));

        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblSearch.setForeground(new Color(17, 71, 50));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 20, 0, 15);
        searchPanel.add(lblSearch, gbc);

        txtSearch = new RoundedTextField(20, 15);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setPreferredSize(new Dimension(txtSearch.getPreferredSize().width, 35));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 20);
        searchPanel.add(txtSearch, gbc);

        btnAdd = new JButton("Thêm tài khoản");
        btnAdd.setBackground(new Color(17, 71, 50));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setPreferredSize(new Dimension(140, 35));
        btnAdd.setFocusPainted(false);

        btnAdd.addActionListener(e -> {
            if (!PermissionUtil.hasActionPermission("MANAGE_ACCOUNT")) {
                return;
            }
            AccountFormDialog dialog = new AccountFormDialog(
                    getOwnerFrame(), this, null);
            dialog.setDataToForm(null, null);
            dialog.setVisible(true);
            reloadAccounts();
        });

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(btnAdd, gbc);

        box.add(searchPanel, BorderLayout.NORTH);

        String[] cols = { "Tên nhân viên", "Email", "Password", "Chức vụ", "Trạng thái", "Hành động" };
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(40);
        table.getTableHeader().setDefaultRenderer(new CustomHeaderRenderer());
        table.getTableHeader().setPreferredSize(new Dimension(table.getWidth(), 35));
        table.getTableHeader().setUI(new javax.swing.plaf.basic.BasicTableHeaderUI());
        table.setSelectionBackground(new Color(190, 210, 190));
        table.setSelectionForeground(Color.BLACK);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSelected, boolean hasFocus,
                                                           int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isSelected, hasFocus, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                if (isSelected) {
                    l.setBackground(t.getSelectionBackground());
                    l.setForeground(t.getSelectionForeground());
                } else {
                    l.setBackground(Color.WHITE);
                    l.setForeground(Color.BLACK);
                }
                l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(220, 220, 220)));
                return l;
            }
        };

        for (int i = 0; i < table.getColumnCount() - 1; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        table.getColumnModel().getColumn(5).setCellRenderer(new ActionRenderer());

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!PermissionUtil.hasActionPermission("MANAGE_ACCOUNT")) {
                    return;
                }
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col >= 0 && table.convertColumnIndexToModel(col) == 5) {
                    openEditDialog(row);
                }
            }
        });

        applyPermissions();

        txtSearch.addActionListener(e -> filterAccountTable());
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterAccountTable();
            }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterAccountTable();
            }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterAccountTable();
            }
        });

        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Tài khoản Nhân viên");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.BLACK);
        tablePanel.add(lblTitle, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(17, 71, 50), 1));
        tablePanel.add(scroll, BorderLayout.CENTER);

        box.add(tablePanel, BorderLayout.CENTER);

        box.revalidate();
        box.repaint();
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

            super.paintComponent(g);
        }
    }

    class RoundedTextField extends JTextField {
        private int radius;

        public RoundedTextField(int columns, int radius) {
            super(columns);
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            super.paintComponent(g2);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Color.GRAY);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            g2.dispose();
        }
    }

    class CustomHeaderRenderer extends DefaultTableCellRenderer {
        public CustomHeaderRenderer() {
            setOpaque(true);
            setBackground(new Color(17, 71, 50));
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.GRAY));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBackground(new Color(17, 71, 50));
            setForeground(Color.WHITE);
            return this;
        }
    }

    class ActionRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setOpaque(true);
            panel.setBackground(s ? t.getSelectionBackground() : Color.WHITE);
            panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(220, 220, 220)));

            JButton btn = new JButton("Sửa");
            btn.setBackground(new Color(240, 173, 78));
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btn.setPreferredSize(new Dimension(50, 25));
            btn.setFocusPainted(false);

            panel.add(btn);
            return panel;
        }
    }

    private void showEditAdminDialog() {
        if (!PermissionUtil.hasActionPermission("MANAGE_ACCOUNT")
                || SharedData.currentUser == null
                || SharedData.currentUser.getRoleId() != 1) {
            return;
        }
        JDialog dialog = new JDialog(getOwnerFrame(), "Sửa Admin", true);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField txtUser = new JTextField(currentAdminUsername);
        JPasswordField txtPass = new JPasswordField();

        form.add(new JLabel("Username:"));
        form.add(txtUser);
        form.add(new JLabel("Password:"));
        form.add(txtPass);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");

        btnCancel.addActionListener(e -> dialog.dispose());
        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        btnSave.addActionListener(e -> {
            try {
                String message = accountApiClient.updateAdminAccount(new AdminAccountUpdateRequestDTO(
                        currentAdminUsername,
                        txtUser.getText().trim(),
                        new String(txtPass.getPassword()).trim()
                ));
                JOptionPane.showMessageDialog(dialog, message);
                dialog.dispose();
                reloadAccounts();
            } catch (ApiException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Lỗi API", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void reloadAccounts() {
        try {
            listAccountGoc = accountApiClient.getAccounts();
            updateAdminTable(listAccountGoc);
            filterAccountTable();
        } catch (ApiException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi API", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateAdminTable(List<AccountViewDTO> accounts) {
        adminModel.setRowCount(0);
        boolean foundAdmin = false;
        if (accounts != null) {
            for (AccountViewDTO account : accounts) {
                if (account.roleId() == 1) {
                    currentAdminUsername = account.username();
                    adminModel.addRow(new Object[] { account.username(), "********" });
                    foundAdmin = true;
                    break;
                }
            }
        }
        if (!foundAdmin) {
            currentAdminUsername = "";
            adminModel.addRow(new Object[] { "Không tìm thấy", "N/A" });
        }
    }

    public void loadAccountData() {
        reloadAccounts();
    }

    private void filterAccountTable() {
        model.setRowCount(0);
        if (listAccountGoc == null) {
            return;
        }
        String keyword = txtSearch.getText().toLowerCase().trim();

        for (AccountViewDTO account : listAccountGoc) {
            if (account.roleId() == 1) {
                continue;
            }

            String statusStr = account.status() == 1 ? "Được phép hoạt động" : "Ngưng hoạt động";
            if (!keyword.isEmpty()) {
                String kwClean = removeDiacritics(keyword);
                boolean matches = (account.employeeName() != null
                        && (account.employeeName().toLowerCase().contains(keyword)
                        || removeDiacritics(account.employeeName()).contains(kwClean)))
                        || (account.username() != null && account.username().toLowerCase().contains(keyword))
                        || (account.roleName() != null
                        && (account.roleName().toLowerCase().contains(keyword)
                        || removeDiacritics(account.roleName()).contains(kwClean)))
                        || statusStr.toLowerCase().contains(keyword)
                        || removeDiacritics(statusStr).contains(kwClean);
                if (!matches) {
                    continue;
                }
            }

            model.addRow(new Object[] {
                    account.employeeName(),
                    account.username(),
                    "********",
                    account.roleName(),
                    statusStr,
                    "Sửa"
            });
        }
    }

    private void openEditDialog(int row) {
        if (!PermissionUtil.hasActionPermission("MANAGE_ACCOUNT")) {
            return;
        }
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        if (listAccountGoc == null || row < 0) {
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= model.getRowCount()) {
            return;
        }
        String user = model.getValueAt(modelRow, 1).toString();
        AccountViewDTO account = listAccountGoc.stream()
                .filter(item -> item.username().equals(user))
                .findFirst()
                .orElse(null);
        if (account == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy tài khoản đã chọn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        EmployeeDTO employee = new EmployeeDTO();
        employee.setEmployeeId(account.employeeId());
        employee.setEmployeeName(account.employeeName());
        employee.setRoleId(account.roleId());
        employee.setRoleName(account.roleName());

        AccountFormDialog dialog = new AccountFormDialog(getOwnerFrame(), this, account);
        dialog.setDataToForm(account, employee);
        dialog.setVisible(true);
        reloadAccounts();
    }

    private String removeDiacritics(String str) {
        if (str == null) return "";
        String nfd = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfd).replaceAll("").replace('đ', 'd').replace('Đ', 'D').toLowerCase();
    }
}
