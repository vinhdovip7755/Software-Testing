package com.bookstore.gui.panel.EmployeeTab;

import com.bookstore.util.Refreshable;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

public class EmployeeTabbedPane extends JPanel implements Refreshable {
    private JTabbedPane tabbedPane = new JTabbedPane();

    public EmployeeTabbedPane() {
        setLayout(new BorderLayout());
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_CENTER);

        tabbedPane.addTab("Danh sách nhân viên", new EmployeePanel());
        tabbedPane.addTab("Bảng lương theo tháng", new SalaryPanel());

        add(tabbedPane, BorderLayout.CENTER);

        tabbedPane.addChangeListener(e -> refresh());
    }

    @Override
    public void refresh() {
        Component selectedTab = tabbedPane.getSelectedComponent();
        if (selectedTab instanceof Refreshable) {
            ((Refreshable) selectedTab).refresh();
        }
    }
}
