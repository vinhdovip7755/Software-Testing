package com.bookstore.gui.panel.SellingTab;

import com.bookstore.util.PermissionUtil;
import com.bookstore.util.Refreshable;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;

public class SellingTabbedPane extends JPanel implements Refreshable{
    private JTabbedPane tabbedPane = new JTabbedPane();
    private SellingPanel sellingPanel;
    private CustomerPanel customerPanel;
    private MembershipRankPanel membershipRankPanel;

    public SellingTabbedPane() {
        initUI();
    }

    @Override
    public void refresh() {
        Component selectedTab = tabbedPane.getSelectedComponent();
        if (selectedTab instanceof Refreshable r) {
            r.refresh();
        }
    }

    private void initUI() {
        setLayout(new BorderLayout());
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_CENTER);

        if (PermissionUtil.hasViewPermission("MANAGE_SELLING")) {
            sellingPanel = new SellingPanel();
            tabbedPane.addTab("Bán Hàng", sellingPanel);
        }

        if (PermissionUtil.hasViewPermission("MANAGE_CUSTOMER")) {
            customerPanel = new CustomerPanel();
            membershipRankPanel = new MembershipRankPanel();
            tabbedPane.addTab("Khách Hàng", customerPanel);
            tabbedPane.addTab("Hạng Thành Viên", membershipRankPanel);
        }

        add(tabbedPane, BorderLayout.CENTER);

        tabbedPane.addChangeListener(e -> {
            Component selectedTab = tabbedPane.getSelectedComponent();
            if (selectedTab instanceof Refreshable r) {
                r.refresh();
            }
        });
    }
}