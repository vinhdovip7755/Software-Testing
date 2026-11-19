package com.bookstore.codedui;

import com.bookstore.gui.main.LoginFrame;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * CodedUITestBuilder - Cong cu ghi nhan hanh dong va tao khang dinh (Record & Add Assertions)
 * truc quan cho ung dung Java Swing BookStoreApp.
 * Mo phong giao dien cua Visual Studio UIMap - Coded UI Test Builder (Trang 1-4 tai lieu PDF).
 */
public class CodedUITestBuilder {

    private final List<RecordedStep> recordedSteps = new ArrayList<>();
    private final List<RecordedAssertion> recordedAssertions = new ArrayList<>();
    private final Map<Component, String> lastTextValues = new IdentityHashMap<>();
    private final Set<Window> builderWindows = Collections.newSetFromMap(new IdentityHashMap<>());

    private JFrame toolbarFrame;
    private JButton btnRecord;
    private JButton btnShowSteps;
    private JButton btnAssertions;
    private JButton btnGenerateCode;
    private JLabel lbStatus;

    private JFrame stepsFrame;
    private DefaultListModel<String> stepsListModel;
    private JList<String> stepsList;

    private JFrame assertionFrame;
    private JTree controlTree;
    private DefaultTreeModel controlTreeModel;
    private DefaultTableModel propertyTableModel;
    private JScrollPane controlMapScroll;
    private JToggleButton btnShowControlMap;
    private JToggleButton btnCrosshairPick;
    private Component selectedTargetComponent;

    private boolean recording = false;
    private boolean appLaunchedByRecorder = false;
    private boolean pickMode = false;
    private AWTEventListener globalListener;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatLightLaf.setup();
            new CodedUITestBuilder().start();
        });
    }

    public void start() {
        if (!showStartupOptionsDialog()) {
            return;
        }
        installGlobalEventListener();
        showBuilderToolbar();
    }

    /**
     * Buoc 2 (Trang 1 PDF): Hop thoai "Generate Code for Coded UI Test".
     */
    private boolean showStartupOptionsDialog() {
        JDialog dialog = new JDialog((Frame) null, "Generate Code for Coded UI Test", true);
        builderWindows.add(dialog);
        dialog.setSize(470, 250);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(10, 12));
        root.setBorder(new EmptyBorder(15, 18, 15, 18));
        root.setBackground(Color.WHITE);

        JLabel lbHeader = new JLabel("How do you want to create your coded UI test?");
        lbHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbHeader.setForeground(new Color(0, 82, 155));

        JLabel lbInfo = new JLabel("<html>The code file for the coded UI test has been added to your test project.<br>"
                + "To generate code for this test, you can select from the options below.</html>");
        lbInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel topPanel = new JPanel(new BorderLayout(0, 8));
        topPanel.setOpaque(false);
        topPanel.add(lbHeader, BorderLayout.NORTH);
        topPanel.add(lbInfo, BorderLayout.CENTER);

        JRadioButton rbRecord = new JRadioButton("Record actions, edit UI map or add assertions", true);
        rbRecord.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rbRecord.setOpaque(false);
        JLabel lbRecordSub = new JLabel("    Perform tasks in your application and generate code for your actions.");
        lbRecordSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbRecordSub.setForeground(Color.DARK_GRAY);

        JRadioButton rbExisting = new JRadioButton("Use an existing action recording", false);
        rbExisting.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rbExisting.setOpaque(false);

        ButtonGroup group = new ButtonGroup();
        group.add(rbRecord);
        group.add(rbExisting);

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setOpaque(false);
        optionsPanel.add(rbRecord);
        optionsPanel.add(lbRecordSub);
        optionsPanel.add(Box.createVerticalStrut(8));
        optionsPanel.add(rbExisting);

        final boolean[] accepted = {false};
        JButton btnOk = new JButton("OK");
        btnOk.setPreferredSize(new Dimension(85, 28));
        btnOk.addActionListener(e -> {
            accepted[0] = true;
            dialog.dispose();
        });

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setPreferredSize(new Dimension(85, 28));
        btnCancel.addActionListener(e -> {
            accepted[0] = false;
            dialog.dispose();
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnOk);
        bottomPanel.add(btnCancel);

        root.add(topPanel, BorderLayout.NORTH);
        root.add(optionsPanel, BorderLayout.CENTER);
        root.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.getRootPane().setDefaultButton(btnOk);
        dialog.setVisible(true);
        builderWindows.remove(dialog);
        return accepted[0];
    }

    /**
     * Buoc 3 (Trang 2 PDF): Cua so noi UIMap - Coded UI Test Builder.
     */
    private void showBuilderToolbar() {
        toolbarFrame = new JFrame("UIMap - Coded UI Test Builder");
        builderWindows.add(toolbarFrame);
        toolbarFrame.setAlwaysOnTop(true);
        toolbarFrame.setResizable(false);
        toolbarFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        toolbarFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                shutdownBuilder();
            }
        });

        JPanel panel = new JPanel(new BorderLayout(6, 4));
        panel.setBorder(new EmptyBorder(6, 8, 6, 8));
        panel.setBackground(new Color(245, 247, 250));

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnBar.setOpaque(false);

        btnRecord = new JButton("● Start Recording");
        btnRecord.setMnemonic(KeyEvent.VK_R);
        btnRecord.setToolTipText("Start / Pause Recording (Alt-R)");
        btnRecord.setForeground(new Color(198, 40, 40));
        btnRecord.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRecord.addActionListener(e -> toggleRecording());

        btnShowSteps = new JButton("Show Steps");
        btnShowSteps.setMnemonic(KeyEvent.VK_S);
        btnShowSteps.setToolTipText("Show Recorded Steps (Alt-S)");
        btnShowSteps.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnShowSteps.addActionListener(e -> showRecordedStepsWindow());

        btnAssertions = new JButton("⊕ Add Assertions");
        btnAssertions.setToolTipText("Inspect Swing Controls & Add Assertions");
        btnAssertions.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnAssertions.addActionListener(e -> showAddAssertionsWindow());

        btnGenerateCode = new JButton("Generate Code");
        btnGenerateCode.setMnemonic(KeyEvent.VK_G);
        btnGenerateCode.setToolTipText("Generate Java Coded UI Method (Alt-G)");
        btnGenerateCode.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnGenerateCode.setForeground(new Color(17, 71, 50));
        btnGenerateCode.addActionListener(e -> showGenerateCodeDialog());

        btnBar.add(btnRecord);
        btnBar.add(btnShowSteps);
        btnBar.add(btnAssertions);
        btnBar.add(btnGenerateCode);

        lbStatus = new JLabel("Ready | Steps: 0 | Assertions: 0");
        lbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbStatus.setForeground(Color.DARK_GRAY);

        panel.add(btnBar, BorderLayout.CENTER);
        panel.add(lbStatus, BorderLayout.SOUTH);

        toolbarFrame.setContentPane(panel);
        toolbarFrame.pack();

        Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int x = screenBounds.x + screenBounds.width - toolbarFrame.getWidth() - 20;
        int y = screenBounds.y + screenBounds.height - toolbarFrame.getHeight() - 20;
        toolbarFrame.setLocation(x, y);
        toolbarFrame.setVisible(true);
    }

    private void toggleRecording() {
        recording = !recording;
        if (recording) {
            btnRecord.setText("❚❚ Pause Recording");
            btnRecord.setForeground(new Color(21, 101, 192));
            if (!appLaunchedByRecorder && !hasTargetAppOpen()) {
                appLaunchedByRecorder = true;
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                loginFrame.setVisible(true);
                addStep(new RecordedStep(
                        "Launch 'com.bookstore.gui.main.LoginFrame'",
                        "ApplicationUnderTest.launch(\"com.bookstore.gui.main.LoginFrame\");"
                ));
            }
        } else {
            btnRecord.setText("● Resume Recording");
            btnRecord.setForeground(new Color(198, 40, 40));
        }
        updateStatusLabel();
    }

    private boolean hasTargetAppOpen() {
        for (Window w : Window.getWindows()) {
            if (w.isShowing() && !builderWindows.contains(w)) {
                return true;
            }
        }
        return false;
    }

    private void installGlobalEventListener() {
        globalListener = event -> {
            Object src = event.getSource();
            if (!(src instanceof Component comp)) {
                return;
            }
            Window ownerWindow = comp instanceof Window w ? w : SwingUtilities.getWindowAncestor(comp);
            if (ownerWindow != null && builderWindows.contains(ownerWindow)) {
                return;
            }

            if (pickMode && event instanceof MouseEvent me && me.getID() == MouseEvent.MOUSE_PRESSED) {
                me.consume();
                pickMode = false;
                if (btnCrosshairPick != null) {
                    btnCrosshairPick.setSelected(false);
                }
                selectComponentInAssertionInspector(comp);
                return;
            }

            if (!recording) {
                return;
            }

            if (event instanceof MouseEvent me && me.getID() == MouseEvent.MOUSE_RELEASED
                    && SwingUtilities.isLeftMouseButton(me)) {
                if (comp instanceof AbstractButton btn) {
                    String label = describeButton(btn);
                    String varName = toIdentifier("uI", label, "Button");
                    addStep(new RecordedStep(
                            "Click '" + label + "' button",
                            "Mouse.click(" + varName + ", new Point(" + me.getX() + ", " + me.getY() + "));"
                    ));
                }
            } else if (event instanceof FocusEvent fe && fe.getID() == FocusEvent.FOCUS_LOST && !fe.isTemporary()) {
                if (comp instanceof JTextComponent tc) {
                    String text = tc instanceof JPasswordField pf ? new String(pf.getPassword()) : tc.getText();
                    String prev = lastTextValues.get(tc);
                    if (!Objects.equals(prev, text) && !text.isEmpty()) {
                        lastTextValues.put(tc, text);
                        String fieldName = describeTextComponent(tc);
                        String varName = toIdentifier("uI", fieldName, "Edit");
                        String displayVal = tc instanceof JPasswordField ? "******" : text;
                        addStep(new RecordedStep(
                                "Type '" + displayVal + "' in '" + fieldName + "'",
                                "Keyboard.sendKeys(" + varName + ", \"" + escapeJava(text) + "\");"
                        ));
                    }
                }
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(
                globalListener,
                AWTEvent.MOUSE_EVENT_MASK | AWTEvent.FOCUS_EVENT_MASK
        );
    }

    private void addStep(RecordedStep step) {
        recordedSteps.add(step);
        if (stepsListModel != null) {
            stepsListModel.addElement(step.description);
        }
        updateStatusLabel();
    }

    private void updateStatusLabel() {
        if (lbStatus != null) {
            String state = recording ? "Recording..." : "Paused";
            lbStatus.setText(state + " | Steps: " + recordedSteps.size() + " | Assertions: " + recordedAssertions.size());
        }
    }

    /**
     * Buoc 5 (Trang 2 PDF): Cua so "Recorded Actions - Coded UI Test Builder" ho tro xoa buoc.
     */
    private void showRecordedStepsWindow() {
        if (stepsFrame != null && stepsFrame.isDisplayable()) {
            stepsFrame.toFront();
            return;
        }
        stepsFrame = new JFrame("Recorded Actions - Coded UI Test Builder");
        builderWindows.add(stepsFrame);
        stepsFrame.setAlwaysOnTop(true);
        stepsFrame.setSize(380, 260);
        stepsFrame.setLocation(toolbarFrame.getX(), Math.max(20, toolbarFrame.getY() - 270));

        stepsListModel = new DefaultListModel<>();
        for (RecordedStep s : recordedSteps) {
            stepsListModel.addElement(s.description);
        }
        stepsList = new JList<>(stepsListModel);
        stepsList.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPopupMenu popup = new JPopupMenu();
        JMenuItem miDelete = new JMenuItem("Delete Selected Action(s) - Del");
        miDelete.addActionListener(e -> deleteSelectedSteps());
        popup.add(miDelete);
        stepsList.setComponentPopupMenu(popup);

        stepsList.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteSteps");
        stepsList.getActionMap().put("deleteSteps", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedSteps();
            }
        });

        stepsFrame.setContentPane(new JScrollPane(stepsList));
        stepsFrame.setVisible(true);
    }

    private void deleteSelectedSteps() {
        int[] indices = stepsList.getSelectedIndices();
        if (indices.length == 0) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                stepsFrame,
                "Do you want to delete the selected recorded action(s)?",
                "Coded UI Test Builder",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            for (int i = indices.length - 1; i >= 0; i--) {
                int idx = indices[i];
                recordedSteps.remove(idx);
                stepsListModel.remove(idx);
            }
            updateStatusLabel();
        }
    }

    /**
     * Buoc 7-9 (Trang 3-4 PDF): Cua so "Add Assertions" voi UI Control Map va Properties.
     */
    private void showAddAssertionsWindow() {
        if (assertionFrame != null && assertionFrame.isDisplayable()) {
            refreshControlTree();
            assertionFrame.toFront();
            return;
        }
        assertionFrame = new JFrame("Add Assertions - Coded UI Test Builder");
        builderWindows.add(assertionFrame);
        assertionFrame.setAlwaysOnTop(true);
        assertionFrame.setSize(680, 420);
        assertionFrame.setLocation(Math.max(20, toolbarFrame.getX() - 300), Math.max(20, toolbarFrame.getY() - 430));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        btnShowControlMap = new JToggleButton("<< Hide UI Control Map", true);
        btnShowControlMap.addActionListener(e -> {
            boolean show = btnShowControlMap.isSelected();
            controlMapScroll.setVisible(show);
            btnShowControlMap.setText(show ? "<< Hide UI Control Map" : ">> Show UI Control Map");
            assertionFrame.revalidate();
        });

        btnCrosshairPick = new JToggleButton("⌖ Pick Control");
        btnCrosshairPick.setToolTipText("Click vao nut nay roi click len giao dien BookStoreApp de chon control");
        btnCrosshairPick.addActionListener(e -> pickMode = btnCrosshairPick.isSelected());

        JButton btnRefresh = new JButton("Refresh UI Tree");
        btnRefresh.addActionListener(e -> refreshControlTree());

        JButton btnAddAssertionTop = new JButton("Add Assertion... (Alt+A)");
        btnAddAssertionTop.setMnemonic(KeyEvent.VK_A);
        btnAddAssertionTop.addActionListener(e -> openAddAssertionDialogForSelectedRow());

        topBar.add(btnShowControlMap);
        topBar.add(btnCrosshairPick);
        topBar.add(btnRefresh);
        topBar.add(btnAddAssertionTop);

        controlTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode("UIMap"));
        controlTree = new JTree(controlTreeModel);
        controlTree.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        controlTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) controlTree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof ControlNodeWrapper wrapper) {
                selectedTargetComponent = wrapper.component;
                populateProperties(wrapper.component);
            }
        });
        controlMapScroll = new JScrollPane(controlTree);
        controlMapScroll.setPreferredSize(new Dimension(260, 350));

        propertyTableModel = new DefaultTableModel(new String[]{"Property", "Value"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable propertyTable = new JTable(propertyTableModel);
        propertyTable.setRowHeight(24);
        propertyTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPopupMenu propMenu = new JPopupMenu();
        JMenuItem miAddAssertion = new JMenuItem("Add Assertion... (Alt+A)");
        miAddAssertion.addActionListener(e -> openAddAssertionDialogForSelectedRow(propertyTable));
        JMenuItem miCopyValue = new JMenuItem("Copy Value to Clipboard");
        miCopyValue.addActionListener(e -> {
            int row = propertyTable.getSelectedRow();
            if (row >= 0) {
                Object val = propertyTableModel.getValueAt(row, 1);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                        new StringSelection(val != null ? val.toString() : ""), null
                );
            }
        });
        propMenu.add(miAddAssertion);
        propMenu.add(miCopyValue);
        propertyTable.setComponentPopupMenu(propMenu);

        propertyTable.getInputMap(JComponent.WHEN_FOCUSED)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_DOWN_MASK), "addAssertion");
        propertyTable.getActionMap().put("addAssertion", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openAddAssertionDialogForSelectedRow(propertyTable);
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, controlMapScroll, new JScrollPane(propertyTable));
        splitPane.setDividerLocation(260);

        assertionFrame.setLayout(new BorderLayout());
        assertionFrame.add(topBar, BorderLayout.NORTH);
        assertionFrame.add(splitPane, BorderLayout.CENTER);
        refreshControlTree();
        assertionFrame.setVisible(true);
    }

    private void refreshControlTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("UIMap");
        for (Window w : Window.getWindows()) {
            if (w.isShowing() && !builderWindows.contains(w)) {
                DefaultMutableTreeNode winNode = new DefaultMutableTreeNode(new ControlNodeWrapper(w));
                buildTreeNodes(w, winNode);
                root.add(winNode);
            }
        }
        controlTreeModel.setRoot(root);
        for (int i = 0; i < controlTree.getRowCount(); i++) {
            controlTree.expandRow(i);
        }
    }

    private void buildTreeNodes(Container container, DefaultMutableTreeNode parentNode) {
        for (Component c : container.getComponents()) {
            if (!c.isShowing()) {
                continue;
            }
            if (c instanceof AbstractButton || c instanceof JTextComponent || c instanceof JLabel
                    || c instanceof JTable || c instanceof JComboBox) {
                parentNode.add(new DefaultMutableTreeNode(new ControlNodeWrapper(c)));
            } else if (c instanceof Container sub) {
                buildTreeNodes(sub, parentNode);
            }
            if (c instanceof JTextComponent && c instanceof Container sub) {
                // Include trailing buttons inside FlatLaf text fields (e.g. password toggle eye)
                buildTreeNodes(sub, parentNode);
            }
        }
    }

    private void selectComponentInAssertionInspector(Component target) {
        if (assertionFrame == null || !assertionFrame.isDisplayable()) {
            showAddAssertionsWindow();
        }
        refreshControlTree();
        selectedTargetComponent = target;
        populateProperties(target);
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) controlTreeModel.getRoot();
        Enumeration<?> e = root.breadthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
            if (node.getUserObject() instanceof ControlNodeWrapper w && w.component == target) {
                TreePath path = new TreePath(node.getPath());
                controlTree.setSelectionPath(path);
                controlTree.scrollPathToVisible(path);
                break;
            }
        }
        assertionFrame.toFront();
    }

    private void populateProperties(Component comp) {
        propertyTableModel.setRowCount(0);
        if (comp == null) {
            return;
        }
        String controlType = comp instanceof JToggleButton ? "ToggleButton"
                : comp instanceof AbstractButton ? "Button"
                : comp instanceof JTextComponent ? "Edit"
                : comp instanceof JLabel ? "Text"
                : comp instanceof Window ? "Window"
                : comp.getClass().getSimpleName();
        String name = comp instanceof Window w ? UITestControl.getWindowTitle(w)
                : comp instanceof AbstractButton b ? UITestControl.stripHtml(b.getText())
                : comp instanceof JLabel l ? UITestControl.stripHtml(l.getText())
                : comp instanceof JTextComponent tc ? String.valueOf(UITestControl.getPlaceholderText(tc))
                : comp.getName();

        propertyTableModel.addRow(new Object[]{"ControlType", controlType});
        propertyTableModel.addRow(new Object[]{"TechnologyName", "Swing (FlatLaf)"});
        propertyTableModel.addRow(new Object[]{"ClassName", comp.getClass().getSimpleName()});
        propertyTableModel.addRow(new Object[]{"Name", name != null ? name : ""});
        propertyTableModel.addRow(new Object[]{"DisplayText", UITestControl.extractDisplayText(comp)});
        propertyTableModel.addRow(new Object[]{"Enabled", String.valueOf(comp.isEnabled())});
        propertyTableModel.addRow(new Object[]{"Visible", String.valueOf(comp.isVisible())});
    }

    private void openAddAssertionDialogForSelectedRow() {
        for (int i = 0; i < propertyTableModel.getRowCount(); i++) {
            if ("DisplayText".equals(propertyTableModel.getValueAt(i, 0))) {
                showAssertionInputDialog("DisplayText", String.valueOf(propertyTableModel.getValueAt(i, 1)));
                return;
            }
        }
        JOptionPane.showMessageDialog(assertionFrame, "Hay chon mot control trong UI Control Map truoc!");
    }

    private void openAddAssertionDialogForSelectedRow(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            openAddAssertionDialogForSelectedRow();
            return;
        }
        String propName = String.valueOf(propertyTableModel.getValueAt(row, 0));
        String propVal = String.valueOf(propertyTableModel.getValueAt(row, 1));
        showAssertionInputDialog(propName, propVal);
    }

    private void showAssertionInputDialog(String propName, String currentValue) {
        JDialog dlg = new JDialog(assertionFrame, "Add assertion for: " + propName, true);
        builderWindows.add(dlg);
        dlg.setSize(380, 230);
        dlg.setLocationRelativeTo(assertionFrame);

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.setBorder(new EmptyBorder(12, 14, 12, 14));

        JComboBox<String> cboComparator = new JComboBox<>(new String[]{"AreEqual", "AreNotEqual", "Contains"});
        JTextField txtExpected = new JTextField(currentValue);
        JTextField txtFailMsg = new JTextField(propName + " assertion is failed");

        form.add(new JLabel("Comparator:"));
        form.add(cboComparator);
        form.add(new JLabel("Comparison Value:"));
        form.add(txtExpected);
        form.add(new JLabel("Message on Assertion Failure:"));
        form.add(txtFailMsg);

        JButton btnOk = new JButton("OK");
        btnOk.addActionListener(e -> {
            String targetName = selectedTargetComponent != null
                    ? new ControlNodeWrapper(selectedTargetComponent).toString()
                    : "UIControl";
            recordedAssertions.add(new RecordedAssertion(
                    targetName,
                    propName,
                    String.valueOf(cboComparator.getSelectedItem()),
                    txtExpected.getText(),
                    txtFailMsg.getText()
            ));
            updateStatusLabel();
            dlg.dispose();
        });
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnOk);
        bottom.add(btnCancel);

        dlg.setLayout(new BorderLayout());
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(bottom, BorderLayout.SOUTH);
        dlg.setVisible(true);
        builderWindows.remove(dlg);
    }

    /**
     * Buoc 6 & 10 (Trang 3-4 PDF): Hop thoai "Generate Code - Coded UI Test Builder".
     */
    private void showGenerateCodeDialog() {
        JDialog dlg = new JDialog(toolbarFrame, "Generate Code - Coded UI Test Builder", true);
        builderWindows.add(dlg);
        dlg.setSize(400, 220);
        dlg.setLocationRelativeTo(toolbarFrame);

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.setBorder(new EmptyBorder(12, 15, 12, 15));

        String defaultName = !recordedAssertions.isEmpty() && recordedSteps.isEmpty()
                ? "AssertMethod1"
                : "RecordedActionMethod1";
        JTextField txtMethodName = new JTextField(defaultName);
        JTextField txtMethodDesc = new JTextField("Generated by Coded UI Test Builder for BookStoreManager");

        form.add(new JLabel("Method Name:"));
        form.add(txtMethodName);
        form.add(new JLabel("Method Description:"));
        form.add(txtMethodDesc);

        JButton btnAddGenerate = new JButton("Add and Generate");
        btnAddGenerate.addActionListener(e -> {
            String code = buildGeneratedJavaCode(txtMethodName.getText().trim(), txtMethodDesc.getText().trim());
            dlg.dispose();
            showGeneratedCodePreview(code);
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnAddGenerate);

        dlg.setLayout(new BorderLayout());
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(bottom, BorderLayout.SOUTH);
        dlg.setVisible(true);
        builderWindows.remove(dlg);
    }

    private String buildGeneratedJavaCode(String methodName, String description) {
        String safeMethod = methodName.isEmpty() ? "RecordedMethod1" : methodName;
        StringBuilder sb = new StringBuilder();
        sb.append("/**\n * ").append(description).append("\n */\n");
        sb.append("public void ").append(safeMethod).append("() {\n");
        for (RecordedStep step : recordedSteps) {
            sb.append("    // ").append(step.description).append("\n");
            sb.append("    ").append(step.javaStatement).append("\n");
        }
        for (RecordedAssertion assertion : recordedAssertions) {
            sb.append("    // Verify '").append(assertion.propertyName).append("' of ")
                    .append(assertion.targetName).append("\n");
            if ("AreNotEqual".equals(assertion.comparator)) {
                sb.append("    Assertions.assertNotEquals(\"")
                        .append(escapeJava(assertion.expectedValue)).append("\", actualValue, \"")
                        .append(escapeJava(assertion.failureMessage)).append("\");\n");
            } else if ("Contains".equals(assertion.comparator)) {
                sb.append("    Assertions.assertTrue(actualValue.contains(\"")
                        .append(escapeJava(assertion.expectedValue)).append("\"), \"")
                        .append(escapeJava(assertion.failureMessage)).append("\");\n");
            } else {
                sb.append("    Assertions.assertEquals(\"")
                        .append(escapeJava(assertion.expectedValue)).append("\", actualValue, \"")
                        .append(escapeJava(assertion.failureMessage)).append("\");\n");
            }
        }
        sb.append("}\n");
        return sb.toString();
    }

    private void showGeneratedCodePreview(String code) {
        JDialog preview = new JDialog(toolbarFrame, "Generated Coded UI Method", true);
        builderWindows.add(preview);
        preview.setSize(560, 360);
        preview.setLocationRelativeTo(toolbarFrame);

        JTextArea area = new JTextArea(code);
        area.setFont(new Font("Consolas", Font.PLAIN, 12));
        area.setEditable(false);

        JButton btnCopy = new JButton("Copy to Clipboard");
        btnCopy.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(code), null);
            JOptionPane.showMessageDialog(preview, "Da copy ma Java vao Clipboard!");
        });
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> preview.dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnCopy);
        bottom.add(btnClose);

        preview.setLayout(new BorderLayout());
        preview.add(new JScrollPane(area), BorderLayout.CENTER);
        preview.add(bottom, BorderLayout.SOUTH);
        preview.setVisible(true);
        builderWindows.remove(preview);
    }

    private void shutdownBuilder() {
        if (globalListener != null) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(globalListener);
            globalListener = null;
        }
        if (stepsFrame != null && stepsFrame.isDisplayable()) {
            stepsFrame.dispose();
        }
        if (assertionFrame != null && assertionFrame.isDisplayable()) {
            assertionFrame.dispose();
        }
    }

    private static String describeButton(AbstractButton btn) {
        String text = UITestControl.stripHtml(btn.getText());
        if (!text.isEmpty()) {
            return text;
        }
        if (btn instanceof JToggleButton) {
            return "ToggleEye";
        }
        return btn.getClass().getSimpleName();
    }

    private static String describeTextComponent(JTextComponent tc) {
        String ph = UITestControl.getPlaceholderText(tc);
        if (ph != null && !ph.isEmpty()) {
            return ph;
        }
        if (tc.getName() != null && !tc.getName().isEmpty()) {
            return tc.getName();
        }
        return tc.getClass().getSimpleName();
    }

    private static String toIdentifier(String prefix, String raw, String suffix) {
        String cleaned = raw.replaceAll("[^a-zA-Z0-9]", "");
        if (cleaned.isEmpty()) {
            cleaned = "Control";
        }
        return prefix + cleaned + suffix;
    }

    private static String escapeJava(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static class ControlNodeWrapper {
        final Component component;

        ControlNodeWrapper(Component component) {
            this.component = component;
        }

        @Override
        public String toString() {
            if (component instanceof Window w) {
                return w.getClass().getSimpleName() + " [" + UITestControl.getWindowTitle(w) + "]";
            }
            if (component instanceof AbstractButton b) {
                return b.getClass().getSimpleName() + " [" + describeButton(b) + "]";
            }
            if (component instanceof JTextComponent tc) {
                return tc.getClass().getSimpleName() + " [" + describeTextComponent(tc) + "]";
            }
            if (component instanceof JLabel l) {
                return "JLabel [" + UITestControl.stripHtml(l.getText()) + "]";
            }
            return component.getClass().getSimpleName();
        }
    }

    private record RecordedStep(String description, String javaStatement) {
    }

    private record RecordedAssertion(String targetName, String propertyName, String comparator,
                                     String expectedValue, String failureMessage) {
    }
}
