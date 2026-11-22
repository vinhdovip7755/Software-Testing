package com.bookstore.gui.panel.PriceTab;

import com.bookstore.api.client.ApiException;
import com.bookstore.api.client.CategoryApiClient;
import com.bookstore.api.client.PromotionApiClient;
import com.bookstore.api.contract.PromotionSaveRequestDTO;
import com.bookstore.dto.*;
import javax.swing.*;
import com.toedter.calendar.JDateChooser;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import com.bookstore.util.AppConstant;

import java.awt.*;
import java.awt.event.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PromotionDialog extends JDialog {
    private JComboBox<CategoryDTO> cbCategory;
    private final CategoryApiClient categoryApiClient = new CategoryApiClient();
    private final PromotionApiClient promotionApiClient = new PromotionApiClient();
    private JTextField txtID, txtTen, txtPercent, txtSearchBook;
    private JDateChooser dchStart, dchEnd;
    private JTable bookTable;
    private DefaultTableModel bookModel;
    private PromotionDTO data;
    private boolean isEdit;
    private List<Integer> currentlySelectedIds = new ArrayList<>();
    private JComboBox<String> cbStatus = new JComboBox<>();

    public PromotionDialog(JFrame parent, String title, PromotionDTO dto) {
        super(parent, title, true);
        this.data = dto;
        this.isEdit = (dto != null);
        initComponents();

        txtSearchBook.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (cbCategory.getSelectedIndex() != 0) {
                    cbCategory.setSelectedIndex(0);
                } else {
                    refreshBookList();
                }
            }
        });

        cbCategory.addActionListener(e -> refreshBookList());

        if (isEdit) {
            fillData();
        } else {
            txtID.setText("Tự động tạo");
            txtID.setFont(new Font(txtID.getFont().getName(), Font.ITALIC, txtID.getFont().getSize()));
            txtID.setForeground(Color.GRAY);
        }
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setSize(850, 700);
        setLayout(new BorderLayout(15, 15));

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtID = new JTextField();
        txtID.setEditable(false);
        txtTen = new JTextField();
        txtPercent = new JTextField();
        txtSearchBook = new JTextField();
        Date defaultDate = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        dchStart = new JDateChooser(defaultDate);
        dchStart.setDateFormatString("dd/MM/yyyy");
        dchStart.setMinSelectableDate(AppConstant.SYSTEM_MIN_DATE);
        dchStart.setMaxSelectableDate(AppConstant.SYSTEM_MAX_DATE);
        dchEnd = new JDateChooser(defaultDate);
        dchEnd.setDateFormatString("dd/MM/yyyy");
        dchEnd.setMinSelectableDate(AppConstant.SYSTEM_MIN_DATE);
        dchEnd.setMaxSelectableDate(AppConstant.SYSTEM_MAX_DATE);
        cbStatus = new JComboBox<>(new String[] { "Ngừng hoạt động", "Đang chạy" });
        cbCategory = new JComboBox<>();
        cbCategory.addItem(new CategoryDTO(0, "Tất cả thể loại"));
        try {
            for (CategoryDTO category : categoryApiClient.selectAllCategories()) {
                cbCategory.addItem(category);
            }
        } catch (ApiException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Không thể tải thể loại", JOptionPane.ERROR_MESSAGE);
        }

        addComponent(infoPanel, new JLabel("Mã KM:"), 0, 0, 0, 0);
        addComponent(infoPanel, txtID, 1, 0, 1.0, 0);
        addComponent(infoPanel, new JLabel("Tên chương trình:"), 2, 0, 0, 0);
        addComponent(infoPanel, txtTen, 3, 0, 1.0, 0);

        addComponent(infoPanel, new JLabel("Phần trăm (%):"), 0, 1, 0, 0);
        addComponent(infoPanel, txtPercent, 1, 1, 1.0, 0);
        addComponent(infoPanel, new JLabel("Tìm sách:"), 2, 1, 0, 0);
        addComponent(infoPanel, txtSearchBook, 3, 1, 1.0, 0);

        addComponent(infoPanel, new JLabel("Thể loại:"), 0, 2, 0, 0);
        addComponent(infoPanel, cbCategory, 1, 2, 1.0, 0);
        addComponent(infoPanel, new JLabel("Ngày bắt đầu:"), 2, 2, 0, 0);
        addComponent(infoPanel, dchStart, 3, 2, 1.0, 0);

        addComponent(infoPanel, new JLabel("Ngày kết thúc:"), 0, 3, 0, 0);
        addComponent(infoPanel, dchEnd, 1, 3, 1.0, 0);
        addComponent(infoPanel, new JLabel("Trạng thái:"), 2, 3, 0, 0);
        addComponent(infoPanel, cbStatus, 3, 3, 1.0, 0);

        String[] cols = { "Chọn", "Mã sách", "Tên sách", "Thể loại" };
        bookModel = new DefaultTableModel(cols, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 0) ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 0;
            }
        };
        bookTable = new JTable(bookModel);

        TableColumnModel columnModel = bookTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(0).setMaxWidth(50);
        columnModel.getColumn(0).setMinWidth(50);
        columnModel.getColumn(1).setPreferredWidth(70);
        columnModel.getColumn(1).setMaxWidth(100);
        columnModel.getColumn(2).setPreferredWidth(450);
        columnModel.getColumn(3).setPreferredWidth(200);

        bookTable.setShowGrid(true);
        bookTable.setGridColor(new Color(200, 200, 200));
        bookTable.setIntercellSpacing(new Dimension(1, 1));

        refreshBookList();

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        JButton btnSave = new JButton("Lưu dữ liệu");
        btnSave.setBackground(new Color(35, 90, 180));
        btnSave.setForeground(Color.WHITE);
        btnSave.setPreferredSize(new Dimension(120, 35));
        btnSave.addActionListener(e -> saveAction());

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setPreferredSize(new Dimension(100, 35));
        btnCancel.addActionListener(e -> dispose());
        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        JCheckBox chkSelectAll = new JCheckBox("Chọn tất cả sách trong danh sách bên dưới");
        chkSelectAll.setFont(new java.awt.Font(com.bookstore.util.AppConstant.FONT_NAME, java.awt.Font.BOLD, 13));
        chkSelectAll.addActionListener(e -> {
            boolean isSelected = chkSelectAll.isSelected();
            for (int i = 0; i < bookModel.getRowCount(); i++) {
                bookModel.setValueAt(isSelected, i, 0);
            }
            saveCurrentSelection();
        });
        
        JPanel tableWrapper = new JPanel(new java.awt.BorderLayout(0, 5));
        tableWrapper.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        tableWrapper.add(chkSelectAll, java.awt.BorderLayout.NORTH);
        tableWrapper.add(new JScrollPane(bookTable), java.awt.BorderLayout.CENTER);

        add(infoPanel, java.awt.BorderLayout.NORTH);
        add(tableWrapper, java.awt.BorderLayout.CENTER);
        add(btnPanel, java.awt.BorderLayout.SOUTH);
    }

    private void loadBookList(List<BookDTO> books) {
        bookModel.setRowCount(0);
        if (books == null || books.isEmpty()) {
            return;
        }
        for (BookDTO b : books) {
            bookModel.addRow(new Object[] { false, b.getBookId(), b.getBookName(), b.getCategoryName() });
        }
        bookModel.fireTableDataChanged();
    }

    private void refreshBookList() {
        saveCurrentSelection();
        CategoryDTO selectedCategory = (CategoryDTO) cbCategory.getSelectedItem();
        int categoryId = selectedCategory == null ? 0 : selectedCategory.getCategoryId();
        try {
            loadBookList(promotionApiClient.suggestBooks(txtSearchBook.getText().trim(), categoryId));
            restoreSelection();
        } catch (ApiException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Không thể tải danh sách sách", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addComponent(JPanel panel, Component comp, int x, int y, double weightx, int gridwidth) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = weightx;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(comp, gbc);
    }

    private void fillData() {
        txtID.setText("KM" + data.getPromotionId());
        txtTen.setText(data.getPromotionName());
        txtPercent.setText(String.valueOf(data.getPercent()));
        dchStart.setDate(data.getStartDate());
        dchEnd.setDate(data.getEndDate());
        cbStatus.setSelectedIndex(data.getStatus());

        List<Integer> selectedIds;
        try {
            selectedIds = promotionApiClient.getSelectedBookIds(data.getPromotionId());
        } catch (ApiException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Không thể tải sách khuyến mãi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        currentlySelectedIds.clear();
        if (selectedIds != null) {
            currentlySelectedIds.addAll(selectedIds);
        }
        for (int i = 0; i < bookModel.getRowCount(); i++) {
            int bookId = (Integer) bookModel.getValueAt(i, 1);
            if (currentlySelectedIds.contains(bookId))
                bookModel.setValueAt(true, i, 0);
        }
    }

    private void saveAction() {
        try {

            String name = txtTen.getText().trim();
            String percentStr = txtPercent.getText().trim();
            if (name.isEmpty() || percentStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ tên và phần trăm!");
                return;
            }

            double percent;
            if (!percentStr.matches("^\\d+(\\.\\d+)?$")) {
                JOptionPane.showMessageDialog(this, "Phần trăm giảm giá phải là số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                txtPercent.requestFocus();
                return;
            }
            percent = Double.parseDouble(percentStr);
            if (!Double.isFinite(percent) || percent <= 0 || percent > 100) {
                JOptionPane.showMessageDialog(this, "Phần trăm giảm giá phải lớn hơn 0 và nhỏ hơn hoặc bằng 100!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                txtPercent.requestFocus();
                return;
            }
            Date startDate = dchStart.getDate();
            Date endDate = dchEnd.getDate();
            if (startDate == null || endDate == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            LocalDate startLocal = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate endLocal = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (startLocal.isAfter(endLocal)) {
                JOptionPane.showMessageDialog(this, "Ngày bắt đầu không được lớn hơn ngày kết thúc!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Timestamp start = Timestamp.valueOf(startLocal.atStartOfDay());
            Timestamp end = Timestamp.valueOf(endLocal.atTime(23, 59, 59));
            int status = cbStatus.getSelectedIndex();

            if (AppConstant.isDateOutOfBounds(start) || AppConstant.isDateOutOfBounds(end)) {
                JOptionPane.showMessageDialog(this, "Khoảng ngày khuyến mãi phải nằm trong khoảng từ 01/01/2026 đến 31/12/2099!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            saveCurrentSelection();
            List<Integer> selectedBookIds = new ArrayList<>(currentlySelectedIds);

            if (selectedBookIds.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một cuốn sách!");
                return;
            }

            PromotionSaveRequestDTO request = new PromotionSaveRequestDTO(
                    name,
                    percent,
                    start,
                    end,
                    status,
                    selectedBookIds
            );
            if (isEdit) {
                promotionApiClient.updatePromotion(data.getPromotionId(), request);
            } else {
                promotionApiClient.createPromotion(request);
            }
            JOptionPane.showMessageDialog(this, "Lưu thành công!");
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Phần trăm phải là số!");
        } catch (ApiException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
        }
    }

    private void saveCurrentSelection() {
        for (int i = 0; i < bookModel.getRowCount(); i++) {
            int id = (Integer) bookModel.getValueAt(i, 1);
            boolean isChecked = (Boolean) bookModel.getValueAt(i, 0);
            if (isChecked && !currentlySelectedIds.contains(id))
                currentlySelectedIds.add(id);
            else if (!isChecked && currentlySelectedIds.contains(id))
                currentlySelectedIds.remove((Integer) id);
        }
    }

    private void restoreSelection() {
        for (int i = 0; i < bookModel.getRowCount(); i++) {
            int id = (Integer) bookModel.getValueAt(i, 1);
            if (currentlySelectedIds.contains(id))
                bookModel.setValueAt(true, i, 0);
        }
    }
}
