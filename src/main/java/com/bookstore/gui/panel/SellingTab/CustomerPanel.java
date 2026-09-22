package com.bookstore.gui.panel.SellingTab;

import com.bookstore.bus.CustomerBUS;
import com.bookstore.bus.MembershipRankBUS;
import com.bookstore.dto.CustomerDTO;
import com.bookstore.dto.MembershipRankDTO;
import com.bookstore.util.AppConstant;
import com.bookstore.util.Refreshable;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class CustomerPanel extends JPanel implements Refreshable {
    private JTextField txtSearch;
    private JComboBox<String> cboRank;
    private JTextField txtMinPoint;
    private JTextField txtMaxPoint;
    private JButton btnResetFilter, btnAddCustomer, btnEditCustomer, btnViewCustomer;

    private JTable tblCustomer;
    private DefaultTableModel customerModel;

    private CustomerBUS customerBUS = new CustomerBUS();
    private MembershipRankBUS rankBUS = new MembershipRankBUS();

    private List<CustomerDTO> listCustomers;

    public CustomerPanel() {
        initUI();
    }

    @Override
    public void refresh() {
        loadCustomerTable();
        loadRanksToComboBox();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 15));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        addEvents();
    }

    private JPanel createTopPanel() {
        JPanel pTop = new JPanel(new BorderLayout(0, 15));
        pTop.setOpaque(false);

        JPanel pSearchRow = new JPanel(new BorderLayout(15, 0));
        pSearchRow.setOpaque(false);

        txtSearch = new JTextField();
        txtSearch.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm kiếm theo tên hoặc số điện thoại...");
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSVGIcon("icon/search_icon.svg").derive(20, 20));

        btnAddCustomer = new JButton("+ Thêm Khách Hàng");
        btnAddCustomer.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnAddCustomer.setForeground(Color.WHITE);
        btnAddCustomer.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        btnAddCustomer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddCustomer.setPreferredSize(new Dimension(200, 40));
        btnAddCustomer.putClientProperty(FlatClientProperties.STYLE, "arc: 5; hoverBackground: #00A364;");

        pSearchRow.add(txtSearch, BorderLayout.CENTER);
        pSearchRow.add(btnAddCustomer, BorderLayout.EAST);

        JPanel pFilterContainer = new JPanel(new GridLayout(2, 1, 0, 10));
        pFilterContainer.setOpaque(false);

        JPanel pFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pFilters.setOpaque(false);

        JPanel pRank = createFilterGroup("Hạng thành viên:");
        cboRank = new JComboBox<>();
        cboRank.addItem("Tất cả hạng");
        cboRank.setPreferredSize(new Dimension(200, 35));
        cboRank.setBackground(Color.WHITE);
        pRank.add(cboRank);

        JPanel pMinPoint = createFilterGroup("Điểm từ:");
        txtMinPoint = new JTextField();
        txtMinPoint.setPreferredSize(new Dimension(100, 35));
        pMinPoint.add(txtMinPoint);

        JPanel pMaxPoint = createFilterGroup("Đến:");
        txtMaxPoint = new JTextField();
        txtMaxPoint.setPreferredSize(new Dimension(100, 35));
        pMaxPoint.add(txtMaxPoint);

        pFilters.add(pRank);
        pFilters.add(pMinPoint);
        pFilters.add(pMaxPoint);

        JPanel pActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pActions.setOpaque(false);

        FlatSVGIcon.ColorFilter whiteFilter = new FlatSVGIcon.ColorFilter(color -> Color.WHITE);

        FlatSVGIcon resetIcon = new FlatSVGIcon("icon/reset_icon.svg").derive(16, 16);
        resetIcon.setColorFilter(whiteFilter);
        btnResetFilter = new JButton("Xóa bộ lọc", resetIcon);
        btnResetFilter.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnResetFilter.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnResetFilter.setBackground(Color.decode("#ff3131"));
        btnResetFilter.setForeground(Color.WHITE);
        btnResetFilter.setPreferredSize(new Dimension(150, 35));
        btnResetFilter.putClientProperty(FlatClientProperties.STYLE, "arc: 5; hoverBackground: #ff5757;");

        FlatSVGIcon editIcon = new FlatSVGIcon("icon/edit_icon.svg").derive(16, 16);
        editIcon.setColorFilter(whiteFilter);
        btnEditCustomer = new JButton("Sửa", editIcon);
        btnEditCustomer.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnEditCustomer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEditCustomer.setPreferredSize(new Dimension(100, 35));
        btnEditCustomer.setBackground(Color.decode("#ffcb4b"));
        btnEditCustomer.setForeground(Color.WHITE);
        btnEditCustomer.putClientProperty(FlatClientProperties.STYLE, "arc: 5; hoverBackground: #ffdd53;");

        FlatSVGIcon viewIcon = new FlatSVGIcon("icon/info_icon.svg").derive(16, 16);
        viewIcon.setColorFilter(whiteFilter);
        btnViewCustomer = new JButton("Xem chi tiết", viewIcon);
        btnViewCustomer.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnViewCustomer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnViewCustomer.setPreferredSize(new Dimension(140, 35));
        btnViewCustomer.setBackground(Color.decode("#5674ff"));
        btnViewCustomer.setForeground(Color.WHITE);
        btnViewCustomer.putClientProperty(FlatClientProperties.STYLE, "arc: 5; hoverBackground: #64B5F6;");

        pActions.add(btnResetFilter);
        pActions.add(btnEditCustomer);
        pActions.add(btnViewCustomer);

        pFilterContainer.add(pFilters);
        pFilterContainer.add(pActions);

        pTop.add(pSearchRow, BorderLayout.NORTH);
        pTop.add(pFilterContainer, BorderLayout.CENTER);

        return pTop;
    }

    private JPanel createFilterGroup(String title) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 13));
        label.setForeground(Color.DARK_GRAY);
        panel.add(label, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        String[] headers = {"ID", "Tên khách hàng", "Số điện thoại", "Điểm thưởng", "Hạng thành viên"};
        customerModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblCustomer = new JTable(customerModel);
        styleTable(tblCustomer);

        tblCustomer.getColumnModel().getColumn(0).setMinWidth(0);
        tblCustomer.getColumnModel().getColumn(0).setMaxWidth(0);
        tblCustomer.getColumnModel().getColumn(0).setPreferredWidth(0);

        JScrollPane scrollPane = new JScrollPane(tblCustomer);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.decode("#E0E0E0")));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void styleTable(JTable table) {
        JTableHeader header = table.getTableHeader();
        header.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        header.setForeground(Color.WHITE);
        header.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setReorderingAllowed(false);

        table.setRowHeight(35);
        table.setShowGrid(true);
        table.setSelectionBackground(Color.decode("#d4ffee"));
        table.setSelectionForeground(Color.BLACK);
        table.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void loadCustomerTable() {
        listCustomers = customerBUS.selectAllCustomers();
        updateCustomerTable(listCustomers);
    }

    private void updateCustomerTable(List<CustomerDTO> list) {
        customerModel.setRowCount(0);
        for (CustomerDTO customer : list) {
            customerModel.addRow(new Object[]{
                    customer.getCustomerId(),
                    customer.getCustomerName(),
                    customer.getCustomerPhone(),
                    customer.getPoint(),
                    customer.getRankName()
            });
        }
    }

    private void filterCustomers() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        String selectedRank = cboRank.getSelectedItem() != null ? cboRank.getSelectedItem().toString() : "Tất cả hạng";

        int minPoint = 0;
        int maxPoint = Integer.MAX_VALUE;

        try {
            if (!txtMinPoint.getText().trim().isEmpty()) {
                minPoint = Integer.parseInt(txtMinPoint.getText().trim());
            }
            if (!txtMaxPoint.getText().trim().isEmpty()) {
                maxPoint = Integer.parseInt(txtMaxPoint.getText().trim());
            }
        } catch (NumberFormatException e) {

        }

        List<CustomerDTO> filteredList = new ArrayList<>();

        for (CustomerDTO customer : listCustomers) {
            String name = customer.getCustomerName().toLowerCase();
            String phone = customer.getCustomerPhone().toLowerCase();

            boolean matchKeyword = keyword.isEmpty() || name.contains(keyword) || phone.contains(keyword);
            boolean matchRank = selectedRank.equals("Tất cả hạng") ||
                    (customer.getRankName() != null && customer.getRankName().equals(selectedRank));
            boolean matchPoint = customer.getPoint() >= minPoint && customer.getPoint() <= maxPoint;

            if (matchKeyword && matchRank && matchPoint) {
                filteredList.add(customer);
            }
        }

        updateCustomerTable(filteredList);
    }

    private void loadRanksToComboBox () {
        for (ActionListener al : cboRank.getActionListeners()) {
            cboRank.removeActionListener(al);
        }

        cboRank.removeAllItems();
        cboRank.addItem("Tất cả hạng");
        List<MembershipRankDTO> allRanks = rankBUS.getAllRanks();
        for (MembershipRankDTO rank : allRanks){
            cboRank.addItem(rank.getRankName());
        }

        cboRank.addActionListener(e -> checkAndFilterRank());
    }


    private void checkAndFilterRank () {
        String selectedRank = cboRank.getSelectedItem() != null ? cboRank.getSelectedItem().toString() : "Tất cả hạng";

        if (!selectedRank.equals("Tất cả hạng")) {
            boolean hasCustomer = false;
            for (CustomerDTO customer : listCustomers) {
                if (customer.getRankName() != null && customer.getRankName().equals(selectedRank)) {
                    hasCustomer = true;
                    break;
                }
            }

            if (!hasCustomer) {
                JOptionPane.showMessageDialog(this,
                        "Hiện tại chưa có khách hàng nào đạt được hạng: " + selectedRank,
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);

                cboRank.setSelectedIndex(0);
            }
        }

        filterCustomers();
    }

    private void resetFilter() {
        txtSearch.setText("");
        cboRank.setSelectedIndex(0);
        txtMinPoint.setText("");
        txtMaxPoint.setText("");
    }

    private void openCustomerEditDialog() {
        int selectedRow = tblCustomer.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần sửa!");
            return;
        }

        int customerId = (int) tblCustomer.getValueAt(selectedRow, 0);

        CustomerDTO selectedCustomer = null;
        for (CustomerDTO c : listCustomers) {
            if (c.getCustomerId() == customerId) {
                selectedCustomer = c;
                break;
            }
        }

        if (selectedCustomer != null) {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            CustomerEditDialog dialog = new CustomerEditDialog(parentFrame, this, selectedCustomer);
            dialog.setVisible(true);
        }
    }

    private void openCustomerDetailDialog() {
        int selectedRow = tblCustomer.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần xem chi tiết!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int customerId = (int) tblCustomer.getValueAt(selectedRow, 0);

        CustomerDTO selectedCustomer = null;
        for (CustomerDTO c : listCustomers) {
            if (c.getCustomerId() == customerId) {
                selectedCustomer = c;
                break;
            }
        }

        if (selectedCustomer != null) {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            CustomerDetailDialog dialog = new CustomerDetailDialog(parentFrame, selectedCustomer);
            dialog.setVisible(true);
        }
    }

    private void openCustomerInsertDialog() {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        CustomerEditDialog dialog = new CustomerEditDialog(parentFrame, this, null);
        dialog.setVisible(true);
    }

    private void addEvents() {
        DocumentListener docListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterCustomers(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterCustomers(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterCustomers(); }
        };

        txtSearch.getDocument().addDocumentListener(docListener);
        txtMinPoint.getDocument().addDocumentListener(docListener);
        txtMaxPoint.getDocument().addDocumentListener(docListener);

        btnResetFilter.addActionListener(e -> resetFilter());
        btnEditCustomer.addActionListener(e -> openCustomerEditDialog());
        btnViewCustomer.addActionListener(e -> openCustomerDetailDialog());
        btnAddCustomer.addActionListener(e -> openCustomerInsertDialog());
    }
}
