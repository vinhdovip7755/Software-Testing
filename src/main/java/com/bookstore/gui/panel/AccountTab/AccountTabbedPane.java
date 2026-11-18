package com.bookstore.gui.panel.AccountTab;

import com.bookstore.util.LazyTabbedPaneHelper;
import com.bookstore.util.PermissionUtil;
import com.bookstore.util.Refreshable;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

public class AccountTabbedPane extends JPanel implements Refreshable {
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final LazyTabbedPaneHelper lazyTabs = new LazyTabbedPaneHelper(tabbedPane);
    private ProfilePanel profilePanel;

    public AccountTabbedPane() {
        initUI();
    }

    @Override
    public void refresh() {
        if (tabbedPane.getParent() == this) {
            lazyTabs.refreshSelectedTab();
        } else if (profilePanel != null) {
            profilePanel.refresh();
        }
    }

    private void initUI() {
        setLayout(new BorderLayout());

        boolean hasManageAccount = PermissionUtil.hasViewPermission("MANAGE_ACCOUNT");
        boolean hasManageRole = PermissionUtil.hasViewPermission("MANAGE_ROLE");

        if (hasManageAccount || hasManageRole) {
            tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_CENTER);

            if (hasManageAccount) {
                lazyTabs.addTab("Tài Khoản", AccountPanel::new);
            }

            if (hasManageRole) {
                lazyTabs.addTab("Quyền", RolePanel::new);
            }

            lazyTabs.addTab("Cá nhân", ProfilePanel::new);

            lazyTabs.initialize();
            add(tabbedPane, BorderLayout.CENTER);
        } else {
            profilePanel = new ProfilePanel();
            add(profilePanel, BorderLayout.CENTER);
        }
    }
}
