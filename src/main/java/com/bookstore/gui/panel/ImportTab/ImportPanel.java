package com.bookstore.gui.panel.ImportTab;

import com.bookstore.bus.*;
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
    private JComboBox<String> cboCategory;
    private JTextField txtSearch;
    private JButton btnResetFilter;
    private JTable tblProduct;
    private DefaultTableModel productModel;
    private JLabel lbProductImage, lbBookName, lbCategory, lbPrice, lbQuantity;
    private JButton btnAddToImport, btnQuickAddBook;

    private JTable tblCart;
    private DefaultTableModel cartModel;
    private SearchableComboBox<String> cboTicketSupplier;
    private JTextField txtEmployee, txtApprover;
    private JLabel lbFinalTotal;
    private JButton btnRefresh, btnDelete, btnEdit, btnComplete;

    private CategoryBUS categoryBUS = new CategoryBUS();
    private SupplierBUS supplierBUS = new SupplierBUS();
    private BookBUS bookBUS = new BookBUS();
    private ImportBUS importBUS = new ImportBUS();

    private List<BookDTO> listBooks = new ArrayList<>();
    private double finalTotal = 0;

    public ImportPanel() {
        initUI();
    }

    @Override
    public void refresh() {
        loadCategoriesToComBoBox();
        loadSuppliersToComboBox();
        loadBookTable();
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

        JPanel pRow1 = new JPanel(new GridLayout(1,2,10,10));
        pRow1.setOpaque(false);
        cboCategory = new JComboBox<>();
        cboCategory.setBackground(Color.WHITE);
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

        JPanel pInfo = new JPanel(new GridLayout(1, 3, 10, 10));
        pInfo.setOpaque(false);

        String employeeName = "Admin (Chưa đăng nhập)";
        if (SharedData.currentUser != null) employeeName = SharedData.currentUser.getEmployeeName();

        txtEmployee = new JTextField(employeeName);
        txtEmployee.setEditable(false);
        txtEmployee.setBorder(BorderFactory.createTitledBorder("Nhân viên lập phiếu"));
        txtEmployee.setBackground(Color.decode("#F5F5F5"));
        txtEmployee.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 13));

        txtApprover = new JTextField("Hệ thống tự nhận diện khi duyệt");
        txtApprover.setEditable(false);
        txtApprover.setBorder(BorderFactory.createTitledBorder("Người duyệt phiếu"));
        txtApprover.setBackground(Color.decode("#F5F5F5"));
        txtApprover.setForeground(Color.GRAY);
        txtApprover.setFont(new Font(AppConstant.FONT_NAME, Font.ITALIC, 13));

        cboTicketSupplier = new SearchableComboBox<>();
        cboTicketSupplier.setBorder(BorderFactory.createTitledBorder("Nhà Cung Cấp"));
        cboTicketSupplier.setBackground(Color.WHITE);

        pInfo.add(cboTicketSupplier);
        pInfo.add(txtEmployee);
        pInfo.add(txtApprover);

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

    private void loadCategoriesToComBoBox() {
        List<String> list = new ArrayList<>(); list.add("0 - Tất cả thể loại");
        for (CategoryDTO c : categoryBUS.selectAllCategories()) list.add(c.getCategoryId() + " - " + c.getCategoryName());
        cboCategory.setModel(new DefaultComboBoxModel<>(list.toArray(new String[0])));
    }

    private void loadSuppliersToComboBox() {
        List<String> ticketList = new ArrayList<>();
        ticketList.add("0 - Chọn Nhà Cung Cấp...");
        for (SupplierDTO s : supplierBUS.selectAll()) {
            ticketList.add(s.getSupplierId() + " - " + s.getSupplierName());
        }
        cboTicketSupplier.updateData(ticketList);
    }

    private void loadBookTable() {
        listBooks = bookBUS.selectAllBooks();
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

    private void filterBooks() {
        if (cboTicketSupplier.getSelectedItem() == null) {
            productModel.setRowCount(0);
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
            productModel.setRowCount(0);
            return;
        }

        String keyword = txtSearch.getText().trim().toLowerCase();
        int cateId = Integer.parseInt(cboCategory.getSelectedItem().toString().split(" - ")[0]);

        productModel.setRowCount(0);

        for (BookDTO book : listBooks) {

            boolean matchKey = keyword.isEmpty() || book.getBookName().toLowerCase().contains(keyword);
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

    private void addToImport() {
        int selectedRow = tblProduct.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn sách cần nhập!"); return; }

        int bookId = (int) productModel.getValueAt(tblProduct.convertRowIndexToModel(selectedRow), 0);
        BookDTO book = listBooks.stream().filter(b -> b.getBookId() == bookId).findFirst().orElse(null);
        if (book == null) return;

        JPanel pInput = new JPanel(new GridLayout(2, 2, 10, 10));
        JTextField txtQty = new JTextField();
        JTextField txtDiscount = new JTextField("0");
        pInput.add(new JLabel("Số lượng nhập:")); pInput.add(txtQty);
        pInput.add(new JLabel("Chiết khấu NXB (%):")); pInput.add(txtDiscount);

        if (JOptionPane.showConfirmDialog(this, pInput, "Nhập số lượng và chiết khấu", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                int qty = Integer.parseInt(txtQty.getText());
                double discount = Double.parseDouble(txtDiscount.getText());
                if (qty <= 0 || discount < 0 || discount > 100 || book.getCoverPrice() < 0) throw new Exception();

                double price = book.getCoverPrice() * (1 - discount / 100.0);

                boolean exists = false;
                for (int i = 0; i < cartModel.getRowCount(); i++) {
                    if (Integer.parseInt(cartModel.getValueAt(i, 0).toString()) == bookId) {
                        int newQty = Integer.parseInt(cartModel.getValueAt(i, 2).toString()) + qty;
                        cartModel.setValueAt(newQty, i, 2);
                        cartModel.setValueAt(MoneyFormatter.toVND(price), i, 3);
                        cartModel.setValueAt(MoneyFormatter.toVND(newQty * price), i, 4);
                        cartModel.setValueAt(discount, i, 5);
                        exists = true; break;
                    }
                }
                if (!exists) cartModel.insertRow(0, new Object[]{ bookId, book.getBookName(), qty, MoneyFormatter.toVND(price), MoneyFormatter.toVND(qty * price), discount });
                calculateTotal();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Vui lòng nhập số lượng và chiết khấu hợp lệ!"); }
        }
    }

    private void editImportRow() {
        int selectedRow = tblCart.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sách cần sửa!");
            return;
        }

        int modelRow = tblCart.convertRowIndexToModel(selectedRow);
        int bookId = Integer.parseInt(cartModel.getValueAt(modelRow, 0).toString());
        BookDTO book = listBooks.stream().filter(b -> b.getBookId() == bookId).findFirst().orElse(null);
        if (book == null) return;

        JPanel pInput = new JPanel(new GridLayout(2, 2, 10, 10));
        JTextField txtQty = new JTextField(cartModel.getValueAt(modelRow, 2).toString());
        JTextField txtDiscount = new JTextField(cartModel.getValueAt(modelRow, 5).toString());
        pInput.add(new JLabel("Số lượng nhập:")); pInput.add(txtQty);
        pInput.add(new JLabel("Chiết khấu NXB (%):")); pInput.add(txtDiscount);

        if (JOptionPane.showConfirmDialog(this, pInput, "Sửa số lượng và chiết khấu", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            int qty = Integer.parseInt(txtQty.getText().trim());
            double discount = Double.parseDouble(txtDiscount.getText().trim());
            if (qty <= 0 || discount < 0 || discount > 100 || book.getCoverPrice() < 0) throw new Exception();

            double price = book.getCoverPrice() * (1 - discount / 100.0);
            cartModel.setValueAt(qty, modelRow, 2);
            cartModel.setValueAt(MoneyFormatter.toVND(price), modelRow, 3);
            cartModel.setValueAt(MoneyFormatter.toVND(qty * price), modelRow, 4);
            cartModel.setValueAt(discount, modelRow, 5);
            calculateTotal();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số lượng và chiết khấu hợp lệ!");
        }
    }

    private void calculateTotal() {
        finalTotal = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) finalTotal += MoneyFormatter.toDouble(cartModel.getValueAt(i, 4).toString());
        lbFinalTotal.setText("TỔNG TIỀN: " + MoneyFormatter.toVND(finalTotal));
    }

    private void deleteRow() {
        int row = tblCart.getSelectedRow();
        if (row != -1) { cartModel.removeRow(row); calculateTotal(); }
    }

    private void createTicket() {
        if (cartModel.getRowCount() == 0) { JOptionPane.showMessageDialog(this, "Phiếu nhập đang trống!"); return; }
        if (cboTicketSupplier.getSelectedItem() == null || cboTicketSupplier.getSelectedItem().toString().isEmpty()
            || cboTicketSupplier.getSelectedItem().toString().startsWith("0 -")) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Nhà cung cấp ở góc trên bên phải!"); return;
        }

        try {
            int supplierId = Integer.parseInt(cboTicketSupplier.getSelectedItem().toString().split(" - ")[0]);
            int employeeId = SharedData.currentUser != null ? SharedData.currentUser.getEmployeeId() : 1;

            ImportTicketDTO ticket = new ImportTicketDTO();
            ticket.setSupplierID(supplierId); ticket.setEmployeeID(employeeId);
            ticket.setTotalPrice(finalTotal); ticket.setStatus(1);
            ticket.setCreatedDate(new java.sql.Timestamp(System.currentTimeMillis()));

            ImportDetailDTO[] details = new ImportDetailDTO[cartModel.getRowCount()];
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                int bookId = Integer.parseInt(cartModel.getValueAt(i, 0).toString());
                int qty = Integer.parseInt(cartModel.getValueAt(i, 2).toString());
                double importPrice = MoneyFormatter.toDouble(cartModel.getValueAt(i, 3).toString());
                
                BookDTO book = bookBUS.selectById(bookId);
                double discount = Double.parseDouble(cartModel.getValueAt(i, 5).toString());
                details[i] = new ImportDetailDTO(0, bookId, qty, importPrice, book.getCoverPrice(), discount);
            }

            if (importBUS.importBooks(ticket, details)) {
                JOptionPane.showMessageDialog(this, "Tạo phiếu nhập thành công! Phiếu đang ở trạng thái chờ duyệt.");
                cartModel.setRowCount(0); calculateTotal();
            } else JOptionPane.showMessageDialog(this, "Lỗi khi lưu phiếu!");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void quickAddBook() {
        AuthorBUS authorBUS = new AuthorBUS();
        List<AuthorDTO> authors = authorBUS.selectAllAuthors();
        List<CategoryDTO> categories = categoryBUS.selectAllCategories();
        List<SupplierDTO> suppliers = supplierBUS.selectAll();
        java.util.Set<String> allTags = new java.util.HashSet<>();

        com.bookstore.gui.panel.ProductTab.BookFormDialog dialog = 
            new com.bookstore.gui.panel.ProductTab.BookFormDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Thêm Sách Mới", null, authors, categories, suppliers, allTags
            );
        dialog.setVisible(true);
        refresh(); // Refresh list after closed
    }

    private void addEvents() {
        cboCategory.addActionListener(e -> filterBooks());
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterBooks(); }
            public void removeUpdate(DocumentEvent e) { filterBooks(); }
            public void changedUpdate(DocumentEvent e) { filterBooks(); }
        });

        btnResetFilter.addActionListener(e -> {
            txtSearch.setText("");
            cboCategory.setSelectedIndex(0);
            cboTicketSupplier.setSelectedIndex(0);
            filterBooks();
        });

        btnAddToImport.addActionListener(e -> addToImport());
        btnQuickAddBook.addActionListener(e -> quickAddBook());
        btnRefresh.addActionListener(e -> { cartModel.setRowCount(0); calculateTotal(); });
        btnDelete.addActionListener(e -> deleteRow());
        btnEdit.addActionListener(e -> editImportRow());
        btnComplete.addActionListener(e -> createTicket());

        cboTicketSupplier.addActionListener(e -> {
            if (cboTicketSupplier.getSelectedItem() != null) {
                if (cartModel.getRowCount() > 0) {
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Bạn đã đổi Nhà Cung Cấp. Hệ thống sẽ làm trống danh sách sách hiện tại trong phiếu. Bạn đồng ý chứ?",
                            "Cảnh báo đổi NCC", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                    if (confirm == JOptionPane.YES_OPTION) {
                        cartModel.setRowCount(0);
                        calculateTotal();
                    } else {
                        return;
                    }
                }
                filterBooks();
            }
        });
    }
}