package com.bookstore.gui.panel.ProductTab;

import com.bookstore.bus.BookBUS;
import com.bookstore.bus.CategoryBUS;
import com.bookstore.dto.BookDTO;
import com.bookstore.dto.CategoryDTO;
import com.bookstore.util.AppConstant;
import com.bookstore.util.Refreshable;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.util.List;

public class CategoryPanel extends JPanel implements Refreshable {
    private static final Color MAIN_COLOR = Color.decode(AppConstant.GREEN_COLOR_CODE);
    private static final Color BUTTON_COLOR = Color.decode(AppConstant.BUTTON_COLOR);
    private static final Color BORDER_COLOR = Color.decode("#E0E0E0");

    private final CategoryBUS categoryBUS;
    private final BookBUS bookBUS;

    private JTable categoryTable;
    private DefaultTableModel tableModel;
    private JTextField categoryNameField;
    private JLabel formTitleLabel;
    private JButton saveButton;
    private JButton resetButton;

    private List<CategoryDTO> categories;
    private List<BookDTO> books;
    private Integer editingCategoryId;

    public CategoryPanel() {
        categoryBUS = new CategoryBUS();
        bookBUS = new BookBUS();

        initUI();
        loadData();
    }

    @Override
    public void refresh() {
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createLeftPanel(), createRightPanel());
        splitPane.setDividerLocation(0.6);
        splitPane.setResizeWeight(0.6);
        splitPane.setBorder(null);

        contentPanel.add(splitPane, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));

        JLabel titleLabel = new JLabel("DANH SÁCH THỂ LOẠI");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.decode("#1E1E1E"));

        JPanel titleContainer = new JPanel(new BorderLayout());
        titleContainer.add(titleLabel, BorderLayout.WEST);

        String[] columns = {"Tên thể loại", "Số lượng đầu sách", "Thao tác"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }
        };

        categoryTable = new JTable(tableModel);
        categoryTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        categoryTable.setRowHeight(42);
        categoryTable.setGridColor(BORDER_COLOR);
        categoryTable.setShowHorizontalLines(true);
        categoryTable.setShowVerticalLines(true);
        categoryTable.setSelectionBackground(Color.decode("#E8F5E9"));
        categoryTable.setSelectionForeground(Color.BLACK);

        JTableHeader header = categoryTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(MAIN_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        categoryTable.getColumnModel().getColumn(0).setPreferredWidth(360);
        categoryTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        categoryTable.getColumnModel().getColumn(2).setPreferredWidth(130);

        categoryTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        categoryTable.getColumnModel().getColumn(2).setCellRenderer(new ActionButtonRenderer());
        categoryTable.getColumnModel().getColumn(2).setCellEditor(new ActionButtonEditor());

        JScrollPane scrollPane = new JScrollPane(categoryTable);
        scrollPane.setBorder(createRoundedBorder());

        panel.add(titleContainer, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(createRoundedBorder());
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(360, 0));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        formTitleLabel = new JLabel("THÊM THỂ LOẠI");
        formTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel("Tên thể loại");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        categoryNameField = new JTextField();
        categoryNameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        categoryNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        saveButton = createStyledButton("THÊM THỂ LOẠI", BUTTON_COLOR);
        saveButton.addActionListener(e -> onSaveCategory());

        resetButton = createStyledButton("LÀM MỚI", Color.decode("#F5F5F5"));
        resetButton.setForeground(Color.decode("#333333"));
        resetButton.addActionListener(e -> resetForm());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        buttonPanel.setPreferredSize(new Dimension(0, 36));
        buttonPanel.add(saveButton);
        buttonPanel.add(resetButton);

        formPanel.add(formTitleLabel);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(nameLabel);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(categoryNameField);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(buttonPanel);
        formPanel.add(Box.createVerticalGlue());

        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(0, 36));
        return button;
    }

    private Border createRoundedBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
        );
    }

    private void loadData() {
        categories = categoryBUS.selectAllCategories();
        books = bookBUS.selectAllBooks();
        loadTableData();
    }

    private void loadTableData() {
        tableModel.setRowCount(0);

        for (CategoryDTO category : categories) {
            int bookCount = 0;
            for (BookDTO book : books) {
                if (book.getCategoryId() == category.getCategoryId()) {
                    bookCount++;
                }
            }
            tableModel.addRow(new Object[]{category, bookCount, "Sửa"});
        }
    }

    private void onSaveCategory() {
        String newCategoryName = categoryNameField.getText().trim();

        if (newCategoryName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên thể loại không được để trống.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (editingCategoryId == null) {
            CategoryDTO category = new CategoryDTO();
            category.setCategoryName(newCategoryName);
            String message = categoryBUS.addCategory(category);
            JOptionPane.showMessageDialog(this, message);
        } else {
            CategoryDTO oldCategory = categoryBUS.getById(editingCategoryId);
            if (oldCategory == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy thể loại cần sửa.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                resetForm();
                loadData();
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Mọi sách ở \"" + oldCategory.getCategoryName() + "\" sẽ đổi tên thành \"" + newCategoryName + "\".\nBạn có muốn tiếp tục không?",
                    "Xác nhận sửa thể loại",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            CategoryDTO updatedCategory = new CategoryDTO(editingCategoryId, newCategoryName);
            String message = categoryBUS.updateCategory(updatedCategory);
            JOptionPane.showMessageDialog(this, message);
        }

        resetForm();
        loadData();
    }

    private void startEditCategory(int row) {
        CategoryDTO category = (CategoryDTO) tableModel.getValueAt(row, 0);
        editingCategoryId = category.getCategoryId();
        categoryNameField.setText(category.getCategoryName());
        formTitleLabel.setText("CHỈNH SỬA THỂ LOẠI");
        saveButton.setText("LƯU CHỈNH SỬA");
        categoryNameField.requestFocus();
        categoryNameField.selectAll();
    }

    private void resetForm() {
        editingCategoryId = null;
        categoryNameField.setText("");
        formTitleLabel.setText("THÊM THỂ LOẠI");
        saveButton.setText("THÊM THỂ LOẠI");
    }

    private class ActionButtonRenderer extends JButton implements TableCellRenderer {
        ActionButtonRenderer() {
            setText("Sửa");
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            setForeground(BUTTON_COLOR);
            setBackground(new Color(245, 245, 245));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(new Color(220, 237, 200));
            } else {
                setBackground(new Color(245, 245, 245));
            }
            return this;
        }
    }

    private class ActionButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private int currentRow;

        ActionButtonEditor() {
            super(new JCheckBox());
            button = new JButton("Sửa");
            button.setFont(new Font("Segoe UI", Font.BOLD, 12));
            button.setForeground(BUTTON_COLOR);
            button.setFocusPainted(false);
            button.setBackground(new Color(245, 245, 245));
            button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            button.addActionListener(e -> {
                fireEditingStopped();
                startEditCategory(currentRow);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "Sửa";
        }
    }
}
