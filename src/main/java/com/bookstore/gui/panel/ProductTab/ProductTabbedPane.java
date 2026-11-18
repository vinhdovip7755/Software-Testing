package com.bookstore.gui.panel.ProductTab;

import com.bookstore.util.LazyTabbedPaneHelper;
import com.bookstore.util.PermissionUtil;
import com.bookstore.util.Refreshable;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

public class ProductTabbedPane extends JPanel implements Refreshable {
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final LazyTabbedPaneHelper lazyTabs = new LazyTabbedPaneHelper(tabbedPane);

    public ProductTabbedPane() {
        initUI();
    }

    @Override
    public void refresh() {
        lazyTabs.refreshSelectedTab();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_CENTER);

        if (PermissionUtil.hasViewPermission("MANAGE_PRODUCT")) {
            lazyTabs.addTab("Sản Phẩm", ProductPanel::new);
        }

        if (PermissionUtil.hasViewPermission("MANAGE_AUTHOR")) {
            lazyTabs.addTab("Tác Giả", AuthorPanel::new);
        }

        if (PermissionUtil.hasViewPermission("MANAGE_CATEGORY")) {
            lazyTabs.addTab("Thể Loại", CategoryPanel::new);
        }

        if (PermissionUtil.hasViewPermission("MANAGE_SUPPLIER")) {
            lazyTabs.addTab("Nhà Cung Cấp", SupplierPanel::new);
        }

        lazyTabs.initialize();
        add(tabbedPane, BorderLayout.CENTER);
    }
}