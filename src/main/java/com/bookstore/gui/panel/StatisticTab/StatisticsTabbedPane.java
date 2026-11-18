package com.bookstore.gui.panel.StatisticTab;

import com.bookstore.util.LazyTabbedPaneHelper;
import com.bookstore.util.PermissionUtil;
import com.bookstore.util.Refreshable;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

public class StatisticsTabbedPane extends JPanel implements Refreshable {
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final LazyTabbedPaneHelper lazyTabs = new LazyTabbedPaneHelper(tabbedPane);

    public StatisticsTabbedPane() {
        initUI();
    }

    @Override
    public void refresh() {
        lazyTabs.refreshSelectedTab();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_CENTER);

        if (PermissionUtil.hasViewPermission("STATISTICS")) {
            lazyTabs.addTab("Doanh Thu", FinancialStatsPanel::new);
            lazyTabs.addTab("Sản Phẩm", ProductStatsPanel::new);
            lazyTabs.addTab("Khách Hàng", CustomerStatsPanel::new);
        }

        lazyTabs.initialize();
        add(tabbedPane, BorderLayout.CENTER);
    }
}