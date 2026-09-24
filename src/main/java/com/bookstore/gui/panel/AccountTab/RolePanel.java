package com.bookstore.gui.panel.AccountTab;

import com.bookstore.bus.RoleBUS;
import com.bookstore.dto.RoleDTO;
import com.bookstore.util.AppConstant;
import com.bookstore.util.Refreshable;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class RolePanel extends JPanel implements Refreshable {
    private JButton btnAddRole;
    private JButton btnEditRole;
    private JButton btnViewRole;

    private JTable tblRole;
    private DefaultTableModel roleModel;

    private RoleBUS roleBUS = new RoleBUS();

    public RolePanel() {
        initUI();
    }

    @Override
    public void refresh() {
        loadRoleTable();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 15));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        refresh();
        addEvents();
    }

    private JPanel createTopPanel() {
        JPanel pTop = new JPanel(new BorderLayout());
        pTop.setOpaque(false);

        JLabel lbTitle = new JLabel("QUẢN LÝ CHỨC VỤ & PHÂN QUYỀN");
        lbTitle.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 20));
        lbTitle.setForeground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        pTop.add(lbTitle, BorderLayout.WEST);

        JPanel pButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pButtons.setOpaque(false);

        FlatSVGIcon.ColorFilter whiteFilter = new FlatSVGIcon.ColorFilter(color -> Color.WHITE);

        btnAddRole = new JButton("+ Thêm Chức Vụ");
        btnAddRole.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnAddRole.setForeground(Color.WHITE);
        btnAddRole.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        btnAddRole.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddRole.setPreferredSize(new Dimension(160, 38));
        btnAddRole.putClientProperty(FlatClientProperties.STYLE, "arc: 5; hoverBackground: #00A364;");

        FlatSVGIcon editIcon = new FlatSVGIcon("icon/edit_icon.svg").derive(16, 16);
        editIcon.setColorFilter(whiteFilter);
        btnEditRole = new JButton("Sửa", editIcon);
        btnEditRole.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnEditRole.setForeground(Color.WHITE);
        btnEditRole.setBackground(Color.decode("#ffcb4b"));
        btnEditRole.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEditRole.setPreferredSize(new Dimension(100, 38));
        btnEditRole.putClientProperty(FlatClientProperties.STYLE, "arc: 5; hoverBackground: #ffdd53;");

        FlatSVGIcon viewIcon = new FlatSVGIcon("icon/info_icon.svg").derive(16, 16);
        viewIcon.setColorFilter(whiteFilter);
        btnViewRole = new JButton("Xem chi tiết", viewIcon);
        btnViewRole.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnViewRole.setForeground(Color.WHITE);
        btnViewRole.setBackground(Color.decode("#5674ff"));
        btnViewRole.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnViewRole.setPreferredSize(new Dimension(140, 38));
        btnViewRole.putClientProperty(FlatClientProperties.STYLE, "arc: 5; hoverBackground: #64B5F6;");

        pButtons.add(btnAddRole);
        pButtons.add(btnEditRole);
        pButtons.add(btnViewRole);

        pTop.add(pButtons, BorderLayout.EAST);

        return pTop;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        String[] headers = {"Mã Chức Vụ", "Tên Chức Vụ"};
        roleModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblRole = new JTable(roleModel);
        styleTable(tblRole);

        tblRole.getColumnModel().getColumn(0).setPreferredWidth(150);
        tblRole.getColumnModel().getColumn(0).setMaxWidth(200);

        JScrollPane scrollPane = new JScrollPane(tblRole);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.decode("#E0E0E0")));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void styleTable(JTable table) {
        JTableHeader header = table.getTableHeader();
        header.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        header.setForeground(Color.WHITE);
        header.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 15));
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setReorderingAllowed(false);

        table.setRowHeight(40);
        table.setShowGrid(true);
        table.setGridColor(Color.decode("#E0E0E0"));
        table.setSelectionBackground(Color.decode("#d4ffee"));
        table.setSelectionForeground(Color.BLACK);
        table.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 15));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void loadRoleTable() {
        roleModel.setRowCount(0);
        List<RoleDTO> listRoles = roleBUS.getAllRoles();
        for (RoleDTO role : listRoles) {
            roleModel.addRow(new Object[]{
                    role.getRoleId(),
                    role.getRoleName()
            });
        }
    }

    private void addEvents() {
        btnAddRole.addActionListener(e -> {
            RoleDTO newRole = new RoleDTO(0, "");

            RoleFormDialog dialog = new RoleFormDialog((JFrame) SwingUtilities.getWindowAncestor(this), newRole);
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                refresh();
            }
        });

        btnEditRole.addActionListener(e -> {
            int selectedRow = tblRole.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn chức vụ cần sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int roleId = (int) tblRole.getValueAt(selectedRow, 0);
            String roleName = (String) tblRole.getValueAt(selectedRow, 1);
            RoleDTO selectedRole = new RoleDTO(roleId, roleName);

            RoleFormDialog dialog = new RoleFormDialog((JFrame) SwingUtilities.getWindowAncestor(this), selectedRole);
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                refresh();
            }
        });

        btnViewRole.addActionListener(e -> {
            int selectedRow = tblRole.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn chức vụ cần xem!");
                return;
            }

            int roleId = (int) tblRole.getValueAt(selectedRow, 0);
            String roleName = (String) tblRole.getValueAt(selectedRow, 1);
            RoleDTO selectedRole = new RoleDTO(roleId, roleName);

            RoleDetailDialog dialog = new RoleDetailDialog((JFrame) SwingUtilities.getWindowAncestor(this), selectedRole);
            dialog.setVisible(true);
        });
    }
}