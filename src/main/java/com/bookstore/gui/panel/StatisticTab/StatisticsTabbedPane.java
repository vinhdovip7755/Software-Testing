package com.bookstore.gui.panel.StatisticTab;

import com.bookstore.util.Refreshable;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

public class StatisticsTabbedPane extends JPanel implements Refreshable{
    JTabbedPane tabbedPane = new JTabbedPane();

    public StatisticsTabbedPane(){
        initUI();
    }

    @Override
    public void refresh() {
        Component selectedTab = tabbedPane.getSelectedComponent();
        if (selectedTab instanceof Refreshable r) {
            r.refresh();
        }
    }

    private void initUI(){
        setLayout(new BorderLayout());
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_CENTER);
        tabbedPane.addTab("Doanh Thu", new FinancialStatsPanel());
        tabbedPane.addTab("Sản Phẩm", new ProductStatsPanel());
        tabbedPane.addTab("Khách Hàng", new CustomerStatsPanel());
        add(tabbedPane, BorderLayout.CENTER);

        tabbedPane.addChangeListener(e -> {
            Component selectedTab = tabbedPane.getSelectedComponent();
            if (selectedTab instanceof Refreshable r) {
                r.refresh();
            }
        });
    }
}