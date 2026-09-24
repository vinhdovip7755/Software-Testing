package com.bookstore.gui.panel.AccountTab;

import com.bookstore.bus.*;
import com.bookstore.dto.*;
import com.bookstore.util.AppConstant;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoleFormDialog extends JDialog {
    private RoleDTO role;
    private JTextField txtName;
    private DefaultTableModel tableModel;
    private JTable table;
    private List<ActionDTO> listActions;

    private RoleBUS roleBUS = new RoleBUS();
    private ActionBUS actionBUS = new ActionBUS();
    private PermissionBUS permissionBUS = new PermissionBUS();

    private boolean isSaved = false;
    private boolean isAddMode;

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

        String[] headers = {"Tên chứcầnăng", "Quyền Xem", "Quyền Thao tác"};
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
        listActions = actionBUS.selectAllActions();
        Map<String, PermissionDTO> currentPerms = permissionBUS.getPermissionsByRoleId(role.getRoleId());

        tableModel.setRowCount(0);
        for (ActionDTO action : listActions) {
            PermissionDTO perm = currentPerms.get(action.getActionCode());
            boolean canView = (perm != null && perm.isView());
            boolean canAction = (perm != null && perm.isAction());

            tableModel.addRow(new Object[]{ action.getActionName(), canView, canAction });
        }
    }

    private void saveRole() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }


        String newName = txtName.getText().trim();
        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên chức vụ không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtName.requestFocus();
            return;
        }

        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên chức vụ không được để trống!");
            return;
        }

        role.setRoleName(newName);
        int targetRoleId = role.getRoleId();

        if (isAddMode) {
            targetRoleId = roleBUS.insertRole(role);

            if (targetRoleId == 0) {
                JOptionPane.showMessageDialog(this, "Thêm thất bại! Tên chức vụ có thể đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            boolean updateSuccess = roleBUS.updateRole(role);

            if (!updateSuccess) {
                JOptionPane.showMessageDialog(this, "Cập nhật tên chức vụ thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        List<PermissionDTO> newPerms = new ArrayList<>();
        List<Integer> actionIds = new ArrayList<>();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            boolean isView = (boolean) tableModel.getValueAt(i, 1);
            boolean isAction = (boolean) tableModel.getValueAt(i, 2);

            ActionDTO action = listActions.get(i);

            newPerms.add(new PermissionDTO(action.getActionCode(), isView, isAction));
            actionIds.add(action.getActionId());
        }

        boolean success = permissionBUS.saveRolePermissions(targetRoleId, newPerms, actionIds);

        if (success) {
            String msg = isAddMode ? "Thêm chức vụ mới thành công!" : "Cập nhật phân quyền thành công!";
            JOptionPane.showMessageDialog(this, msg);
            isSaved = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống khi lưu chi tiết quyền!", "Lỗi Fatal", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return isSaved; }
}
