package com.bookstore.gui.panel.PriceTab;

import com.bookstore.util.PermissionUtil;
import com.bookstore.util.Refreshable;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

public class PriceTabbedPane extends JPanel implements Refreshable {
    private JTabbedPane tabbedPane = new JTabbedPane();
    private PricePanel pricePanel;
    private PromotionPanel promotionPanel;

    public PriceTabbedPane() {
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

        if (PermissionUtil.hasViewPermission("MANAGE_PRICE")) {
            pricePanel = new PricePanel();
            tabbedPane.addTab("Giá Bán", pricePanel);
        }

        if (PermissionUtil.hasViewPermission("MANAGE_PROMOTION")) {
            promotionPanel = new PromotionPanel();
            tabbedPane.addTab("Khuyến Mãi", promotionPanel);
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
