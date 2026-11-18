package com.bookstore.gui.panel.SellingTab;

import com.bookstore.util.LazyTabbedPaneHelper;
import com.bookstore.util.PermissionUtil;
import com.bookstore.util.Refreshable;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

public class SellingTabbedPane extends JPanel implements Refreshable {
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final LazyTabbedPaneHelper lazyTabs = new LazyTabbedPaneHelper(tabbedPane);

    public SellingTabbedPane() {
        initUI();
    }

    @Override
    public void refresh() {
        lazyTabs.refreshSelectedTab();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_CENTER);

        if (PermissionUtil.hasViewPermission("MANAGE_SELLING")) {
            lazyTabs.addTab("Bán Hàng", SellingPanel::new);
        }

        if (PermissionUtil.hasViewPermission("MANAGE_CUSTOMER")) {
            lazyTabs.addTab("Khách Hàng", CustomerPanel::new);
            lazyTabs.addTab("Hạng Thành Viên", MembershipRankPanel::new);
        }

        lazyTabs.initialize();
        add(tabbedPane, BorderLayout.CENTER);
    }
}