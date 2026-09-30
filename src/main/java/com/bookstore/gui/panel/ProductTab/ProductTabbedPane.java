package com.bookstore.gui.panel.ProductTab;

import com.bookstore.util.PermissionUtil;
import com.bookstore.util.Refreshable;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;

public class ProductTabbedPane extends JPanel implements Refreshable{
    private JTabbedPane tabbedPane = new JTabbedPane();
    private ProductPanel productPanel;
    private AuthorPanel authorPanel;
    private CategoryPanel categoryPanel;
    private SupplierPanel supplierPanel;

    public ProductTabbedPane() {
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

        if (PermissionUtil.hasViewPermission("MANAGE_PRODUCT")) {
            productPanel = new ProductPanel();
            tabbedPane.addTab("Sản Phẩm", productPanel);
        }

        if (PermissionUtil.hasViewPermission("MANAGE_AUTHOR")) {
            authorPanel = new AuthorPanel();
            tabbedPane.addTab("Tác Giả", authorPanel);
        }

        if (PermissionUtil.hasViewPermission("MANAGE_CATEGORY")) {
            categoryPanel = new CategoryPanel();
            tabbedPane.addTab("Thể Loại", categoryPanel);
        }

        if (PermissionUtil.hasViewPermission("MANAGE_SUPPLIER")) {
            supplierPanel = new SupplierPanel();
            tabbedPane.addTab("Nhà Cung Cấp", supplierPanel);
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