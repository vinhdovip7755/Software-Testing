package com.bookstore.gui.panel.SellingTab;

import com.bookstore.bus.MembershipRankBUS;
import com.bookstore.dto.MembershipRankDTO;
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

public class MembershipRankPanel extends JPanel implements Refreshable {
    private JButton btnAddRank, btnEditRank, btnDeleteRank;

    private JTable tblRank;
    private DefaultTableModel rankModel;

    private MembershipRankBUS rankBUS = new MembershipRankBUS();
    private List<MembershipRankDTO> listRanks;

    public MembershipRankPanel() {
        initUI();
    }

    @Override
    public void refresh() {
        loadRankTable();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 15));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadRankTable();
        addEvents();
    }

    private JPanel createTopPanel() {
        JPanel pTop = new JPanel(new BorderLayout(0, 15));
        pTop.setOpaque(false);

        JPanel pActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0));
        pActions.setOpaque(false);

        btnAddRank = new JButton("+ Thêm Hạng");
        btnAddRank.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnAddRank.setForeground(Color.WHITE);
        btnAddRank.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));
        btnAddRank.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddRank.setPreferredSize(new Dimension(180, 40));
        btnAddRank.putClientProperty(FlatClientProperties.STYLE, "hoverBackground: #00A364;");

        FlatSVGIcon.ColorFilter whiteFilter = new FlatSVGIcon.ColorFilter(color -> Color.WHITE);
        FlatSVGIcon editIcon = new FlatSVGIcon("icon/edit_icon.svg").derive(16, 16);
        editIcon.setColorFilter(whiteFilter);
        btnEditRank = new JButton("Sửa", editIcon);
        btnEditRank.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnEditRank.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEditRank.setPreferredSize(new Dimension(180, 40));
        btnEditRank.setBackground(Color.decode("#ffcb4b"));
        btnEditRank.setForeground(Color.WHITE);
        btnEditRank.putClientProperty(FlatClientProperties.STYLE, "hoverBackground: #ffdd53;");

        FlatSVGIcon deleteIcon = new FlatSVGIcon("icon/delete_icon.svg").derive(16, 16);
        deleteIcon.setColorFilter(whiteFilter);
        btnDeleteRank = new JButton("Xóa", deleteIcon);
        btnDeleteRank.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        btnDeleteRank.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDeleteRank.setPreferredSize(new Dimension(180, 40));
        btnDeleteRank.setBackground(Color.decode("#ff3131"));
        btnDeleteRank.setForeground(Color.WHITE);
        btnDeleteRank.putClientProperty(FlatClientProperties.STYLE, "arc: 5; hoverBackground: #ff5757;");

        pActions.add(btnAddRank);
        pActions.add(btnEditRank);
        pActions.add(btnDeleteRank);

        pTop.add(pActions, BorderLayout.CENTER);

        return pTop;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        String[] headers = {"ID", "Tên hạng thành viên", "Điểm tối thiểu", "Phần trăm giảm giá (%)"};
        rankModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblRank = new JTable(rankModel);
        styleTable(tblRank);

        tblRank.getColumnModel().getColumn(0).setMinWidth(0);
        tblRank.getColumnModel().getColumn(0).setMaxWidth(0);
        tblRank.getColumnModel().getColumn(0).setPreferredWidth(0);

        JScrollPane scrollPane = new JScrollPane(tblRank);
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
        table.setGridColor(Color.decode("#E0E0E0"));
        table.setSelectionBackground(Color.decode("#d4ffee"));
        table.setSelectionForeground(Color.BLACK);
        table.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    private void loadRankTable() {
        listRanks = rankBUS.getAllRanks();
        rankModel.setRowCount(0);
        if (listRanks != null) {
            for (MembershipRankDTO rank : listRanks) {
                rankModel.addRow(new Object[]{
                        rank.getRankId(),
                        rank.getRankName(),
                        rank.getMinPoint(),
                        rank.getDiscountPercent() + "%"
                });
            }
        }
    }

    private void openRankEditDialog() {
        int selectedRow = tblRank.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hạng thành viên cần sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int rankId = (int) tblRank.getValueAt(selectedRow, 0);

        MembershipRankDTO selectedRank = null;
        if (listRanks != null) {
            for (MembershipRankDTO rank : listRanks) {
                if (rank.getRankId() == rankId) {
                    selectedRank = rank;
                    break;
                }
            }
        }

        if (selectedRank != null) {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            MembershipRankDialog dialog = new MembershipRankDialog(parentFrame, selectedRank);
            dialog.setVisible(true);
            loadRankTable();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi: Không tìm thấy dữ liệu hạng thành viên này trong bộ nhớ!", "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void openRankInsertDialog() {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        MembershipRankDialog dialog = new MembershipRankDialog(parentFrame, null);
        dialog.setVisible(true);
        loadRankTable();
    }

    private void deleteSelectedRank() {
        int selectedRow = tblRank.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hạng thành viên cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int rankId = (int) tblRank.getValueAt(selectedRow, 0);
        String rankName = (String) tblRank.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa hạng '" + rankName + "' vĩnh viễn không?",
                "Xácầnhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            String message = rankBUS.deleteRank(rankId);

            if (message.equals("Xóa hạng thành viên thành công!")) {
                JOptionPane.showMessageDialog(this, message);
                loadRankTable();
            } else {
                JOptionPane.showMessageDialog(this, message, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void addEvents() {
        btnAddRank.addActionListener(e -> openRankInsertDialog());
        btnEditRank.addActionListener(e -> openRankEditDialog());
        btnDeleteRank.addActionListener(e -> deleteSelectedRank());
    }
}
