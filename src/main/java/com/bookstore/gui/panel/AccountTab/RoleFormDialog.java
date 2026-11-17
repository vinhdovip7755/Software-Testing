package com.bookstore.gui.panel.AccountTab;

import com.bookstore.api.client.ApiException;
import com.bookstore.api.client.RoleApiClient;
import com.bookstore.api.contract.RoleActionPermissionDTO;
import com.bookstore.api.contract.RoleSaveRequestDTO;
import com.bookstore.dto.RoleDTO;
import com.bookstore.util.AppConstant;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class RoleFormDialog extends JDialog {
    private RoleDTO role;
    private JTextField txtName;
    private DefaultTableModel tableModel;
    private JTable table;
    private List<RoleActionPermissionDTO> permissionRows;
    private final RoleApiClient roleApiClient = new RoleApiClient();

    private boolean isSaved = false;
    private boolean isAddMode;
    private boolean permissionsLoaded;

    public RoleFormDialog(Frame owner, RoleDTO role) {
        super(owner, role.getRoleId() == 0 ? "Thêm mới Chức vụ" : "Chỉnh sửa Chức vụ", true);
        this.role = role;
        this.isAddMode = (role.getRoleId() == 0);

        initUI();
        loadData();
    }

    private void initUI() {
        setSize(650, 550);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(0, 10));

        JPanel pInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        pInfo.add(new JLabel("<html>Tên chức vụ <font color='red'>*</font>: </html>"));
        txtName = new JTextField(role.getRoleName(), 30);
        txtName.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));
        pInfo.add(txtName);
        add(pInfo, BorderLayout.NORTH);

        String[] headers = {"Tên chức năng", "Quyền Xem", "Quyền Thao tác"};
        tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1 || columnIndex == 2) return Boolean.class;
                return String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1 || column == 2;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(35);
        table.setShowGrid(true);
        table.setSelectionBackground(Color.decode("#d4ffee"));
        table.setSelectionForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        table.getTableHeader().setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();

                if (row >= 0 && (col == 1 || col == 2)) {
                    boolean isView = (boolean) tableModel.getValueAt(row, 1);
                    boolean isAction = (boolean) tableModel.getValueAt(row, 2);

                    if (col == 2 && isAction && !isView) {
                        tableModel.setValueAt(true, row, 1);
                    }

                    else if (col == 1 && !isView && isAction) {
                        tableModel.setValueAt(false, row, 2);
                    }
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel pBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        JButton btnSave = new JButton("Lưu thay đổi");
        btnSave.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnSave.addActionListener(e -> saveRole());

        JButton btnCancel = new JButton("Hủy");
        btnCancel.addActionListener(e -> dispose());

        pBottom.add(btnCancel);
        pBottom.add(btnSave);
        add(pBottom, BorderLayout.SOUTH);
    }

    private void loadData() {
        try {
            permissionRows = roleApiClient.getPermissions(role.getRoleId());
            permissionsLoaded = true;
            tableModel.setRowCount(0);
            for (RoleActionPermissionDTO permission : permissionRows) {
                tableModel.addRow(new Object[]{
                        permission.getActionName(),
                        permission.isCanView(),
                        permission.isCanAction()
                });
            }
        } catch (ApiException e) {
            permissionRows = List.of();
            permissionsLoaded = false;
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi API", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveRole() {
        if (!permissionsLoaded) {
            JOptionPane.showMessageDialog(this, "Không thể tải danh sách quyền từ API.", "Lỗi API", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }


        String newName = txtName.getText().trim();
        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên chức vụ không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtName.requestFocus();
            return;
        }

        if (!newName.matches("^(?=.*\\p{L})[\\p{L}0-9\\s.'\\u2019\\u2018\\u0060\\u02BB\\u2013-]+$")) {
            JOptionPane.showMessageDialog(this, "Tên chức vụ không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtName.requestFocus();
            return;
        }

        List<RoleActionPermissionDTO> permissions = new java.util.ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            boolean isView = (boolean) tableModel.getValueAt(i, 1);
            boolean isAction = (boolean) tableModel.getValueAt(i, 2);
            RoleActionPermissionDTO original = permissionRows.get(i);
            permissions.add(new RoleActionPermissionDTO(
                    original.getActionId(),
                    original.getActionCode(),
                    original.getActionName(),
                    isView,
                    isAction
            ));
        }

        RoleSaveRequestDTO request = new RoleSaveRequestDTO(newName, permissions);
        try {
            String message = isAddMode
                    ? roleApiClient.createRole(request)
                    : roleApiClient.updateRole(role.getRoleId(), request);
            JOptionPane.showMessageDialog(this, message);
            isSaved = true;
            dispose();
        } catch (ApiException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi API", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return isSaved; }
}
