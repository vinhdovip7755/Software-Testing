package com.bookstore.gui.panel.PriceTab;

import com.bookstore.util.LazyTabbedPaneHelper;
import com.bookstore.util.PermissionUtil;
import com.bookstore.util.Refreshable;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

public class PriceTabbedPane extends JPanel implements Refreshable {
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final LazyTabbedPaneHelper lazyTabs = new LazyTabbedPaneHelper(tabbedPane);

    public PriceTabbedPane() {
        initUI();
    }

    @Override
    public void refresh() {
        lazyTabs.refreshSelectedTab();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_CENTER);

        if (PermissionUtil.hasViewPermission("MANAGE_PRICE")) {
            lazyTabs.addTab("Giá Bán", PricePanel::new);
        }

        if (PermissionUtil.hasViewPermission("MANAGE_PROMOTION")) {
            lazyTabs.addTab("Khuyến Mãi", PromotionPanel::new);
        }

        lazyTabs.initialize();
        add(tabbedPane, BorderLayout.CENTER);
    }
}
