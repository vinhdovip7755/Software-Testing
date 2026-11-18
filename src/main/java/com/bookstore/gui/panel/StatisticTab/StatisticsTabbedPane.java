package com.bookstore.gui.panel.StatisticTab;

import com.bookstore.util.PermissionUtil;
import com.bookstore.util.Refreshable;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class StatisticsTabbedPane extends JPanel implements Refreshable {
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final Map<Component, Supplier<JComponent>> pendingTabs = new IdentityHashMap<>();

    public StatisticsTabbedPane() {
        initUI();
    }

    @Override
    public void refresh() {
        activateSelectedTab(true);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_CENTER);

        if (PermissionUtil.hasViewPermission("STATISTICS")) {
            addLazyTab("Doanh Thu", FinancialStatsPanel::new);
            addLazyTab("Sản Phẩm", ProductStatsPanel::new);
            addLazyTab("Khách Hàng", CustomerStatsPanel::new);
        }

        activateSelectedTab(false);
        add(tabbedPane, BorderLayout.CENTER);
        tabbedPane.addChangeListener(e -> activateSelectedTab(true));
    }

    private void addLazyTab(String title, Supplier<JComponent> factory) {
        JPanel placeholder = new JPanel();
        pendingTabs.put(placeholder, factory);
        tabbedPane.addTab(title, placeholder);
    }

    private void activateSelectedTab(boolean refreshIfLoaded) {
        int index = tabbedPane.getSelectedIndex();
        if (index < 0) {
            return;
        }
        Component selectedTab = tabbedPane.getComponentAt(index);
        Supplier<JComponent> factory = pendingTabs.get(selectedTab);
        if (factory != null) {
            JComponent realComponent = factory.get();
            if (realComponent != null) {
                pendingTabs.remove(selectedTab);
                tabbedPane.setComponentAt(index, realComponent);
            }
        } else if (refreshIfLoaded && selectedTab instanceof Refreshable r) {
            r.refresh();
        }
    }
}