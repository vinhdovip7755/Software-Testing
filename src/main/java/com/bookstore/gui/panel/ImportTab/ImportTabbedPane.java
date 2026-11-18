package com.bookstore.gui.panel.ImportTab;

import com.bookstore.util.LazyTabbedPaneHelper;
import com.bookstore.util.PermissionUtil;
import com.bookstore.util.Refreshable;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

public class ImportTabbedPane extends JPanel implements Refreshable {
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final LazyTabbedPaneHelper lazyTabs = new LazyTabbedPaneHelper(tabbedPane);

    public ImportTabbedPane() {
        initUI();
    }

    @Override
    public void refresh() {
        lazyTabs.refreshSelectedTab();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_CENTER);

        if (PermissionUtil.hasViewPermission("MANAGE_IMPORT_TICKET")) {
            lazyTabs.addTab("Phiếu Nhập", ImportTicketPanel::new);
        }

        if (PermissionUtil.hasViewPermission("MANAGE_IMPORT")) {
            lazyTabs.addTab("Nhập Hàng", ImportPanel::new);
        }

        lazyTabs.initialize();
        add(tabbedPane, BorderLayout.CENTER);
    }
}
