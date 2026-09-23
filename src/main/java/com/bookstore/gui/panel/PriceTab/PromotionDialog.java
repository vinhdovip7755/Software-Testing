package com.bookstore.gui.panel.PriceTab;

import com.bookstore.bus.*;
import com.bookstore.dao.*;
import com.bookstore.dto.*;
import javax.swing.*;
import com.toedter.calendar.JDateChooser;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import java.awt.*;
import java.awt.event.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PromotionDialog extends JDialog {
    private JComboBox<String> cbCategory;
    private CategoryBUS categoryBUS = new CategoryBUS();
    private JTextField txtID, txtTen, txtPercent, txtSearchBook;
    private JDateChooser dchStart, dchEnd;
    private JTable bookTable;
    private DefaultTableModel bookModel;
    private PromotionBUS bus = new PromotionBUS();
    private BookDAO bookDAO = new BookDAO();
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
                saveCurrentSelection();
                String keyword = txtSearchBook.getText().trim();

                cbCategory.setSelectedIndex(0);

                loadBookList(bus.suggestBooksByName(keyword));
                restoreSelection();
            }
        });

        cbCategory.addActionListener(e -> {
            saveCurrentSelection();
            String categoryName = (String) cbCategory.getSelectedItem();

            if (cbCategory.getSelectedIndex() == 0) {

                loadBookList(bus.suggestBooksByPromotionName(""));
            } else {

                loadBookList(bookDAO.getByCategoryName(categoryName));
            }

            restoreSelection();
        });

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
        dchStart = new JDateChooser(new java.util.Date());
        dchStart.setDateFormatString("yyyy-MM-dd");
        dchEnd = new JDateChooser(new java.util.Date());
        dchEnd.setDateFormatString("yyyy-MM-dd");
        cbStatus = new JComboBox<>(new String[] { "Ngừng hoạt động", "Đang chạy" });
        cbCategory = new JComboBox<>();
        cbCategory.addItem("Tất cả thể loại");
        for (CategoryDTO cat : categoryBUS.selectAllCategories())
            cbCategory.addItem(cat.getCategoryName());

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

        loadBookList(bus.suggestBooksByPromotionName(""));

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

        List<Integer> selectedIds = bus.getSelectedBookIds(data.getPromotionId());
        for (int i = 0; i < bookModel.getRowCount(); i++) {
            int bookId = (Integer) bookModel.getValueAt(i, 1);
            if (selectedIds.contains(bookId))
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

            double percent = Double.parseDouble(percentStr);
            java.util.Date startDate = dchStart.getDate();
            java.util.Date endDate = dchEnd.getDate();
            if (startDate == null || endDate == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày hợp lệ!");
                return;
            }
            java.sql.Timestamp start = new java.sql.Timestamp(startDate.getTime());
            java.sql.Timestamp end = new java.sql.Timestamp(endDate.getTime());
            int status = cbStatus.getSelectedIndex();

            if (start.after(end)) {
                JOptionPane.showMessageDialog(this, "Ngày bắt đầu phải trướcầngày kết thúc!");
                return;
            }

            if (data != null) {
                data.setPromotionName(name);
                data.setPercent(percent);
                data.setStartDate(start);
                data.setEndDate(end);
                data.setStatus(status);
            }

            saveCurrentSelection();
            List<Integer> selectedBookIds = new ArrayList<>(currentlySelectedIds);

            if (selectedBookIds.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một cuốn sách!");
                return;
            }

            if (bus.savePromotion(isEdit, data, name, percent, start, end, status, selectedBookIds)) {
                JOptionPane.showMessageDialog(this, "Lưu thành công!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lưu thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Phần trăm phải là số!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
            ex.printStackTrace();
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
