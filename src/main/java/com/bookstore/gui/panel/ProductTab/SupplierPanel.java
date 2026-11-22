package com.bookstore.gui.panel.ProductTab;

import com.bookstore.api.client.ApiException;
import com.bookstore.api.client.BookApiClient;
import com.bookstore.api.client.SupplierApiClient;
import com.bookstore.dto.BookDTO;
import com.bookstore.dto.SupplierDTO;
import com.bookstore.util.AppConstant;
import com.bookstore.util.PermissionUtil;
import com.bookstore.util.Refreshable;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class SupplierPanel extends JPanel implements Refreshable {
    private static final Color MAIN_COLOR = Color.decode(AppConstant.GREEN_COLOR_CODE);
    private static final Color BUTTON_COLOR = Color.decode(AppConstant.BUTTON_COLOR);
    private static final Color BORDER_COLOR = Color.decode("#E0E0E0");

    private final SupplierApiClient supplierBUS;
    private final BookApiClient bookBUS;

    private JTextField searchField;
    private JTable supplierTable;
    private DefaultTableModel tableModel;

    private JTextField nameField;
    private JTextField addressField;
    private JTextField phoneField;
    private JButton saveButton;
    private JButton clearButton;
    private JLabel formTitleLabel;
    private JSplitPane splitPane;
    private JPanel rightPanel;

    private List<SupplierDTO> allSuppliers;
    private List<SupplierDTO> filteredSuppliers;
    private List<BookDTO> allBooks;
    private int selectedSupplierId = -1;

    public SupplierPanel() {
        supplierBUS = new SupplierApiClient();
        bookBUS = new BookApiClient();
        allSuppliers = new ArrayList<>();
        filteredSuppliers = new ArrayList<>();
        allBooks = new ArrayList<>();

        loadData();
        initUI();
        applyPermissions();
        applyFilter();
    }

    private void applyPermissions() {
        boolean hasAction = PermissionUtil.hasActionPermission("MANAGE_SUPPLIER");
        if (rightPanel != null) {
            rightPanel.setVisible(hasAction);
        }
        if (splitPane != null) {
            splitPane.setDividerSize(hasAction ? 8 : 0);
        }
        if (nameField != null) {
            nameField.setEditable(hasAction);
        }
        if (addressField != null) {
            addressField.setEditable(hasAction);
        }
        if (phoneField != null) {
            phoneField.setEditable(hasAction);
        }
        if (saveButton != null) {
            saveButton.setVisible(hasAction);
            saveButton.setEnabled(hasAction);
        }
        if (clearButton != null) {
            clearButton.setVisible(hasAction);
            clearButton.setEnabled(hasAction);
        }
        if (supplierTable != null) {
            if (!hasAction && supplierTable.isEditing()) {
                supplierTable.getCellEditor().cancelCellEditing();
            }
            supplierTable.repaint();
        }
    }

    @Override
    public void refresh() {
        applyPermissions();
        loadData();
        applyFilter();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setBorder(null);
        splitPane.setResizeWeight(0.7);
        splitPane.setDividerSize(8);

        rightPanel = createRightPanel();
        splitPane.setLeftComponent(createLeftPanel());
        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBorder(new EmptyBorder(20, 25, 20, 15));
        panel.setBackground(Color.WHITE);

        panel.add(createFilterPanel(), BorderLayout.NORTH);
        panel.add(createTablePanel(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFilterPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setBackground(Color.WHITE);

        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(Color.WHITE);

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(createInputBorder());
        searchField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSVGIcon("icon/search_icon.svg").derive(20, 20));
        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập từ khóa tìm kiếm...");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilter();
            }
        });
        searchPanel.add(searchField, BorderLayout.CENTER);

        wrapper.add(searchPanel, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));

        String[] columns = {"Tên nhà cung cấp", "Địa chỉ", "Số điện thoại", "Số lượng đầu sách", "Thao tác"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 && PermissionUtil.hasActionPermission("MANAGE_SUPPLIER");
            }
        };

        supplierTable = new JTable(tableModel);
        supplierTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        supplierTable.setRowHeight(38);
        supplierTable.setSelectionBackground(new Color(232, 245, 233));
        supplierTable.setSelectionForeground(Color.BLACK);
        supplierTable.setGridColor(BORDER_COLOR);
        supplierTable.setShowVerticalLines(true);
        supplierTable.setShowHorizontalLines(true);
        supplierTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int row = supplierTable.rowAtPoint(e.getPoint());
                    showSupplierDetailDialog(row);
                }
            }
        });

        JTableHeader header = supplierTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(MAIN_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);

        supplierTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonCellRenderer());
        supplierTable.getColumnModel().getColumn(4).setCellEditor(new ButtonCellEditor());
        supplierTable.getColumnModel().getColumn(0).setPreferredWidth(220);
        supplierTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        supplierTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        supplierTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        supplierTable.getColumnModel().getColumn(4).setPreferredWidth(90);

        JScrollPane scrollPane = new JScrollPane(supplierTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(20, 10, 20, 25));
        panel.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(18, 18, 18, 18)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 6, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;

        formTitleLabel = new JLabel("THÊM NHÀ CUNG CẤP");
        formTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        formTitleLabel.setForeground(MAIN_COLOR);

        gbc.gridy = 0;
        formPanel.add(formTitleLabel, gbc);

        gbc.gridy = 1;
        formPanel.add(createFormLabel("Tên nhà cung cấp *"), gbc);

        nameField = new JTextField();
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameField.setBorder(createInputBorder());
        gbc.gridy = 2;
        formPanel.add(nameField, gbc);

        gbc.gridy = 3;
        formPanel.add(createFormLabel("Địa chỉ"), gbc);

        addressField = new JTextField();
        addressField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addressField.setBorder(createInputBorder());
        gbc.gridy = 4;
        formPanel.add(addressField, gbc);

        gbc.gridy = 5;
        formPanel.add(createFormLabel("Số điện thoại *"), gbc);

        phoneField = new JTextField();
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        phoneField.setBorder(createInputBorder());
        gbc.gridy = 6;
        formPanel.add(phoneField, gbc);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        saveButton = createStyledButton("LƯU", BUTTON_COLOR);
        saveButton.addActionListener(e -> saveSupplier());
        buttonPanel.add(saveButton);

        clearButton = createStyledButton("LÀM MỚI", Color.decode("#757575"));
        clearButton.addActionListener(e -> resetForm());
        buttonPanel.add(clearButton);

        gbc.gridy = 7;
        gbc.insets = new Insets(18, 0, 0, 0);
        formPanel.add(buttonPanel, gbc);

        panel.add(formPanel, BorderLayout.NORTH);
        return panel;
    }

    private void loadData() {
        try {
            allSuppliers = supplierBUS.selectAll();
            allBooks = bookBUS.selectAllBooks();
        } catch (ApiException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Không thể tải dữ liệu", JOptionPane.ERROR_MESSAGE);
            allSuppliers = new ArrayList<>();
            allBooks = new ArrayList<>();
        }
    }

    private void applyFilter() {
        filteredSuppliers.clear();
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        for (SupplierDTO supplier : allSuppliers) {
            String supplierName = supplier.getSupplierName() == null ? "" : supplier.getSupplierName().toLowerCase();
            String address = supplier.getSupplierAddress() == null ? "" : supplier.getSupplierAddress().toLowerCase();
            String phone = supplier.getSupplierPhone() == null ? "" : supplier.getSupplierPhone().toLowerCase();

            if (supplierName.contains(keyword) || address.contains(keyword) || phone.contains(keyword)) {
                filteredSuppliers.add(supplier);
            }
        }

        reloadTable();
    }

    private void reloadTable() {
        tableModel.setRowCount(0);

        for (SupplierDTO supplier : filteredSuppliers) {
            tableModel.addRow(new Object[]{
                    supplier.getSupplierName(),
                    supplier.getSupplierAddress(),
                    supplier.getSupplierPhone(),
                    countBooksBySupplierId(supplier.getSupplierId()),
                    "Sửa"
            });
        }
    }

    private void saveSupplier() {
        if (!PermissionUtil.hasActionPermission("MANAGE_SUPPLIER")) {
            return;
        }
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên nhà cung cấp không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            phoneField.requestFocus();
            return;
        }

        if (!phone.matches(AppConstant.REGEX_SUPPLIER_PHONE)) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ! (Số di động 10 chữ số hoặc số bàn cố định 11 chữ số bắt đầu bằng số 0)", "Lỗi", JOptionPane.ERROR_MESSAGE);
            phoneField.requestFocus();
            return;
        }

        SupplierDTO supplierDTO = new SupplierDTO();
        supplierDTO.setSupplierName(name);
        supplierDTO.setSupplierAddress(address);
        supplierDTO.setSupplierPhone(phone);

        String message;
        try {
            if (selectedSupplierId == -1) {
                message = supplierBUS.addSupplier(supplierDTO);
            } else {
                SupplierDTO oldSupplier = getSelectedSupplier();
                if (oldSupplier != null) {
                    int affectedBooks = countBooksBySupplierId(oldSupplier.getSupplierId());
                    String confirmMessage = "Mọi sách đang dùng thông tin nhà cung cấp cũ sẽ đổi thành thông tin mới.\n\n"
                            + "Thông tin cũ:\n"
                            + "- Tên: " + oldSupplier.getSupplierName() + "\n"
                            + "- Địa chỉ: " + oldSupplier.getSupplierAddress() + "\n"
                            + "- SĐT: " + oldSupplier.getSupplierPhone() + "\n\n"
                            + "Thông tin mới:\n"
                            + "- Tên: " + supplierDTO.getSupplierName() + "\n"
                            + "- Địa chỉ: " + supplierDTO.getSupplierAddress() + "\n"
                            + "- SĐT: " + supplierDTO.getSupplierPhone() + "\n\n"
                            + "Số sách bị ảnh hưởng: " + affectedBooks + "\n"
                            + "Bạn có muốn tiếp tục Cập nhật không?";

                    int choice = JOptionPane.showConfirmDialog(
                            this,
                            confirmMessage,
                            "Xác nhận Cập nhật nhà cung cấp",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                    );

                    if (choice != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                supplierDTO.setSupplierId(selectedSupplierId);
                message = supplierBUS.updateSupplier(supplierDTO);
            }
        } catch (ApiException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Không thể lưu nhà cung cấp", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, message);
        if (message.toLowerCase().contains("thành công")) {
            loadData();
            applyFilter();
            resetForm();
        }
    }

    private int countBooksBySupplierId(int supplierId) {
        int count = 0;
        for (BookDTO book : allBooks) {
            if (book.getSupplierId() == supplierId) {
                count++;
            }
        }
        return count;
    }

    private SupplierDTO getSelectedSupplier() {
        for (SupplierDTO supplier : allSuppliers) {
            if (supplier.getSupplierId() == selectedSupplierId) {
                return supplier;
            }
        }
        return null;
    }

    private void showSupplierDetailDialog(int row) {
        if (row < 0 || row >= filteredSuppliers.size()) {
            return;
        }

        SupplierDTO supplier = filteredSuppliers.get(row);

        String detailMessage = "Tên nhà cung cấp: " + supplier.getSupplierName() + "\n"
                + "Địa chỉ: " + supplier.getSupplierAddress() + "\n"
                + "Số điện thoại: " + supplier.getSupplierPhone() + "\n";

        JOptionPane.showMessageDialog(
                this,
                detailMessage,
                "Chi tiết nhà cung cấp",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void selectSupplierForEdit(int row) {
        if (!PermissionUtil.hasActionPermission("MANAGE_SUPPLIER")) {
            return;
        }
        if (row < 0 || row >= filteredSuppliers.size()) {
            return;
        }

        SupplierDTO supplier = filteredSuppliers.get(row);
        selectedSupplierId = supplier.getSupplierId();
        nameField.setText(supplier.getSupplierName());
        addressField.setText(supplier.getSupplierAddress());
        phoneField.setText(supplier.getSupplierPhone());
        formTitleLabel.setText("CHỈNH SỬA NHÀ CUNG CẤP");
        saveButton.setText("Cập nhật");
    }

    private void resetForm() {
        selectedSupplierId = -1;
        nameField.setText("");
        addressField.setText("");
        phoneField.setText("");
        formTitleLabel.setText("THÊM NHÀ CUNG CẤP");
        saveButton.setText("LƯU");
        supplierTable.clearSelection();
    }

    private JLabel createFormLabel(String text) {
        JLabel label;
        if (text.endsWith("*")) {
            String baseText = text.substring(0, text.length() - 1).trim();
            label = new JLabel("<html>" + baseText + " <font color='#D32F2F'>*</font></html>");
        } else {
            label = new JLabel(text);
        }
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(Color.decode("#333333"));
        return label;
    }

    private Border createInputBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(0, 38));
        return button;
    }

    private class ButtonCellRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        private final JLabel emptyLabel = new JLabel("", SwingConstants.CENTER);

        public ButtonCellRenderer() {
            setText("Sửa");
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            setForeground(BUTTON_COLOR);
            setBackground(new Color(245, 245, 245));
            emptyLabel.setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (!PermissionUtil.hasActionPermission("MANAGE_SUPPLIER")) {
                emptyLabel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                return emptyLabel;
            }
            if (isSelected) {
                setBackground(new Color(220, 237, 200));
            } else {
                setBackground(new Color(245, 245, 245));
            }
            return this;
        }
    }

    private class ButtonCellEditor extends DefaultCellEditor {
        private final JButton button;
        private int selectedRow;

        public ButtonCellEditor() {
            super(new JCheckBox());
            button = new JButton("Sửa");
            button.setFont(new Font("Segoe UI", Font.BOLD, 12));
            button.setForeground(BUTTON_COLOR);
            button.setFocusPainted(false);
            button.setBackground(new Color(245, 245, 245));
            button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            button.addActionListener(e -> {
                fireEditingStopped();
                selectSupplierForEdit(selectedRow);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            selectedRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "Sửa";
        }
    }
}
