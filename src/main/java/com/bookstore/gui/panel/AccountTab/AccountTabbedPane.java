package com.bookstore.gui.panel.AccountTab;

import com.bookstore.util.Refreshable;
import com.bookstore.util.PermissionUtil;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

public class AccountTabbedPane extends JPanel implements Refreshable {
    private JTabbedPane tabbedPane = new JTabbedPane();
    private AccountPanel accountPanel;
    private RolePanel rolePanel;
    private ProfilePanel profilePanel;

    public AccountTabbedPane() {
        initUI();
    }

    @Override
    public void refresh() {
        if (tabbedPane.getParent() == this) {
            Component selectedTab = tabbedPane.getSelectedComponent();
            if (selectedTab instanceof Refreshable r) {
                r.refresh();
            }
        } else if (profilePanel != null) {
            profilePanel.refresh();
        }
    }

    private void initUI() {
        setLayout(new BorderLayout());
        
        boolean hasManageAccount = PermissionUtil.hasViewPermission("MANAGE_ACCOUNT");
        boolean hasManageRole = PermissionUtil.hasViewPermission("MANAGE_ROLE");

        profilePanel = new ProfilePanel();

        if (hasManageAccount || hasManageRole) {
            tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_CENTER);

            if (hasManageAccount) {
                accountPanel = new AccountPanel();
                tabbedPane.addTab("Tài Khoản", accountPanel);
            }

            if (hasManageRole) {
                rolePanel = new RolePanel();
                tabbedPane.addTab("Quyền", rolePanel);
            }

            tabbedPane.addTab("Cá nhân", profilePanel);

            add(tabbedPane, BorderLayout.CENTER);

            tabbedPane.addChangeListener(e -> {
                Component selectedTab = tabbedPane.getSelectedComponent();
                if (selectedTab instanceof Refreshable r) {
                    r.refresh();
                }
            });
        } else {
            add(profilePanel, BorderLayout.CENTER);
        }
    }
}
