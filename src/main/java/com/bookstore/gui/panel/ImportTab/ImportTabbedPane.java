package com.bookstore.gui.panel.ImportTab;

import com.bookstore.util.PermissionUtil;
import com.bookstore.util.Refreshable;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

public class ImportTabbedPane extends JPanel implements Refreshable{
    private JTabbedPane tabbedPane = new JTabbedPane();
    private ImportTicketPanel importTicketPanel;
    private ImportPanel importPanel;

    public ImportTabbedPane() {
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

        if (PermissionUtil.hasViewPermission("MANAGE_IMPORT_TICKET")) {
            importTicketPanel = new ImportTicketPanel();
            tabbedPane.addTab("Phiếu Nhập", importTicketPanel);
        }

        if (PermissionUtil.hasViewPermission("MANAGE_IMPORT")) {
            importPanel = new ImportPanel();
            tabbedPane.addTab("Nhập Hàng", importPanel);
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
