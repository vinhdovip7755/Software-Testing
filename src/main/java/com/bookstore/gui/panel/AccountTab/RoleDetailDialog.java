package com.bookstore.gui.panel.AccountTab;

import com.bookstore.api.client.ApiException;
import com.bookstore.api.client.RoleApiClient;
import com.bookstore.api.contract.RoleActionPermissionDTO;
import com.bookstore.dto.RoleDTO;
import com.bookstore.util.AppConstant;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class RoleDetailDialog extends JDialog {
    private RoleDTO role;
    private final RoleApiClient roleApiClient = new RoleApiClient();
    private DefaultTableModel tableModel;
    private boolean loadedSuccessfully = false;

    public RoleDetailDialog(Frame owner, RoleDTO role) {
        super(owner, "Chi tiết phân quyền", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.role = role;
        initUI();
        loadPermissionData();
    }

    private void initUI() {
        setSize(600, 600);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(0, 10));

        JPanel pInfo = new JPanel(new GridLayout(2, 2, 10, 10));
        pInfo.setBorder(new EmptyBorder(15, 15, 5, 15));

        JTextField txtId = new JTextField(String.valueOf(role.getRoleId()));
        txtId.setEditable(false);
        JTextField txtName = new JTextField(role.getRoleName());
        txtName.setEditable(false);

        pInfo.add(new JLabel("Mã chức vụ:"));
        pInfo.add(txtId);
        pInfo.add(new JLabel("Tên chức vụ:"));
        pInfo.add(txtName);

        add(pInfo, BorderLayout.NORTH);

        String[] headers = {"Tên chức năng", "Được phép Xem", "Được phép Thao tác"};

        tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1 || columnIndex == 2) {
                    return Boolean.class;
                }
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(35);
        table.setShowGrid(true);
        table.setSelectionBackground(Color.decode("#d4ffee"));
        table.setSelectionForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 13));
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(0, 15, 0, 15));
        add(scrollPane, BorderLayout.CENTER);

        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dispose());
        JPanel pBottom = new JPanel();
        pBottom.add(btnClose);
        add(pBottom, BorderLayout.SOUTH);
    }

    private void loadPermissionData() {
        try {
            List<RoleActionPermissionDTO> permissions = roleApiClient.getPermissions(role.getRoleId());
            tableModel.setRowCount(0);
            for (RoleActionPermissionDTO permission : permissions) {
                tableModel.addRow(new Object[]{
                        permission.getActionName(),
                        permission.isCanView(),
                        permission.isCanAction()
                });
            }
            loadedSuccessfully = true;
        } catch (ApiException e) {
            loadedSuccessfully = false;
            JOptionPane.showMessageDialog(getOwner(), e.getMessage(), "Lỗi API", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isLoadedSuccessfully() {
        return loadedSuccessfully;
    }
}
