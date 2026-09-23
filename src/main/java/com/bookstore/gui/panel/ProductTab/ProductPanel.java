package com.bookstore.gui.panel.ProductTab;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;
import java.io.IOException;

import com.bookstore.util.AppConstant;
import com.bookstore.util.Refreshable;
import com.bookstore.util.SearchableComboBox;
import com.bookstore.bus.*;
import com.bookstore.dto.*;
import com.bookstore.util.ExcelUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

public class ProductPanel extends JPanel implements Refreshable {
    private static final Color MAIN_COLOR = Color.decode(AppConstant.GREEN_COLOR_CODE);
    private static final Color BUTTON_COLOR = Color.decode(AppConstant.BUTTON_COLOR);
    private static final Color BORDER_COLOR = Color.decode("#E0E0E0");

    private JTextField searchField;
    private SearchableComboBox<String> authorCombo;
    private JComboBox<String> categoryCombo;
    private SearchableComboBox<String> supplierCombo;
    private JComboBox<String> statusCombo;
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private Set<String> selectedTags = new HashSet<>();
    private JButton tagFilterButton;
    private JLabel noResultsLabel;
    private JScrollPane scrollPane;
    private BookBUS bookBUS;
    private AuthorBUS authorBUS;
    private CategoryBUS categoryBUS;
    private SupplierBUS supplierBUS;

    private List<BookDTO> allBooks;
    private List<AuthorDTO> authors;
    private List<CategoryDTO> categories;
    private List<SupplierDTO> suppliers;
    private Set<String> allTags;

    public ProductPanel() {
        bookBUS = new BookBUS();
        authorBUS = new AuthorBUS();
        categoryBUS = new CategoryBUS();
        supplierBUS = new SupplierBUS();

        loadDataFromDatabase();

        initUI();
        loadTableData();
        applyPermissions();
    }

    private void applyPermissions() {
        com.bookstore.dto.PermissionDTO perm = com.bookstore.util.SharedData.userPermissions.get("MANAGE_PRODUCT");
        boolean hasAction = (perm != null && perm.isAction());
        // btnAddProduct.setVisible(hasAction);
        // btnEditProduct.setVisible(hasAction);
        // btnDeleteProduct.setVisible(hasAction);
        // btnImportExcel.setVisible(hasAction);
    }

    @Override
    public void refresh() {
        loadDataFromDatabase();
        loadTableData();
    }

    private void loadDataFromDatabase() {
        try {
            authors = authorBUS.selectAllAuthors();
            categories = categoryBUS.selectAllCategories();
            suppliers = supplierBUS.selectAll();
            List<BookDTO> booksFromDB = bookBUS.selectAllBooks();
            allBooks = new ArrayList<>(booksFromDB);
            allTags = new HashSet<>();
            for (BookDTO book : booksFromDB) {
                if (book.getTagDetail() != null && !book.getTagDetail().isEmpty()) {
                    String[] tags = book.getTagDetail().split(",");
                    for (String tag : tags) {
                        allTags.add(tag.trim());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi load dữ liệu từ database: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);

            authors = new ArrayList<>();
            categories = new ArrayList<>();
            suppliers = new ArrayList<>();
            allBooks = new ArrayList<>();
            allTags = new HashSet<>();
        }
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));

        JPanel contentPanel = new JPanel(new BorderLayout(0, 15));
        contentPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel topPanel = createTopPanel();
        contentPanel.add(topPanel, BorderLayout.NORTH);

        JPanel tablePanel = createTablePanel();
        contentPanel.add(tablePanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(0, 15));

        JPanel searchRow = new JPanel(new BorderLayout(15, 0));

        searchField = new JTextField();
        searchField.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterBooks();
            }
        });

        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSVGIcon("icon/search_icon.svg").derive(20, 20));

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchRow.add(searchPanel, BorderLayout.CENTER);

        searchRow.add(searchPanel, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        JButton importButton = createStyledButton("IMPORT EXCEL", Color.decode("#1976D2"));
        importButton.setPreferredSize(new Dimension(170, 38));

        FlatSVGIcon importIcon = new FlatSVGIcon("icon/import_excel_icon.svg").derive(20, 20);
        importIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE));
        importButton.setIcon(importIcon);
        importButton.setIconTextGap(8);

        importButton.addActionListener(e -> {
            showImportGuideDialog();
            importBooksFromExcel();
        });
        actionPanel.add(importButton);

        JButton exportButton = createStyledButton("EXPORT EXCEL", Color.decode("#8E24AA"));
        exportButton.setPreferredSize(new Dimension(170, 38));

        FlatSVGIcon exportIcon = new FlatSVGIcon("icon/export_excel_icon.svg").derive(20, 20);
        exportIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE));
        exportButton.setIcon(exportIcon);
        exportButton.setIconTextGap(8);

        exportButton.addActionListener(e -> exportBooksToExcel());
        actionPanel.add(exportButton);


        JButton addButton = createStyledButton("+ THÊM SÁCH", BUTTON_COLOR);
        addButton.setPreferredSize(new Dimension(160, 38));
        addButton.addActionListener(e -> showAddBookDialog());
        actionPanel.add(addButton);

        searchRow.add(actionPanel, BorderLayout.EAST);

        topPanel.add(searchRow, BorderLayout.NORTH);


        JPanel filtersPanel = createFiltersPanel();
        topPanel.add(filtersPanel, BorderLayout.CENTER);

        return topPanel;
    }

    private JPanel createFiltersPanel() {
        JPanel filtersPanel = new JPanel();
        filtersPanel.setLayout(new BoxLayout(filtersPanel, BoxLayout.Y_AXIS));


        JPanel gridPanel = new JPanel(new GridBagLayout());
        gridPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);


        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gridPanel.add(createFilterLabel("Tác giả:"), gbc);


        gbc.gridx = 1;
        gbc.weightx = 1;
        authorCombo = new SearchableComboBox<>(getAuthorNamesList());
        authorCombo.setPreferredSize(new Dimension(220, 35));
        authorCombo.addActionListener(e -> filterBooks());
        gridPanel.add(authorCombo, gbc);


        gbc.gridx = 2;
        gbc.weightx = 0;
        gridPanel.add(createFilterLabel("Thể loại:"), gbc);


        gbc.gridx = 3;
        gbc.weightx = 1;
        categoryCombo = new JComboBox<>(getCategoryNamesList().toArray(new String[0]));
        categoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        categoryCombo.setBackground(Color.WHITE);
        categoryCombo.setPreferredSize(new Dimension(220, 35));
        categoryCombo.addActionListener(e -> filterBooks());
        gridPanel.add(categoryCombo, gbc);


        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gridPanel.add(createFilterLabel("Nhà cung cấp:"), gbc);


        gbc.gridx = 1;
        gbc.weightx = 1;
        supplierCombo = new SearchableComboBox<>(getSupplierNamesList());
        supplierCombo.setPreferredSize(new Dimension(220, 35));
        supplierCombo.addActionListener(e -> filterBooks());
        gridPanel.add(supplierCombo, gbc);


        gbc.gridx = 2;
        gbc.weightx = 0;
        gridPanel.add(createFilterLabel("Trạng thái:"), gbc);


        gbc.gridx = 3;
        gbc.weightx = 1;
        statusCombo = new JComboBox<>(new String[]{"Tất cả trạng thái", "Đang bán", "Ngừng bán"});
        statusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusCombo.setBackground(Color.WHITE);
        statusCombo.setPreferredSize(new Dimension(220, 35));
        statusCombo.addActionListener(e -> filterBooks());
        gridPanel.add(statusCombo, gbc);

        filtersPanel.add(gridPanel);
        filtersPanel.add(Box.createVerticalStrut(12));


        JPanel buttonRow = new JPanel(new GridLayout(0, 5, 8, 8));
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);


        tagFilterButton = new JButton("Tags +");
        tagFilterButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tagFilterButton.setForeground(BUTTON_COLOR);
        tagFilterButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BUTTON_COLOR, 2),
                BorderFactory.createEmptyBorder(7, 18, 7, 18)
        ));
        tagFilterButton.setFocusPainted(false);
        tagFilterButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        tagFilterButton.addActionListener(e -> showTagFilterPanel());
        buttonRow.add(tagFilterButton);


        JButton resetButton = new JButton("✖ Xóa bộ lọc");
        resetButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        resetButton.setForeground(Color.decode("#666666"));
        resetButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(7, 15, 7, 15)
        ));
        resetButton.setFocusPainted(false);
        resetButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetButton.addActionListener(e -> resetFilters());
        buttonRow.add(resetButton);

        filtersPanel.add(buttonRow);

        return filtersPanel;
    }

    private JLabel createFilterLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(Color.decode("#333333"));
        return label;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));


        String[] columns = {"Sách", "Tác giả", "Thể loại", "Nhà cung cấp", "Trạng thái", "Thao tác"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };

        bookTable = new JTable(tableModel);
        bookTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bookTable.setRowHeight(80);
        bookTable.setShowVerticalLines(true);
        bookTable.setShowHorizontalLines(true);
        bookTable.setGridColor(BORDER_COLOR);
        bookTable.setSelectionBackground(Color.decode("#E8F5E9"));
        bookTable.setSelectionForeground(Color.BLACK);


        bookTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = bookTable.rowAtPoint(e.getPoint());
                int col = bookTable.columnAtPoint(e.getPoint());
                if (row >= 0 && col >= 0) {
                    bookTable.setRowSelectionInterval(row, row);
                }
            }
        });

        bookTable.getTableHeader().setReorderingAllowed(false);
        bookTable.getTableHeader().setResizingAllowed(false);


        JTableHeader header = bookTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(MAIN_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 45));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);


        bookTable.getColumnModel().getColumn(0).setPreferredWidth(280);
        bookTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        bookTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        bookTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        bookTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        bookTable.getColumnModel().getColumn(5).setPreferredWidth(150);


        bookTable.getColumnModel().getColumn(0).setCellRenderer(new BookCellRenderer());
        bookTable.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());
        bookTable.getColumnModel().getColumn(5).setCellRenderer(new ActionCellRenderer());
        bookTable.getColumnModel().getColumn(5).setCellEditor(new ActionCellEditor());


        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        bookTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        bookTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        bookTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        scrollPane = new JScrollPane(bookTable);
        scrollPane.setBorder(null);

        noResultsLabel = new JLabel("Không có kết quả tương thích", SwingConstants.CENTER);
        noResultsLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        noResultsLabel.setForeground(Color.decode("#999999"));
        noResultsLabel.setVisible(false);

        JPanel tableContainer = new JPanel();
        tableContainer.setLayout(new OverlayLayout(tableContainer));


        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(noResultsLabel);

        tableContainer.add(centerPanel);
        tableContainer.add(scrollPane);

        tablePanel.add(tableContainer, BorderLayout.CENTER);

        return tablePanel;
    }

    private void loadTableData() {
        loadTableData(allBooks);
    }

    private void loadTableData(List<BookDTO> books) {
        tableModel.setRowCount(0);

        if (books.isEmpty()) {
            noResultsLabel.setVisible(true);
            bookTable.setVisible(false);
        } else {
            noResultsLabel.setVisible(false);
            bookTable.setVisible(true);

            for (BookDTO book : books) {
                Object[] row = new Object[6];
                row[0] = book;
                row[1] = getAuthorNamesForBook(book);
                row[2] = getCategoryName(book.getCategoryId());
                row[3] = getSupplierName(book.getSupplierId());
                row[4] = book.getStatus() == 1 ? "Đang bán" : "Ngừng bán";
                row[5] = book;
                tableModel.addRow(row);
            }
        }
    }

    private void filterBooks() {
        String searchText = searchField.getText().toLowerCase().trim();
        Object selectedAuthorObj = authorCombo.getSelectedItem();
        Object selectedCategoryObj = categoryCombo.getSelectedItem();
        Object selectedSupplierObj = supplierCombo.getSelectedItem();
        String selectedStatus = (String) statusCombo.getSelectedItem();

        String selectedAuthor = getSelectedFilterValue(selectedAuthorObj);
        String selectedCategory = getSelectedFilterValue(selectedCategoryObj);
        String selectedSupplier = getSelectedFilterValue(selectedSupplierObj);

        List<BookDTO> filtered = new ArrayList<>();

        for (BookDTO book : allBooks) {

            if (!searchText.isEmpty() && !book.getBookName().toLowerCase().contains(searchText)) {
                continue;
            }

            if (!selectedAuthor.isEmpty() && !selectedAuthor.equals("Tất cả tác giả")) {
                String authorNames = getAuthorNamesForBook(book);
                if (!authorNames.contains(selectedAuthor)) {
                    continue;
                }
            }

            if (!selectedCategory.isEmpty() && !selectedCategory.equals("Tất cả thể loại")) {
                if (!getCategoryName(book.getCategoryId()).equals(selectedCategory)) {
                    continue;
                }
            }

            if (!selectedSupplier.isEmpty() && !selectedSupplier.equals("Tất cả nhà cung cấp")) {
                if (!getSupplierName(book.getSupplierId()).equals(selectedSupplier)) {
                    continue;
                }
            }

            if (!selectedStatus.equals("Tất cả trạng thái")) {
                int requiredStatus = selectedStatus.equals("Đang bán") ? 1 : 0;
                if (book.getStatus() != requiredStatus) {
                    continue;
                }
            }

            if (!selectedTags.isEmpty()) {
                boolean hasTag = false;
                if (book.getTagDetail() != null) {
                    String[] bookTags = book.getTagDetail().split(",");
                    for (String tag : bookTags) {
                        if (selectedTags.contains(tag.trim())) {
                            hasTag = true;
                            break;
                        }
                    }
                }
                if (!hasTag) {
                    continue;
                }
            }

            filtered.add(book);
        }

        loadTableData(filtered);
    }

    private void showTagFilterPanel() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Lọc theo Tags", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Chọn Tags để lọc");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        contentPanel.add(title, BorderLayout.NORTH);

        JPanel chipsPanel = new JPanel(new GridLayout(0, 3, 8, 8));
        for (String tag : allTags) {
            JToggleButton chip = createTagChip(tag);
            chip.setSelected(selectedTags.contains(tag));
            chipsPanel.add(chip);
        }

        JScrollPane scrollPane = new JScrollPane(chipsPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        JButton cancelButton = createStyledButton("Hủy", Color.decode("#757575"));
        cancelButton.addActionListener(e -> dialog.dispose());

        JButton applyButton = createStyledButton("Áp dụng", BUTTON_COLOR);
        applyButton.addActionListener(e -> {
            selectedTags.clear();
            for (Component comp : chipsPanel.getComponents()) {
                if (comp instanceof JToggleButton) {
                    JToggleButton chip = (JToggleButton) comp;
                    if (chip.isSelected()) {
                        selectedTags.add(chip.getText());
                    }
                }
            }
            updateTagButtonText();
            filterBooks();
            dialog.dispose();
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(applyButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(contentPanel);
        dialog.setVisible(true);
    }

    private JToggleButton createTagChip(String text) {
        JToggleButton chip = new JToggleButton(text);
        chip.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chip.setFocusPainted(false);
        chip.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chip.setBorder(BorderFactory.createEmptyBorder(6, 15, 6, 15));

        chip.setBackground(Color.decode("#F5F5F5"));
        chip.setForeground(Color.decode("#333333"));

        chip.addItemListener(e -> {
            if (chip.isSelected()) {
                chip.setBackground(BUTTON_COLOR);
                chip.setForeground(Color.WHITE);
            } else {
                chip.setBackground(Color.decode("#F5F5F5"));
                chip.setForeground(Color.decode("#333333"));
            }
        });

        return chip;
    }

    private void updateTagButtonText() {
        if (selectedTags.isEmpty()) {
            tagFilterButton.setText("Tags +");
        } else {
            tagFilterButton.setText("Tags (" + selectedTags.size() + ")");
        }
    }

    private void resetFilters() {
        searchField.setText("");
        authorCombo.resetSelection();
        if (categoryCombo.getItemCount() > 0) {
            categoryCombo.setSelectedIndex(0);
        }
        supplierCombo.resetSelection();
        statusCombo.setSelectedIndex(0);
        selectedTags.clear();
        updateTagButtonText();
        filterBooks();
    }


    private void exportBooksToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn vị trí lưu file Excel");
        fileChooser.setSelectedFile(new File("products.xlsx"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();
        if (!selectedFile.getName().toLowerCase().endsWith(".xlsx")) {
            selectedFile = new File(selectedFile.getAbsolutePath() + ".xlsx");
        }

        List<ExcelUtil.ExcelBookRow> exportRows = new ArrayList<>();
        for (BookDTO book : allBooks) {
            ExcelUtil.ExcelBookRow row = new ExcelUtil.ExcelBookRow();
            row.setBookName(book.getBookName());
            row.setSellingPrice(book.getSellingPrice());
            row.setQuantity(book.getQuantity());
            row.setAuthorNames(getAuthorNamesForBook(book));
            row.setCategoryName(getCategoryName(book.getCategoryId()));
            row.setSupplierName(getSupplierName(book.getSupplierId()));
            row.setStatus(book.getStatus());
            row.setTranslator(book.getTranslator());
            row.setTagDetail(book.getTagDetail());
            row.setDescription(book.getDescription());
            row.setImagePath(book.getImage());
            exportRows.add(row);
        }

        try {
            ExcelUtil.exportBooks(selectedFile, exportRows);
            JOptionPane.showMessageDialog(this,
                    "Export Excel thành công:\n" + selectedFile.getAbsolutePath(),
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Không thể export file Excel: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void importBooksFromExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file Excel để import");

        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();
        List<String> errors = new ArrayList<>();
        int successCount = 0;

        try {
            List<ExcelUtil.ExcelBookRow> importedRows = ExcelUtil.importBooks(selectedFile);
            for (ExcelUtil.ExcelBookRow row : importedRows) {
                try {
                    BookDTO book = mapImportedRowToBook(row);
                    String result = bookBUS.addBook(book);

                    if ("OK".equals(result) || result.toLowerCase().contains("thành công") || result.toLowerCase().contains("công")) {
                        bookBUS.addAuthorsToBook(book.getBookId(), book.getAuthorIdsList());
                        successCount++;
                    } else {
                        errors.add("Dòng " + row.getSourceRow() + ": " + result);
                    }
                } catch (IllegalArgumentException ex) {
                    errors.add("Dòng " + row.getSourceRow() + ": " + ex.getMessage());
                }
            }

            loadDataFromDatabase();
            filterBooks();

            StringBuilder message = new StringBuilder();
            message.append("Import thành công ").append(successCount).append(" sản phẩm.");

            if (!errors.isEmpty()) {
                message.append("\n\nCác dòng lỗi:");
                int maxErrorToShow = Math.min(errors.size(), 8);
                for (int i = 0; i < maxErrorToShow; i++) {
                    message.append("\n- ").append(errors.get(i));
                }
                if (errors.size() > maxErrorToShow) {
                    message.append("\n... và ").append(errors.size() - maxErrorToShow).append(" lỗi khác.");
                }
            }

            JOptionPane.showMessageDialog(this,
                    message.toString(),
                    errors.isEmpty() ? "Thành công" : "Import hoàn tất",
                    errors.isEmpty() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Không thể import file Excel: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private BookDTO mapImportedRowToBook(ExcelUtil.ExcelBookRow row) {
        BookDTO book = new BookDTO();
        book.setBookName(row.getBookName());
        book.setSellingPrice(0);
        book.setQuantity(0);
        book.setTranslator(row.getTranslator());
        book.setImage("");
        book.setDescription(row.getDescription());
        book.setStatus(row.getStatus());
        book.setTagDetail(row.getTagDetail());

        int categoryId = getCategoryIdByName(row.getCategoryName());
        if (categoryId <= 0) {
            throw new IllegalArgumentException("Không tìm thấy thể loại: " + row.getCategoryName());
        }
        book.setCategoryId(categoryId);

        int supplierId = getSupplierIdByName(row.getSupplierName());
        if (supplierId <= 0) {
            throw new IllegalArgumentException("Không tìm thấy nhà cung cấp: " + row.getSupplierName());
        }
        book.setSupplierId(supplierId);

        List<Integer> authorIds = getAuthorIdsByNames(row.getAuthorNames());
        if (authorIds.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy tác giả hợp lệ trong cột tác giả");
        }
        book.getAuthorIdsList().addAll(authorIds);

        return book;
    }


    private void showImportGuideDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Hướng dẫn chuẩn hóa file import", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(700, 460);
        dialog.setLocationRelativeTo(this);

        JPanel contentPanel = new JPanel(new BorderLayout(12, 12));
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Chuẩn hóa file Excel trước khi import");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        contentPanel.add(title, BorderLayout.NORTH);

        JTextArea guideText = new JTextArea();
        guideText.setEditable(false);
        guideText.setLineWrap(true);
        guideText.setWrapStyleWord(true);
        guideText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        guideText.setText("1) Dòng đầu tiên phải là tiêu đề cột theo đúng thứ tự:\n"
                + "Tên sách | Tác giả | Thể loại | Nhà cung cấp | Trạng thái | Dịch giả | Tags | Mô tả\n\n"
                + "2) Cột Tác giả có thể nhập nhiều tên, ngăn cách bằng dấu phẩy (,).\n"
                + "3) Tên tác giả, thể loại, nhà cung cấp phải khớp dữ liệu có sẵn trong hệ thống.\n"
                + "4) Cột Trạng thái chấp nhận: 'Đang bán', 'Ngừng bán', 1 hoặc 0.\n"
                + "5) Không cần các cột Giá bán, Số lượng, Đường dẫn ảnh. Khi import hệ thống sẽ tự gán:\n"
                + "   - Giá bán = 0\n"
                + "   - Số lượng = 0\n"
                + "   - Đường dẫn ảnh = rỗng\n\n"
                + "Nhấn 'Tiếp tục import' để chọn file Excel.");
        guideText.setBackground(Color.decode("#FAFAFA"));
        guideText.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane guideScrollPane = new JScrollPane(guideText);
        guideScrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        contentPanel.add(guideScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        JButton continueButton = createStyledButton("Tiếp tục import", BUTTON_COLOR);
        continueButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(continueButton);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }


    private int getCategoryIdByName(String categoryName) {
        if (categoryName == null) {
            return -1;
        }
        for (CategoryDTO category : categories) {
            if (categoryName.trim().equalsIgnoreCase(category.getCategoryName())) {
                return category.getCategoryId();
            }
        }
        return -1;
    }

    private int getSupplierIdByName(String supplierName) {
        if (supplierName == null) {
            return -1;
        }
        for (SupplierDTO supplier : suppliers) {
            if (supplierName.trim().equalsIgnoreCase(supplier.getSupplierName())) {
                return supplier.getSupplierId();
            }
        }
        return -1;
    }

    private List<Integer> getAuthorIdsByNames(String authorNames) {
        List<Integer> authorIds = new ArrayList<>();
        if (authorNames == null || authorNames.trim().isEmpty()) {
            return authorIds;
        }

        String[] names = authorNames.split(",");
        for (String name : names) {
            String normalizedName = name.trim();
            if (normalizedName.isEmpty()) {
                continue;
            }
            for (AuthorDTO author : authors) {
                if (normalizedName.equalsIgnoreCase(author.getAuthorName()) && !authorIds.contains(author.getAuthorId())) {
                    authorIds.add(author.getAuthorId());
                }
            }
        }
        return authorIds;
    }


    private String getSelectedFilterValue(Object selectedObj) {
        if (selectedObj == null) {
            return "";
        }
        String value = selectedObj.toString();
        return value != null ? value.trim() : "";
    }

    private void showAddBookDialog() {
        BookFormDialog dialog = new BookFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Thêm sách mới",
                null,
                authors, categories, suppliers, allTags
        );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            BookDTO newBook = dialog.getBook();

            try {
                BookDTO bookDTO = new BookDTO();
                bookDTO.setBookName(newBook.getBookName());
                bookDTO.setCoverPrice(newBook.getCoverPrice());
                bookDTO.setPublicationYear(newBook.getPublicationYear());
                bookDTO.setSellingPrice(newBook.getSellingPrice());
                bookDTO.setQuantity(newBook.getQuantity());
                bookDTO.setTranslator(newBook.getTranslator());
                bookDTO.setImage(newBook.getImage());
                bookDTO.setDescription(newBook.getDescription());
                bookDTO.setStatus(newBook.getStatus());
                bookDTO.setCategoryId(newBook.getCategoryId());
                bookDTO.setSupplierId(newBook.getSupplierId());
                bookDTO.setTagDetail(newBook.getTagDetail());
                if (newBook.getAuthorIdsList() != null) {
                    bookDTO.getAuthorIdsList().addAll(newBook.getAuthorIdsList());
                }

                String result = bookBUS.addBook(bookDTO);

                if ("OK".equals(result) || result.toLowerCase().contains("thành công") || result.toLowerCase().contains("công")) {
                    if (newBook.getAuthorIdsList() != null && !newBook.getAuthorIdsList().isEmpty()) {
                        bookBUS.addAuthorsToBook(bookDTO.getBookId(), newBook.getAuthorIdsList());
                    }
                    loadDataFromDatabase();
                    filterBooks();

                    JOptionPane.showMessageDialog(this, result,
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, result,
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Lỗi: " + e.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditBookDialog(BookDTO book) {
        BookFormDialog dialog = new BookFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Chỉnh sửa sách",
                book,
                authors, categories, suppliers, allTags
        );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            BookDTO updatedBook = dialog.getBook();

            try {
                BookDTO bookDTO = new BookDTO();
                bookDTO.setBookId(book.getBookId());
                bookDTO.setBookName(updatedBook.getBookName());
                bookDTO.setCoverPrice(updatedBook.getCoverPrice());
                bookDTO.setPublicationYear(updatedBook.getPublicationYear());
                bookDTO.setSellingPrice(updatedBook.getSellingPrice());
                bookDTO.setQuantity(updatedBook.getQuantity());
                bookDTO.setTranslator(updatedBook.getTranslator());
                bookDTO.setImage(updatedBook.getImage());
                bookDTO.setDescription(updatedBook.getDescription());
                bookDTO.setStatus(updatedBook.getStatus());
                bookDTO.setCategoryId(updatedBook.getCategoryId());
                bookDTO.setSupplierId(updatedBook.getSupplierId());
                bookDTO.setTagDetail(updatedBook.getTagDetail());
                if (updatedBook.getAuthorIdsList() != null) {
                    bookDTO.getAuthorIdsList().addAll(updatedBook.getAuthorIdsList());
                }

                String result = bookBUS.updateBook(bookDTO);

                if ("OK".equals(result) || result.toLowerCase().contains("thành công") || result.toLowerCase().contains("công")) {
                    bookBUS.removeAllAuthorsFromBook(book.getBookId());
                    if (updatedBook.getAuthorIdsList() != null && !updatedBook.getAuthorIdsList().isEmpty()) {
                        bookBUS.addAuthorsToBook(book.getBookId(), updatedBook.getAuthorIdsList());
                    }

                    loadDataFromDatabase();
                    filterBooks();

                    JOptionPane.showMessageDialog(this, result,
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, result,
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Lỗi: " + e.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showBookDetail(BookDTO book) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi tiết sách", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(700, 550);
        dialog.setLocationRelativeTo(this);

        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel(book.getBookName());
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        contentPanel.add(title, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 0));

        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(160, 240));
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.TOP);
        imageLabel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        imageLabel.setIcon(new ImageIcon(getBookCoverImage(book, 160, 240)));
        mainPanel.add(imageLabel, BorderLayout.WEST);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        addDetailRow(detailsPanel, "Tác giả:", getAuthorNamesForBook(book));
        addDetailRow(detailsPanel, "Thể loại:", getCategoryName(book.getCategoryId()));
        addDetailRow(detailsPanel, "Nhà cung cấp:", getSupplierName(book.getSupplierId()));
        addDetailRow(detailsPanel, "Trạng thái:", book.getStatus() == 1 ? "Đang bán" : "Ngừng bán");
        addDetailRow(detailsPanel, "Dịch giả:", book.getTranslator() == null || book.getTranslator().trim().isEmpty() ? "Không có" : book.getTranslator());
        addDetailRow(detailsPanel, "Tags:", book.getTagDetail() == null || book.getTagDetail().trim().isEmpty() ? "Không có" : book.getTagDetail());

        mainPanel.add(detailsPanel, BorderLayout.CENTER);
        contentPanel.add(mainPanel, BorderLayout.CENTER);

        if (book.getDescription() != null && !book.getDescription().trim().isEmpty()) {
            JPanel descPanel = new JPanel(new BorderLayout(5, 5));

            JLabel descLabel = new JLabel("Mô tả:");
            descLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            descPanel.add(descLabel, BorderLayout.NORTH);

            JTextArea descArea = new JTextArea(book.getDescription());
            descArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            descArea.setLineWrap(true);
            descArea.setWrapStyleWord(true);
            descArea.setEditable(false);
            descArea.setBackground(Color.decode("#F5F5F5"));
            descArea.setBorder(new EmptyBorder(10, 10, 10, 10));

            JScrollPane descScroll = new JScrollPane(descArea);
            descScroll.setPreferredSize(new Dimension(0, 120));
            descScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
            descPanel.add(descScroll, BorderLayout.CENTER);

            contentPanel.add(descPanel, BorderLayout.SOUTH);
        }

        JButton closeButton = createStyledButton("Đóng", BUTTON_COLOR);
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.add(closeButton);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLabel.setPreferredSize(new Dimension(110, 20));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        row.add(lblLabel);
        row.add(lblValue);
        panel.add(row);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private List<String> getAuthorNamesList() {
        List<String> names = new ArrayList<>();
        names.add("Tất cả tác giả");
        for (AuthorDTO author : authors) {
            names.add(author.getAuthorName());
        }
        return names;
    }

    private List<String> getCategoryNamesList() {
        List<String> names = new ArrayList<>();
        names.add("Tất cả thể loại");
        for (CategoryDTO category : categories) {
            names.add(category.getCategoryName());
        }
        return names;
    }

    private List<String> getSupplierNamesList() {
        List<String> names = new ArrayList<>();
        names.add("Tất cả nhà cung cấp");
        for (SupplierDTO supplier : suppliers) {
            names.add(supplier.getSupplierName());
        }
        return names;
    }

    private String getAuthorNamesForBook(BookDTO book) {
        if (book.getAuthorIdsList() == null || book.getAuthorIdsList().isEmpty()) {
            return "Không rõ";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < book.getAuthorIdsList().size(); i++) {
            int authorId = book.getAuthorIdsList().get(i);
            for (AuthorDTO author : authors) {
                if (author.getAuthorId() == authorId) {
                    sb.append(author.getAuthorName());
                    if (i < book.getAuthorIdsList().size() - 1) {
                        sb.append(", ");
                    }
                    break;
                }
            }
        }
        return sb.toString();
    }

    private String getCategoryName(int categoryId) {
        for (CategoryDTO cat : categories) {
            if (cat.getCategoryId() == categoryId) {
                return cat.getCategoryName();
            }
        }
        return "Không rõ";
    }

    private String getSupplierName(int supplierId) {
        for (SupplierDTO sup : suppliers) {
            if (sup.getSupplierId() == supplierId) {
                return sup.getSupplierName();
            }
        }
        return "Không rõ";
    }

    private Image createBookPlaceholder(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();

        g2d.setColor(Color.decode("#E0E0E0"));
        g2d.fillRect(0, 0, width, height);

        g2d.setColor(Color.decode("#BDBDBD"));
        g2d.drawRect(0, 0, width - 1, height - 1);

        g2d.setColor(Color.decode("#9E9E9E"));
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 40));
        String text = "📖";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        int y = ((height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, x, y);

        g2d.dispose();
        return img;
    }

    private Image getBookCoverImage(BookDTO book, int width, int height) {
        if (book != null && book.getImage() != null && !book.getImage().trim().isEmpty()) {
            String imagePath = "data/book_covers/" + book.getImage();
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                imagePath = book.getImage();
                imageFile = new File(imagePath);
            }

            if (imageFile.exists()) {
                ImageIcon icon = new ImageIcon(imagePath);
                if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
                    return icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                }
            }
        }
        return createBookPlaceholder(width, height);
    }

    private String truncateText(String text, Font font, int maxWidth, int maxLines) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        FontMetrics fm = getFontMetrics(font);
        String[] words = text.split("\\s+");
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        boolean needsEllipsis = false;

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            String testLine;

            if (currentLine.length() == 0) {
                testLine = word;
            } else {
                testLine = currentLine + " " + word;
            }

            int width = fm.stringWidth(testLine);

            if (width <= maxWidth) {
                currentLine = new StringBuilder(testLine);

                if (i == words.length - 1 && lines.size() < maxLines) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());

                    if (lines.size() >= maxLines) {
                        needsEllipsis = true;
                        break;
                    }

                    currentLine = new StringBuilder(word);

                    if (i == words.length - 1) {
                        if (lines.size() < maxLines) {
                            lines.add(currentLine.toString());
                        } else {
                            needsEllipsis = true;
                        }
                        currentLine = new StringBuilder();
                    }
                } else {
                    String truncatedWord = truncateSingleWord(word, fm, maxWidth - fm.stringWidth("..."));
                    lines.add(truncatedWord + "...");
                    needsEllipsis = false;

                    if (lines.size() >= maxLines) {
                        break;
                    }
                    currentLine = new StringBuilder();
                }
            }
        }

        if (currentLine.length() > 0) {
            if (lines.size() < maxLines) {
                lines.add(currentLine.toString());
            } else {
                needsEllipsis = true;
            }
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                result.append("<br>");
            }

            String line = lines.get(i);

            if (i == lines.size() - 1 && needsEllipsis) {
                String withEllipsis = line + "...";
                if (fm.stringWidth(withEllipsis) > maxWidth) {
                    line = truncateSingleWord(line, fm, maxWidth - fm.stringWidth("..."));
                }
                result.append(line).append("...");
            } else {
                result.append(line);
            }
        }

        return result.toString();
    }

    private String truncateSingleWord(String word, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(word) <= maxWidth) {
            return word;
        }

        StringBuilder truncated = new StringBuilder();
        for (char c : word.toCharArray()) {
            String test = truncated.toString() + c;
            if (fm.stringWidth(test) > maxWidth) {
                break;
            }
            truncated.append(c);
        }
        return truncated.toString();
    }

    class BookCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            BookDTO book = (BookDTO) value;

            JPanel panel = new JPanel(new BorderLayout(10, 0));
            panel.setOpaque(true);
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR),
                    new EmptyBorder(5, 10, 5, 10)
            ));

            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(table.getBackground());
            }

            JLabel imageLabel = new JLabel();
            imageLabel.setPreferredSize(new Dimension(50, 70));
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            imageLabel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
            imageLabel.setOpaque(true);
            imageLabel.setBackground(Color.decode("#F5F5F5"));
            imageLabel.setIcon(new ImageIcon(getBookCoverImage(book, 50, 70)));

            panel.add(imageLabel, BorderLayout.WEST);

            int availableWidth = table.getColumnModel().getColumn(0).getWidth() - 80;
            String truncatedName = truncateText(book.getBookName(), new Font("Segoe UI", Font.BOLD, 13), availableWidth, 2);

            JLabel nameLabel = new JLabel("<html><b>" + truncatedName + "</b></html>");
            nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            nameLabel.setVerticalAlignment(SwingConstants.CENTER);
            nameLabel.setHorizontalAlignment(SwingConstants.LEFT);
            panel.add(nameLabel, BorderLayout.CENTER);

            return panel;
        }
    }

    class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel label = new JLabel(value.toString());
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setOpaque(true);
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR),
                    new EmptyBorder(5, 10, 5, 10)
            ));

            if (value.toString().equals("Đang bán")) {
                label.setForeground(Color.decode("#2E7D32"));
                label.setBackground(Color.decode("#E8F5E9"));
            } else {
                label.setForeground(Color.decode("#C62828"));
                label.setBackground(Color.decode("#FFEBEE"));
            }

            if (isSelected) {
                label.setBackground(table.getSelectionBackground());
            }

            return label;
        }
    }

    class ActionCellRenderer extends JPanel implements TableCellRenderer {
        private JButton editButton;
        private JButton viewButton;

        public ActionCellRenderer() {
            setLayout(new GridBagLayout());
            setOpaque(true);
            setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(0, 4, 0, 4);

            editButton = new JButton("Sửa");
            editButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            editButton.setForeground(Color.WHITE);
            editButton.setBackground(Color.decode("#2196F3"));
            editButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
            editButton.setFocusPainted(false);

            viewButton = new JButton("Xem");
            viewButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            viewButton.setForeground(Color.WHITE);
            viewButton.setBackground(Color.decode("#757575"));
            viewButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
            viewButton.setFocusPainted(false);

            add(editButton, gbc);
            gbc.gridx = 1;
            add(viewButton, gbc);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }

            return this;
        }
    }

    class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;
        private JButton editButton;
        private JButton viewButton;
        private BookDTO currentBook;

        public ActionCellEditor() {
            panel = new JPanel(new GridBagLayout());
            panel.setOpaque(true);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(0, 4, 0, 4);

            editButton = new JButton("Sửa");
            editButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            editButton.setForeground(Color.WHITE);
            editButton.setBackground(Color.decode("#2196F3"));
            editButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
            editButton.setFocusPainted(false);
            editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            editButton.addActionListener(e -> {
                fireEditingStopped();
                SwingUtilities.invokeLater(() -> showEditBookDialog(currentBook));
            });

            viewButton = new JButton("Xem");
            viewButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            viewButton.setForeground(Color.WHITE);
            viewButton.setBackground(Color.decode("#757575"));
            viewButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
            viewButton.setFocusPainted(false);
            viewButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            viewButton.addActionListener(e -> {
                fireEditingStopped();
                SwingUtilities.invokeLater(() -> showBookDetail(currentBook));
            });

            panel.add(editButton, gbc);
            gbc.gridx = 1;
            panel.add(viewButton, gbc);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentBook = (BookDTO) value;
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentBook;
        }
    }
}
