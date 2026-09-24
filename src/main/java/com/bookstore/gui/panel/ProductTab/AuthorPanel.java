package com.bookstore.gui.panel.ProductTab;

import com.bookstore.bus.AuthorBUS;
import com.bookstore.dto.AuthorDTO;
import com.bookstore.bus.BookBUS;
import com.bookstore.dto.BookDTO;
import com.bookstore.util.AppConstant;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AuthorPanel extends JPanel implements Refreshable{
    private static final Color MAIN_COLOR = Color.decode(AppConstant.GREEN_COLOR_CODE);
    private static final Color BUTTON_COLOR = Color.decode(AppConstant.BUTTON_COLOR);
    private static final Color BORDER_COLOR = Color.decode("#E0E0E0");

    private final AuthorBUS authorBUS;
    private final BookBUS bookBUS;

    private JTextField searchField;
    private JComboBox<String> nationalityCombo;
    private JTable authorTable;
    private DefaultTableModel tableModel;

    private JTextField nameField;
    private JTextField countryField;
    private JButton saveButton;
    private JButton clearButton;
    private JLabel formTitleLabel;

    private List<AuthorDTO> allAuthors;
    private List<AuthorDTO> filteredAuthors;
    private List<BookDTO> allBooks;
    private int selectedAuthorId = -1;

    public AuthorPanel() {
        authorBUS = new AuthorBUS();
        bookBUS = new BookBUS();
        allAuthors = new ArrayList<>();
        filteredAuthors = new ArrayList<>();
        allBooks = new ArrayList<>();
        loadAuthorData();
        initUI();
        loadCountriesToCombo();
        applyFilter();
    }

    @Override
    public void refresh() {
        loadAuthorData();
        loadCountriesToCombo();
        applyFilter();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setBorder(null);
        splitPane.setResizeWeight(0.7);
        splitPane.setDividerSize(8);

        splitPane.setLeftComponent(createLeftPanel());
        splitPane.setRightComponent(createRightPanel());

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

        JPanel countryPanel = new JPanel(new BorderLayout(8, 0));
        countryPanel.setBackground(Color.WHITE);

        JLabel countryLabel = createFilterLabel("Quốc gia:");
        countryPanel.add(countryLabel, BorderLayout.WEST);

        nationalityCombo = new JComboBox<>();
        nationalityCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nationalityCombo.setBackground(Color.WHITE);
        nationalityCombo.setPreferredSize(new Dimension(220, 35));
        nationalityCombo.addActionListener(e -> applyFilter());
        countryPanel.add(nationalityCombo, BorderLayout.CENTER);

        wrapper.add(searchPanel, BorderLayout.NORTH);
        wrapper.add(countryPanel, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));

        String[] columns = {"Tên tác giả", "Quốc gia", "Số lượng đầu sách", "Thao tác"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };

        authorTable = new JTable(tableModel);
        authorTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        authorTable.setRowHeight(38);
        authorTable.setSelectionBackground(new Color(232, 245, 233));
        authorTable.setSelectionForeground(Color.BLACK);
        authorTable.setGridColor(BORDER_COLOR);
        authorTable.setShowVerticalLines(true);
        authorTable.setShowHorizontalLines(true);

        JTableHeader header = authorTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(MAIN_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        authorTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonCellRenderer());
        authorTable.getColumnModel().getColumn(3).setCellEditor(new ButtonCellEditor());
        authorTable.getColumnModel().getColumn(0).setPreferredWidth(220);
        authorTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        authorTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        authorTable.getColumnModel().getColumn(3).setPreferredWidth(90);

        JScrollPane scrollPane = new JScrollPane(authorTable);
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

        formTitleLabel = new JLabel("THÊM TÁC GIẢ");
        formTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        formTitleLabel.setForeground(MAIN_COLOR);

        gbc.gridy = 0;
        formPanel.add(formTitleLabel, gbc);

        gbc.gridy = 1;
        formPanel.add(createFormLabel("Tên tác giả"), gbc);

        nameField = new JTextField();
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameField.setBorder(createInputBorder());
        gbc.gridy = 2;
        formPanel.add(nameField, gbc);

        gbc.gridy = 3;
        formPanel.add(createFormLabel("Quốc gia"), gbc);

        countryField = new JTextField();
        countryField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        countryField.setBorder(createInputBorder());
        gbc.gridy = 4;
        formPanel.add(countryField, gbc);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        saveButton = createStyledButton("LƯU", BUTTON_COLOR);
        saveButton.addActionListener(e -> saveAuthor());
        buttonPanel.add(saveButton);

        clearButton = createStyledButton("LÀM MỚI", Color.decode("#757575"));
        clearButton.addActionListener(e -> resetForm());
        buttonPanel.add(clearButton);

        gbc.gridy = 5;
        gbc.insets = new Insets(18, 0, 0, 0);
        formPanel.add(buttonPanel, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        return panel;
    }

    private void loadAuthorData() {
        allAuthors = authorBUS.selectAllAuthors();
        allBooks = bookBUS.selectAllBooks();
    }

    private void loadCountriesToCombo() {
        for (java.awt.event.ActionListener al : nationalityCombo.getActionListeners()) {
            nationalityCombo.removeActionListener(al);
        }

        nationalityCombo.removeAllItems();
        nationalityCombo.addItem("Tất cả quốc gia");

        Set<String> uniqueCountries = new HashSet<>();

        for (AuthorDTO author : allAuthors) {
            String country = author.getNationality();
            if (country != null && !country.trim().isEmpty()) {
                uniqueCountries.add(country.trim());
            }
        }

        for (String country : uniqueCountries) {
            nationalityCombo.addItem(country);
        }

        nationalityCombo.addActionListener(e -> applyFilter());
    }

    private void applyFilter() {
        filteredAuthors.clear();

        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String countryFilter = nationalityCombo.getSelectedItem() == null ? "Tất cả quốc gia" : nationalityCombo.getSelectedItem().toString();

        for (AuthorDTO author : allAuthors) {
            boolean matchedName = author.getAuthorName() != null && author.getAuthorName().toLowerCase().contains(keyword);
            boolean matchedCountry = "Tất cả quốc gia".equals(countryFilter)
                    || (author.getNationality() != null && author.getNationality().equalsIgnoreCase(countryFilter));

            if (matchedName && matchedCountry) {
                filteredAuthors.add(author);
            }
        }

        reloadTable();
    }

    private void reloadTable() {
        tableModel.setRowCount(0);

        for (AuthorDTO author : filteredAuthors) {
            tableModel.addRow(new Object[]{
                    author.getAuthorName(),
                    author.getNationality(),
                    countBooksByAuthorId(author.getAuthorId()),
                    "Sửa"
            });
        }
    }

    private int countBooksByAuthorId(int authorId) {
        int count = 0;
        for (BookDTO book : allBooks) {
            if (book.getAuthorIdsList().contains(authorId)) {
                count++;
            }
        }
        return count;
    }

    private void saveAuthor() {
        String name = nameField.getText().trim();
        String country = countryField.getText().trim();

        AuthorDTO authorDTO = new AuthorDTO();
        authorDTO.setAuthorName(name);
        authorDTO.setNationality(country);

        String message;
        if (selectedAuthorId == -1) {
            message = authorBUS.addAuthor(authorDTO);
        } else {
            authorDTO.setAuthorId(selectedAuthorId);
            message = authorBUS.updateAuthor(authorDTO);
        }

        JOptionPane.showMessageDialog(this, message);
        if (message.toLowerCase().contains("thành công")) {
            loadAuthorData();
            loadCountriesToCombo();
            applyFilter();
            resetForm();
        }
    }

    private void selectAuthorForEdit(int row) {
        if (row < 0 || row >= filteredAuthors.size()) {
            return;
        }

        AuthorDTO author = filteredAuthors.get(row);
        selectedAuthorId = author.getAuthorId();
        nameField.setText(author.getAuthorName());
        countryField.setText(author.getNationality());
        formTitleLabel.setText("CHỈNH SỬA TÁC GIẢ");
        saveButton.setText("Cập nhật");
    }

    private void resetForm() {
        selectedAuthorId = -1;
        nameField.setText("");
        countryField.setText("");
        formTitleLabel.setText("THÊM TÁC GIẢ");
        saveButton.setText("LƯU");
        authorTable.clearSelection();
    }

    private JLabel createFilterLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(Color.decode("#555555"));
        return label;
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
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
        public ButtonCellRenderer() {
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
                selectAuthorForEdit(selectedRow);
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
