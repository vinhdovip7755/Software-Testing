package com.bookstore.gui.panel.ImportTab;

import com.bookstore.api.client.ApiException;
import com.bookstore.api.client.AuthorApiClient;
import com.bookstore.api.client.BookApiClient;
import com.bookstore.api.client.CategoryApiClient;
import com.bookstore.api.client.ImportApiClient;
import com.bookstore.api.client.SupplierApiClient;
import com.bookstore.dto.*;
import com.bookstore.util.*;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImportPanel extends JPanel implements Refreshable {
    private SearchableComboBox<String> cboCategory;
    private JTextField txtSearch;
    private JButton btnResetFilter;
    private JTable tblProduct;
    private DefaultTableModel productModel;
    private JLabel lbProductImage, lbBookName, lbCategory, lbPrice, lbQuantity;
    private JButton btnAddToImport, btnQuickAddBook;

    private JTable tblCart;
    private DefaultTableModel cartModel;
    private SearchableComboBox<String> cboTicketSupplier;
    private JTextField txtEmployee, txtDate;
    private JLabel lbFinalTotal;
    private JButton btnRefresh, btnDelete, btnEdit, btnComplete;

    private final CategoryApiClient categoryApiClient = new CategoryApiClient();
    private final SupplierApiClient supplierApiClient = new SupplierApiClient();
    private final BookApiClient bookApiClient = new BookApiClient();
    private final AuthorApiClient authorApiClient = new AuthorApiClient();
    private final ImportApiClient importApiClient = new ImportApiClient();

    private static final String DEFAULT_SUPPLIER_ITEM = "0 - Chọn Nhà Cung Cấp...";
    private static final String DEFAULT_CATEGORY_ITEM = "0 - Tất cả thể loại";

    private static class CartItem {
        final int bookId;
        final String bookName;
        int quantity;
        double coverPrice;
        double discountPercent;
        double importPrice;

        CartItem(int bookId, String bookName, int quantity, double coverPrice, double discountPercent) {
            this.bookId = bookId;
            this.bookName = bookName;
            update(quantity, coverPrice, discountPercent);
        }

        void update(int quantity, double coverPrice, double discountPercent) {
            this.quantity = quantity;
            this.coverPrice = coverPrice;
            this.discountPercent = discountPercent;
            this.importPrice = coverPrice * (1.0 - discountPercent / 100.0);
        }

        double getLineTotal() {
            return quantity * importPrice;
        }
    }

    private List<BookDTO> listBooks = new ArrayList<>();
    private final List<CartItem> cartItems = new ArrayList<>();
    private List<String> supplierItems = new ArrayList<>();
    private double finalTotal = 0;
    private String previousSupplierItem = DEFAULT_SUPPLIER_ITEM;
    private boolean isUpdatingCombos = false;

    public ImportPanel() {
        initUI();
    }

    private void applyPermissions() {
        boolean canManageImport = PermissionUtil.hasActionPermission("MANAGE_IMPORT");
        if (btnQuickAddBook != null) {
            btnQuickAddBook.setVisible(canManageImport);
            btnQuickAddBook.setEnabled(canManageImport);
        }
        if (btnAddToImport != null) {
            btnAddToImport.setVisible(canManageImport);
            btnAddToImport.setEnabled(canManageImport);
        }
        if (btnRefresh != null) {
            btnRefresh.setVisible(canManageImport);
            btnRefresh.setEnabled(canManageImport);
        }
        if (btnDelete != null) {
            btnDelete.setVisible(canManageImport);
            btnDelete.setEnabled(canManageImport);
        }
        if (btnEdit != null) {
            btnEdit.setVisible(canManageImport);
            btnEdit.setEnabled(canManageImport);
        }
        if (btnComplete != null) {
            btnComplete.setVisible(canManageImport);
            btnComplete.setEnabled(canManageImport);
        }
    }

    @Override
    public void refresh() {
        applyPermissions();
        if (SharedData.currentUser != null && txtEmployee != null) {
            txtEmployee.setText(SharedData.currentUser.getEmployeeName());
        }
        if (txtDate != null) {
            txtDate.setText(new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date()));
        }
        try {
            String preservedSupplier = previousSupplierItem;
            if (cboTicketSupplier != null && cboTicketSupplier.getSelectedItem() != null) {
                String currentSel = cboTicketSupplier.getSelectedItem().toString();
                if (parseLeadingId(currentSel) > 0) {
                    preservedSupplier = currentSel;
                }
            }
            String preservedCategory = (cboCategory != null && cboCategory.getSelectedItem() != null)
                    ? cboCategory.getSelectedItem().toString()
                    : DEFAULT_CATEGORY_ITEM;

            isUpdatingCombos = true;
            try {
                loadSuppliersToComboBox(preservedSupplier);
                loadCategoriesToComBoBox(preservedCategory);
                int currentSupId = parseLeadingId(previousSupplierItem);
                if (cboCategory != null) {
                    cboCategory.setEnabled(currentSupId > 0);
                }
            } finally {
                isUpdatingCombos = false;
            }
            loadBookTable();
        } catch (ApiException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi kết nối API", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initUI() {
        setLayout(new GridLayout(1, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        setBackground(Color.WHITE);

        add(createLeftPanel());
        add(createRightPanel());

        refresh();
        addEvents();
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JPanel pFilter = new JPanel(new GridLayout(2, 1, 10, 10));
        pFilter.setOpaque(false);

        JPanel pRow1 = new JPanel(new GridLayout(1, 2, 10, 10));
        pRow1.setOpaque(false);

        cboTicketSupplier = new SearchableComboBox<>();
        cboTicketSupplier.setBorder(BorderFactory.createTitledBorder("Nhà Cung Cấp (*)"));
        cboTicketSupplier.setBackground(Color.WHITE);

        cboCategory = new SearchableComboBox<>();
        cboCategory.setBorder(BorderFactory.createTitledBorder("Thể loại"));
        cboCategory.setBackground(Color.WHITE);
        cboCategory.setEnabled(false);

        pRow1.add(cboTicketSupplier);
        pRow1.add(cboCategory);
        pFilter.add(pRow1);

        JPanel pSearchRow = new JPanel(new BorderLayout(10, 0));
        pSearchRow.setOpaque(false);
        txtSearch = new JTextField();
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm kiếm theo tên hoặc mã sách...");
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSVGIcon("icon/search_icon.svg").derive(20,20));
        btnResetFilter = createActionButton("Làm mới", AppConstant.GREEN_COLOR_CODE);
        btnQuickAddBook = createActionButton("+ Thêm Nhanh Sách", "#1976D2");
        JPanel pActionBtns = new JPanel(new GridLayout(1, 2, 5, 0));
        pActionBtns.setOpaque(false);
        pActionBtns.add(btnQuickAddBook);
        pActionBtns.add(btnResetFilter);
        
        pSearchRow.add(txtSearch, BorderLayout.CENTER);
        pSearchRow.add(pActionBtns, BorderLayout.EAST);
        pFilter.add(pSearchRow);

        String[] headers = {"Mã", "Tên sách", "Thể loại", "SL Tồn"};
        productModel = new DefaultTableModel(headers, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblProduct = new JTable(productModel);
        tblProduct.setRowHeight(30);
        tblProduct.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        styleTable(tblProduct);
        setColumnWidth(tblProduct, 1, 250);
        setColumnWidth(tblProduct, 3, 70);
        tblProduct.getColumnModel().removeColumn(tblProduct.getColumnModel().getColumn(0));

        tblProduct.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblProduct.getSelectedRow() != -1) fillProductDetail();
        });

        JPanel pDetail = new JPanel(new BorderLayout(10, 0));
        pDetail.setBackground(Color.WHITE);
        pDetail.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        lbProductImage = new JLabel("Ảnh", SwingConstants.CENTER);
        lbProductImage.setPreferredSize(new Dimension(110, 150));
        lbProductImage.setOpaque(true);
        lbProductImage.setBackground(Color.decode("#EEEEEE"));

        lbBookName = new JLabel("Chọn sách để nhập");
        lbCategory = new JLabel("-");
        lbPrice = new JLabel("-");
        lbQuantity = new JLabel("-");
        Font lbDetailFont = new Font(AppConstant.FONT_NAME, Font.BOLD, 14);
        lbBookName.setFont(lbDetailFont); lbCategory.setFont(lbDetailFont);
        lbPrice.setFont(lbDetailFont); lbQuantity.setFont(lbDetailFont);

        JPanel pInfo = new JPanel(new GridLayout(2, 2, 10, 5));
        pInfo.setBackground(Color.WHITE);
        pInfo.add(createDetailLabel("Tên sách:", lbBookName));
        pInfo.add(createDetailLabel("Thể loại:", lbCategory));
        pInfo.add(createDetailLabel("Giá bán hiện tại:", lbPrice));
        pInfo.add(createDetailLabel("Số lượng tồn kho:", lbQuantity));

        btnAddToImport = createActionButton("Thêm vào phiếu nhập", AppConstant.GREEN_COLOR_CODE);
        btnAddToImport.setPreferredSize(new Dimension(0, 40));

        JPanel pDetailRight = new JPanel(new BorderLayout(0, 10));
        pDetailRight.setOpaque(false);
        pDetailRight.add(pInfo, BorderLayout.CENTER);
        pDetailRight.add(btnAddToImport, BorderLayout.SOUTH);

        pDetail.add(lbProductImage, BorderLayout.WEST);
        pDetail.add(pDetailRight, BorderLayout.CENTER);

        panel.add(pFilter, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblProduct), BorderLayout.CENTER);
        panel.add(pDetail, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JPanel pInfo = new JPanel(new GridLayout(1, 2, 10, 10));
        pInfo.setOpaque(false);

        String employeeName = "Admin (Chưa đăng nhập)";
        if (SharedData.currentUser != null) employeeName = SharedData.currentUser.getEmployeeName();

        txtEmployee = new JTextField(employeeName);
        txtEmployee.setEditable(false);
        txtEmployee.setBorder(BorderFactory.createTitledBorder("Nhân viên lập phiếu"));
        txtEmployee.setBackground(Color.decode("#F5F5F5"));
        txtEmployee.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 13));

        txtDate = new JTextField(new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date()));
        txtDate.setEditable(false);
        txtDate.setBorder(BorderFactory.createTitledBorder("Ngày lập phiếu"));
        txtDate.setBackground(Color.decode("#F5F5F5"));
        txtDate.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 13));

        pInfo.add(txtEmployee);
        pInfo.add(txtDate);

        JPanel pActions = new JPanel(new GridLayout(1, 3, 10, 0));
        pActions.setOpaque(false);
        btnRefresh = createActionButton("Làm mới phiếu", AppConstant.GREEN_COLOR_CODE);
        btnDelete = createActionButton("Xóa sách", "#E53935");
        btnEdit = createActionButton("Sửa số lượng/% CK", "#FBC02D");
        pActions.add(btnRefresh); pActions.add(btnDelete); pActions.add(btnEdit);

        JPanel pTopRight = new JPanel(new BorderLayout(0, 10));
        pTopRight.setOpaque(false);
        pTopRight.add(pInfo, BorderLayout.CENTER);
        pTopRight.add(pActions, BorderLayout.SOUTH);

        String[] cartHeaders = {"Mã", "Tên sách", "SL Nhập", "Giá Nhập", "Thành tiền", "CK NXB"};
        cartModel = new DefaultTableModel(cartHeaders, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblCart = new JTable(cartModel);
        tblCart.setRowHeight(30);
        tblCart.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        styleTable(tblCart);
        setColumnWidth(tblCart, 2, 70);
        setColumnWidth(tblCart, 3, 100);
        setColumnWidth(tblCart, 4, 120);
        tblCart.getColumnModel().removeColumn(tblCart.getColumnModel().getColumn(0));
        tblCart.getColumnModel().removeColumn(tblCart.getColumnModel().getColumn(4));

        JPanel pSummary = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pSummary.setBackground(Color.WHITE);
        lbFinalTotal = new JLabel("TỔNG TIỀN: 0đ");
        lbFinalTotal.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 22));
        lbFinalTotal.setForeground(Color.RED);
        pSummary.add(lbFinalTotal);

        btnComplete = createActionButton("Hoàn Tất Tạo Phiếu", AppConstant.GREEN_COLOR_CODE);
        btnComplete.setPreferredSize(new Dimension(0, 50));
        btnComplete.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 18));

        JPanel pBottomRight = new JPanel(new BorderLayout(0, 10));
        pBottomRight.setOpaque(false);
        pBottomRight.add(pSummary, BorderLayout.CENTER);
        pBottomRight.add(btnComplete, BorderLayout.SOUTH);

        panel.add(pTopRight, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblCart), BorderLayout.CENTER);
        panel.add(pBottomRight, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createDetailLabel(String title, JComponent value) {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
        JLabel lbTitle = new JLabel(title); lbTitle.setForeground(Color.GRAY);
        p.add(lbTitle, BorderLayout.NORTH); p.add(value, BorderLayout.CENTER);
        return p;
    }

    private JButton createActionButton(String text, String colorHex) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(100, 50));
        btn.setForeground(Color.WHITE); btn.setBackground(Color.decode(colorHex));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
        return btn;
    }

    private void styleTable(JTable table) {
        JTableHeader header = table.getTableHeader();
        header.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        header.setForeground(Color.WHITE);
        header.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 13));
        table.setSelectionBackground(Color.decode("#d4ffee"));
    }

    private void setColumnWidth(JTable table, int colIndex, int width) {
        table.getColumnModel().getColumn(colIndex).setPreferredWidth(width);
    }

    private int parseLeadingId(String item) {
        if (item == null || item.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(item.split(" - ")[0].trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private void selectComboItemSilently(SearchableComboBox<String> combo, String item) {
        combo.setSelectedItem(item);
        if (combo.getEditor() != null && combo.getEditor().getEditorComponent() instanceof JTextField tf) {
            tf.setText(item != null ? item : "");
        }
    }

    private void loadCategoriesToComBoBox(String preservedCategory) {
        List<String> list = new ArrayList<>();
        list.add(DEFAULT_CATEGORY_ITEM);
        int preservedId = parseLeadingId(preservedCategory);
        String matched = DEFAULT_CATEGORY_ITEM;
        for (CategoryDTO c : categoryApiClient.selectAllCategories()) {
            String entry = c.getCategoryId() + " - " + c.getCategoryName();
            list.add(entry);
            if (preservedId > 0 && c.getCategoryId() == preservedId) {
                matched = entry;
            }
        }
        cboCategory.updateData(list);
        selectComboItemSilently(cboCategory, matched);
    }

    private void loadSuppliersToComboBox(String preservedSupplier) {
        List<String> ticketList = new ArrayList<>();
        ticketList.add(DEFAULT_SUPPLIER_ITEM);
        int preservedId = parseLeadingId(preservedSupplier);
        String matched = DEFAULT_SUPPLIER_ITEM;
        for (SupplierDTO s : supplierApiClient.selectAll()) {
            String entry = s.getSupplierId() + " - " + s.getSupplierName();
            ticketList.add(entry);
            if (preservedId > 0 && s.getSupplierId() == preservedId) {
                matched = entry;
            }
        }
        supplierItems = ticketList;
        cboTicketSupplier.updateData(ticketList);
        selectComboItemSilently(cboTicketSupplier, matched);
        previousSupplierItem = matched;
    }

    private void revertSupplierSelection() {
        isUpdatingCombos = true;
        try {
            if (!supplierItems.isEmpty()) {
                cboTicketSupplier.updateData(supplierItems);
            }
            selectComboItemSilently(cboTicketSupplier, previousSupplierItem);
        } finally {
            isUpdatingCombos = false;
        }
        if (!previousSupplierItem.equals(String.valueOf(cboTicketSupplier.getSelectedItem()))) {
            SwingUtilities.invokeLater(() -> {
                isUpdatingCombos = true;
                try {
                    if (!supplierItems.isEmpty()) {
                        cboTicketSupplier.updateData(supplierItems);
                    }
                    selectComboItemSilently(cboTicketSupplier, previousSupplierItem);
                } finally {
                    isUpdatingCombos = false;
                }
            });
        }
    }

    private void loadBookTable() {
        listBooks = bookApiClient.selectAllBooks();
        filterBooks();
    }

    private void fillProductDetail() {
        int selectedRow = tblProduct.getSelectedRow();
        if (selectedRow == -1) return;
        int bookId = (int) productModel.getValueAt(tblProduct.convertRowIndexToModel(selectedRow), 0);
        BookDTO book = listBooks.stream().filter(b -> b.getBookId() == bookId).findFirst().orElse(null);
        if (book != null) {
            lbBookName.setText("<html>" + book.getBookName() + "</html>");
            lbCategory.setText(book.getCategoryName());
            lbPrice.setText(MoneyFormatter.toVND(book.getSellingPrice()));
            lbQuantity.setText(String.valueOf(book.getQuantity()));

            if (book.getImage() != null && !book.getImage().isEmpty()) {
                File file = new File("data/book_covers/" + book.getImage());
                if (file.exists()) lbProductImage.setIcon(new ImageIcon(new ImageIcon(file.getAbsolutePath()).getImage().getScaledInstance(110, 150, Image.SCALE_SMOOTH)));
                else lbProductImage.setIcon(null);
            } else lbProductImage.setIcon(null);
        }
    }

    private void clearProductDetail() {
        if (lbBookName != null) lbBookName.setText("Chọn sách để nhập");
        if (lbCategory != null) lbCategory.setText("-");
        if (lbPrice != null) lbPrice.setText("-");
        if (lbQuantity != null) lbQuantity.setText("-");
        if (lbProductImage != null) lbProductImage.setIcon(null);
        if (tblProduct != null && tblProduct.getSelectedRow() != -1) {
            tblProduct.clearSelection();
        }
    }

    private void filterBooks() {
        if (cboTicketSupplier == null || cboTicketSupplier.getSelectedItem() == null) {
            if (cboCategory != null) cboCategory.setEnabled(false);
            productModel.setRowCount(0);
            clearProductDetail();
            return;
        }

        String supStr = cboTicketSupplier.getSelectedItem().toString();
        int supId = 0;
        try {
            supId = Integer.parseInt(supStr.split(" - ")[0]);
        } catch (Exception e) {
            supId = 0;
        }

        if (supId == 0) {
            if (cboCategory != null) cboCategory.setEnabled(false);
            productModel.setRowCount(0);
            clearProductDetail();
            return;
        }

        if (cboCategory != null) {
            cboCategory.setEnabled(true);
        }

        String keyword = txtSearch.getText().trim().toLowerCase();
        int cateId = 0;
        if (cboCategory.getSelectedItem() != null) {
            try {
                cateId = Integer.parseInt(cboCategory.getSelectedItem().toString().split(" - ")[0]);
            } catch (Exception e) {
                cateId = 0;
            }
        }

        productModel.setRowCount(0);
        clearProductDetail();

        for (BookDTO book : listBooks) {
            String bookName = book.getBookName() != null ? book.getBookName().toLowerCase() : "";
            boolean matchKey = keyword.isEmpty() || bookName.contains(keyword)
                    || String.valueOf(book.getBookId()).contains(keyword);
            boolean matchCate = (cateId == 0) || (book.getCategoryId() == cateId);
            boolean matchSupplier = (book.getSupplierId() == supId);

            if (matchKey && matchCate && matchSupplier) {
                productModel.addRow(new Object[]{
                        book.getBookId(),
                        book.getBookName(),
                        book.getCategoryName(),
                        book.getQuantity()
                });
            }
        }
    }

    @SuppressWarnings("unused")
    private void fillAllBook(){
        productModel.setRowCount(0);
        for (BookDTO book : listBooks) {
            productModel.addRow(new Object[]{
                    book.getBookId(),
                    book.getBookName(),
                    book.getCategoryName(),
                    book.getQuantity()
            });
        }
    }

    private int findCartItemIndex(int bookId) {
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).bookId == bookId) {
                return i;
            }
        }
        return -1;
    }

    private void clearCart() {
        cartItems.clear();
        cartModel.setRowCount(0);
        calculateTotal();
    }

    private void addToImport() {
        if (!PermissionUtil.hasActionPermission("MANAGE_IMPORT")) {
            return;
        }
        int selectedRow = tblProduct.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn sách cần nhập!"); return; }

        int bookId = (int) productModel.getValueAt(tblProduct.convertRowIndexToModel(selectedRow), 0);
        BookDTO book = listBooks.stream().filter(b -> b.getBookId() == bookId).findFirst().orElse(null);
        if (book == null) return;

        if (!Double.isFinite(book.getCoverPrice()) || book.getCoverPrice() <= 0) {
            JOptionPane.showMessageDialog(this, "Sách '" + book.getBookName() + "' chưa có giá bìa hợp lệ! Vui lòng kiểm tra lại thông tin sách.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int existingIndex = findCartItemIndex(bookId);
        double initialDiscount = existingIndex != -1 ? cartItems.get(existingIndex).discountPercent : 0;

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        ImportQuantityDiscountDialog dialog = new ImportQuantityDiscountDialog(parentWindow, "Nhập số lượng và chiết khấu", 0, initialDiscount);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            int qty = dialog.getQuantity();
            double discount = dialog.getDiscount();

            if (existingIndex != -1) {
                CartItem existing = cartItems.get(existingIndex);
                int newQty;
                try {
                    newQty = Math.addExact(existing.quantity, qty);
                } catch (ArithmeticException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Tổng số lượng nhập vượt quá giới hạn cho phép (" + Integer.MAX_VALUE + ")!",
                            "Lỗi số lượng", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double effectiveDiscount = existing.discountPercent;
                if (Double.compare(discount, existing.discountPercent) != 0) {
                    int choice = JOptionPane.showConfirmDialog(this,
                            String.format("Sách '%s' đã có trong phiếu với chiết khấu %.2f%%.\n"
                                    + "Bạn có muốn áp dụng chiết khấu mới (%.2f%%) cho toàn bộ %d cuốn không?\n"
                                    + "Chọn 'No' để giữ nguyên chiết khấu cũ (%.2f%%).",
                                    book.getBookName(), existing.discountPercent, discount, newQty, existing.discountPercent),
                            "Xác nhận chiết khấu", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
                        return;
                    }
                    if (choice == JOptionPane.YES_OPTION) {
                        effectiveDiscount = discount;
                    }
                }

                existing.update(newQty, book.getCoverPrice(), effectiveDiscount);
                cartModel.setValueAt(existing.quantity, existingIndex, 2);
                cartModel.setValueAt(MoneyFormatter.toVND(existing.importPrice), existingIndex, 3);
                cartModel.setValueAt(MoneyFormatter.toVND(existing.getLineTotal()), existingIndex, 4);
                cartModel.setValueAt(existing.discountPercent, existingIndex, 5);
            } else {
                CartItem item = new CartItem(bookId, book.getBookName(), qty, book.getCoverPrice(), discount);
                cartItems.add(0, item);
                cartModel.insertRow(0, new Object[]{
                        item.bookId,
                        item.bookName,
                        item.quantity,
                        MoneyFormatter.toVND(item.importPrice),
                        MoneyFormatter.toVND(item.getLineTotal()),
                        item.discountPercent
                });
            }
            calculateTotal();
        }
    }

    private void editImportRow() {
        if (!PermissionUtil.hasActionPermission("MANAGE_IMPORT")) {
            return;
        }
        int selectedRow = tblCart.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sách cần sửa!");
            return;
        }

        int modelRow = tblCart.convertRowIndexToModel(selectedRow);
        if (modelRow < 0 || modelRow >= cartItems.size()) {
            return;
        }
        CartItem item = cartItems.get(modelRow);
        BookDTO book = listBooks.stream().filter(b -> b.getBookId() == item.bookId).findFirst().orElse(null);
        double coverPrice = (book != null && Double.isFinite(book.getCoverPrice()) && book.getCoverPrice() > 0)
                ? book.getCoverPrice()
                : item.coverPrice;

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        ImportQuantityDiscountDialog dialog = new ImportQuantityDiscountDialog(
                parentWindow, "Sửa số lượng và chiết khấu", item.quantity, item.discountPercent);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            item.update(dialog.getQuantity(), coverPrice, dialog.getDiscount());
            cartModel.setValueAt(item.quantity, modelRow, 2);
            cartModel.setValueAt(MoneyFormatter.toVND(item.importPrice), modelRow, 3);
            cartModel.setValueAt(MoneyFormatter.toVND(item.getLineTotal()), modelRow, 4);
            cartModel.setValueAt(item.discountPercent, modelRow, 5);
            calculateTotal();
        }
    }

    private void calculateTotal() {
        finalTotal = 0;
        for (CartItem item : cartItems) {
            finalTotal += item.getLineTotal();
        }
        lbFinalTotal.setText("TỔNG TIỀN: " + MoneyFormatter.toVND(finalTotal));
    }

    private void deleteRow() {
        if (!PermissionUtil.hasActionPermission("MANAGE_IMPORT")) {
            return;
        }
        int row = tblCart.getSelectedRow();
        if (row != -1) {
            int modelRow = tblCart.convertRowIndexToModel(row);
            if (modelRow >= 0 && modelRow < cartItems.size()) {
                cartItems.remove(modelRow);
                cartModel.removeRow(modelRow);
                calculateTotal();
            }
        }
    }

    private void createTicket() {
        if (!PermissionUtil.hasActionPermission("MANAGE_IMPORT")) {
            return;
        }
        if (cartItems.isEmpty()) { JOptionPane.showMessageDialog(this, "Phiếu nhập đang trống!"); return; }
        int supplierId = parseLeadingId(cboTicketSupplier.getSelectedItem() != null
                ? cboTicketSupplier.getSelectedItem().toString()
                : null);
        if (supplierId <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Nhà cung cấp!"); return;
        }

        long totalQty = 0;
        for (CartItem item : cartItems) {
            totalQty += item.quantity;
            if (totalQty > Integer.MAX_VALUE) {
                JOptionPane.showMessageDialog(this, "Tổng số lượng sách trong phiếu vượt quá giới hạn cho phép!", "Lỗi tạo phiếu nhập", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        try {
            ImportTicketDTO ticket = new ImportTicketDTO();
            ticket.setSupplierID(supplierId);
            ticket.setStatus(1);
            ticket.setCreatedDate(new java.sql.Timestamp(System.currentTimeMillis()));

            ImportDetailDTO[] details = new ImportDetailDTO[cartItems.size()];
            for (int i = 0; i < cartItems.size(); i++) {
                CartItem item = cartItems.get(i);
                BookDTO book = bookApiClient.selectById(item.bookId);
                double coverPrice = (book != null && Double.isFinite(book.getCoverPrice()) && book.getCoverPrice() > 0)
                        ? book.getCoverPrice()
                        : item.coverPrice;
                double importPrice = coverPrice * (1.0 - item.discountPercent / 100.0);
                details[i] = new ImportDetailDTO(0, item.bookId, item.quantity, importPrice, coverPrice, item.discountPercent);
            }

            importApiClient.createImport(ticket, details);
            JOptionPane.showMessageDialog(this, "Tạo phiếu nhập thành công! Phiếu đang ở trạng thái chờ duyệt.");
            clearCart();
        } catch (ApiException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi tạo phiếu nhập", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void quickAddBook() {
        if (!PermissionUtil.hasActionPermission("MANAGE_IMPORT")) {
            return;
        }
        try {
            List<AuthorDTO> authors = authorApiClient.selectAllAuthors();
            List<CategoryDTO> categories = categoryApiClient.selectAllCategories();
            List<SupplierDTO> suppliers = supplierApiClient.selectAll();
            java.util.Set<String> allTags = new java.util.HashSet<>();

            Window owner = SwingUtilities.getWindowAncestor(this);
            JFrame ownerFrame = (owner instanceof JFrame frame) ? frame : null;
            com.bookstore.gui.panel.ProductTab.BookFormDialog dialog =
                    new com.bookstore.gui.panel.ProductTab.BookFormDialog(
                            ownerFrame,
                            "Thêm Sách Mới", null, authors, categories, suppliers, allTags
                    );
            dialog.setVisible(true);

            if (dialog.isSaved() && dialog.getBook() != null) {
                String result = bookApiClient.addBookForImport(dialog.getBook());
                if ("OK".equals(result) || result.toLowerCase().contains("thành công") || result.toLowerCase().contains("công")) {
                    JOptionPane.showMessageDialog(this, result, "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    refresh();
                } else {
                    JOptionPane.showMessageDialog(this, result, "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (ApiException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi kết nối API", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addEvents() {
        cboCategory.addActionListener(e -> {
            if (!isUpdatingCombos) {
                filterBooks();
            }
        });
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterBooks(); }
            public void removeUpdate(DocumentEvent e) { filterBooks(); }
            public void changedUpdate(DocumentEvent e) { filterBooks(); }
        });

        btnResetFilter.addActionListener(e -> {
            if (!cartItems.isEmpty()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Làm mới bộ lọc sẽ bỏ chọn Nhà Cung Cấp và làm trống danh sách sách hiện tại trong phiếu. Bạn đồng ý chứ?",
                        "Cảnh báo đổi NCC", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
                clearCart();
            }
            isUpdatingCombos = true;
            try {
                txtSearch.setText("");
                cboTicketSupplier.resetSelection();
                cboCategory.resetSelection();
                previousSupplierItem = cboTicketSupplier.getSelectedItem() != null
                        ? cboTicketSupplier.getSelectedItem().toString()
                        : DEFAULT_SUPPLIER_ITEM;
                if (cboCategory != null) cboCategory.setEnabled(false);
            } finally {
                isUpdatingCombos = false;
            }
            filterBooks();
        });

        btnAddToImport.addActionListener(e -> addToImport());
        btnQuickAddBook.addActionListener(e -> quickAddBook());
        btnRefresh.addActionListener(e -> {
            if (PermissionUtil.hasActionPermission("MANAGE_IMPORT")) {
                clearCart();
            }
        });
        btnDelete.addActionListener(e -> deleteRow());
        btnEdit.addActionListener(e -> editImportRow());
        btnComplete.addActionListener(e -> createTicket());

        cboTicketSupplier.addActionListener(e -> {
            if (isUpdatingCombos || cboTicketSupplier.getSelectedItem() == null) {
                return;
            }
            String supStr = cboTicketSupplier.getSelectedItem().toString();
            int supId = parseLeadingId(supStr);
            int prevSupId = parseLeadingId(previousSupplierItem);

            if (supId == prevSupId && supStr.equals(previousSupplierItem)) {
                return;
            }

            if (!cartItems.isEmpty() && supId != prevSupId) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Bạn đã đổi Nhà Cung Cấp. Hệ thống sẽ làm trống danh sách sách hiện tại trong phiếu. Bạn đồng ý chứ?",
                        "Cảnh báo đổi NCC", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    clearCart();
                } else {
                    revertSupplierSelection();
                    return;
                }
            }

            previousSupplierItem = supStr;
            if (supId == 0) {
                isUpdatingCombos = true;
                try {
                    cboCategory.resetSelection();
                    cboCategory.setEnabled(false);
                } finally {
                    isUpdatingCombos = false;
                }
            } else {
                cboCategory.setEnabled(true);
            }
            filterBooks();
        });
    }
}