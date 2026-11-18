package com.bookstore.gui.panel.AccountTab;

import com.bookstore.util.PermissionUtil;
import com.bookstore.util.Refreshable;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AccountTabbedPane extends JPanel implements Refreshable {
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final Map<Component, Supplier<JComponent>> pendingTabs = new IdentityHashMap<>();
    private ProfilePanel profilePanel;

    public AccountTabbedPane() {
        initUI();
    }

    @Override
    public void refresh() {
        if (tabbedPane.getParent() == this) {
            activateSelectedTab(true);
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
                addLazyTab("Tài Khoản", AccountPanel::new);
            }

            if (hasManageRole) {
                addLazyTab("Quyền", RolePanel::new);
            }

            addLazyTab("Cá nhân", ProfilePanel::new);

            activateSelectedTab(false);
            add(tabbedPane, BorderLayout.CENTER);
            tabbedPane.addChangeListener(e -> activateSelectedTab(true));
        } else {
            profilePanel = new ProfilePanel();
            add(profilePanel, BorderLayout.CENTER);
        }
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
