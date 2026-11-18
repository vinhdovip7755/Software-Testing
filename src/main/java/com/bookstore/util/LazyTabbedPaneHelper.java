package com.bookstore.util;

import javax.swing.*;
import java.awt.*;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Manages lazy sub-tab instantiation for {@link JTabbedPane} containers so hidden
 * or unselected tabs are only constructed when first selected.
 */
public class LazyTabbedPaneHelper {
    private final JTabbedPane tabbedPane;
    private final Map<Component, Supplier<JComponent>> pendingTabs = new IdentityHashMap<>();
    private boolean initialized;

    public LazyTabbedPaneHelper(JTabbedPane tabbedPane) {
        this.tabbedPane = Objects.requireNonNull(tabbedPane, "tabbedPane must not be null");
    }

    public void addTab(String title, Supplier<JComponent> factory) {
        Objects.requireNonNull(factory, "factory must not be null");
        JPanel placeholder = new JPanel();
        pendingTabs.put(placeholder, factory);
        tabbedPane.addTab(title, placeholder);
    }

    /**
     * Materializes the initial selected tab without calling {@link Refreshable#refresh()}
     * and attaches the change listener for subsequent tab selections.
     */
    public void initialize() {
        activateSelectedTab(false);
        if (!initialized) {
            initialized = true;
            tabbedPane.addChangeListener(e -> activateSelectedTab(true));
        }
    }

    /**
     * Materializes the currently selected tab if it is still a placeholder, or calls
     * {@link Refreshable#refresh()} if it was already instantiated.
     */
    public void refreshSelectedTab() {
        activateSelectedTab(true);
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
