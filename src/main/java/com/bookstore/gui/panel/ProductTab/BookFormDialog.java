package com.bookstore.gui.panel.ProductTab;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import com.bookstore.util.AppConstant;
import com.bookstore.dto.AuthorDTO;
import com.bookstore.dto.BookDTO;
import com.bookstore.dto.CategoryDTO;
import com.bookstore.dto.SupplierDTO;
import com.bookstore.bus.AuthorBUS;
import com.bookstore.bus.CategoryBUS;
import com.bookstore.bus.SupplierBUS;

public class BookFormDialog extends JDialog {
    private static final Color BG_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = Color.decode("#E0E0E0");
    private static final Color BUTTON_COLOR = Color.decode(AppConstant.BUTTON_COLOR);

    private JTextField nameField;
    private JTextArea descriptionArea;
    private JTextField translatorField;
    private JTextField yearField;
    private JTextField coverPriceField;
    private JPanel authorPanel;
    private JComboBox<String> categoryCombo;
    private JComboBox<String> supplierCombo;
    private JRadioButton activeRadio;
    private JRadioButton inactiveRadio;
    private JPanel tagPanel;
    private JLabel imageLabel;
    private String selectedImagePath;

    private BookDTO book;
    private boolean saved = false;

    private List<AuthorDTO> authors;
    private List<CategoryDTO> categories;
    private List<SupplierDTO> suppliers;
    private Set<String> allTags;

    private Set<Integer> selectedAuthorIds = new HashSet<>();
    private Set<String> selectedTags = new HashSet<>();
    private final AuthorBUS authorBUS = new AuthorBUS();
    private final CategoryBUS categoryBUS = new CategoryBUS();
    private final SupplierBUS supplierBUS = new SupplierBUS();

    public BookFormDialog(Frame parent, String title, BookDTO book,
                          List<AuthorDTO> authors,
                          List<CategoryDTO> categories,
                          List<SupplierDTO> suppliers,
                          Set<String> allTags) {
        super(parent, title, true);
        this.book = book;
        this.authors = authors;
        this.categories = categories;
        this.suppliers = suppliers;
        this.allTags = allTags;

        initUI();

        if (book != null) {
            loadBookData();
        }

        setSize(900, 650);
        setLocationRelativeTo(parent);
    }


    private void initUI() {
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout(20, 0));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel leftPanel = createImagePanel();
        mainPanel.add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = createFormPanel();
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createImagePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setPreferredSize(new Dimension(220, 0));

        imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(180, 250));
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);
        imageLabel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 2));
        imageLabel.setOpaque(true);
        imageLabel.setBackground(Color.decode("#F5F5F5"));

        imageLabel.setIcon(new ImageIcon(createBookPlaceholder(180, 250)));
        imageLabel.setText("Kéo và thả ảnh vào đây");
        imageLabel.setHorizontalTextPosition(JLabel.CENTER);
        imageLabel.setVerticalTextPosition(JLabel.BOTTOM);
        imageLabel.setForeground(Color.decode("#757575"));

        imageLabel.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                try {
                    @SuppressWarnings("unchecked")
                    java.util.List<File> files = (java.util.List<File>) support.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) {
                        return setSelectedImage(files.get(0));
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BookFormDialog.this,
                            "Không thể đọc file ảnh đã kéo thả.",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
                return false;
            }
        });
        panel.add(imageLabel, BorderLayout.CENTER);

        JLabel helperLabel = new JLabel("Định dạng: .jpg, .jpeg, .png, .webp", JLabel.CENTER);
        helperLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        helperLabel.setForeground(Color.decode("#777777"));

        JButton uploadButton = new JButton("Browse ảnh");
        uploadButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        uploadButton.setBackground(Color.decode("#E0E0E0"));
        uploadButton.setForeground(Color.decode("#333333"));
        uploadButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        uploadButton.setFocusPainted(false);
        uploadButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        uploadButton.addActionListener(e -> openImageChooser());

        JPanel actionPanel = new JPanel(new BorderLayout(0, 8));
        actionPanel.setBackground(BG_COLOR);
        actionPanel.add(helperLabel, BorderLayout.NORTH);
        actionPanel.add(uploadButton, BorderLayout.SOUTH);

        panel.add(actionPanel, BorderLayout.SOUTH);


        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_COLOR);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_COLOR);
        wrapper.add(scrollPane, BorderLayout.CENTER);

        nameField = new JTextField();
        nameField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (nameField.getText().trim().isEmpty()) {
                    nameField.putClientProperty(com.formdev.flatlaf.FlatClientProperties.OUTLINE, "error");
                    javax.swing.JOptionPane.showMessageDialog(BookFormDialog.this, "Vui lòng nhập tên sách!", "Cảnh báo", javax.swing.JOptionPane.WARNING_MESSAGE);
                    nameField.requestFocusInWindow();
                } else {
                    nameField.putClientProperty(com.formdev.flatlaf.FlatClientProperties.OUTLINE, null);
                }
            }
        });
        addFormField(panel, "Tên sách *", nameField, true);

        authorPanel = new JPanel();
        authorPanel.setLayout(new BoxLayout(authorPanel, BoxLayout.Y_AXIS));
        authorPanel.setBackground(BG_COLOR);
        addFormSection(panel, "Tác giả *", authorPanel, true);
        refreshAuthorPanel();

        categoryCombo = new JComboBox<>(getCategoryNames());
        categoryCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (categoryCombo.getSelectedIndex() <= 0) {
                    categoryCombo.putClientProperty(com.formdev.flatlaf.FlatClientProperties.OUTLINE, "error");
                    javax.swing.JOptionPane.showMessageDialog(BookFormDialog.this, "Vui lòng chọn thể loại!", "Cảnh báo", javax.swing.JOptionPane.WARNING_MESSAGE);
                    categoryCombo.requestFocusInWindow();
                } else {
                    categoryCombo.putClientProperty(com.formdev.flatlaf.FlatClientProperties.OUTLINE, null);
                }
            }
        });
        categoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        addFormField(panel, "Thể loại *", createQuickAddField(categoryCombo, e -> quickAddCategory()), true);

        supplierCombo = new JComboBox<>(getSupplierNames());
        supplierCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (supplierCombo.getSelectedIndex() <= 0) {
                    supplierCombo.putClientProperty(com.formdev.flatlaf.FlatClientProperties.OUTLINE, "error");
                    javax.swing.JOptionPane.showMessageDialog(BookFormDialog.this, "Vui lòng chọn nhà cung cấp!", "Cảnh báo", javax.swing.JOptionPane.WARNING_MESSAGE);
                    supplierCombo.requestFocusInWindow();
                } else {
                    supplierCombo.putClientProperty(com.formdev.flatlaf.FlatClientProperties.OUTLINE, null);
                }
            }
        });
        supplierCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        addFormField(panel, "Nhà cung cấp *", createQuickAddField(supplierCombo, e -> quickAddSupplier()), true);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        statusPanel.setBackground(BG_COLOR);

        activeRadio = new JRadioButton("Đang bán", true);
        inactiveRadio = new JRadioButton("Ngừng bán");

        activeRadio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        inactiveRadio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        activeRadio.setBackground(BG_COLOR);
        inactiveRadio.setBackground(BG_COLOR);

        ButtonGroup statusGroup = new ButtonGroup();
        statusGroup.add(activeRadio);
        statusGroup.add(inactiveRadio);

        statusPanel.add(activeRadio);
        statusPanel.add(inactiveRadio);

        addFormField(panel, "Trạng thái *", statusPanel, true);

        addFormField(panel, "Người dịch", translatorField = new JTextField(), false);
        addFormField(panel, "Giá bìa", coverPriceField = new JTextField(), false);
        addFormField(panel, "Năm xuất bản", yearField = new JTextField(), false);

        tagPanel = new JPanel();
        tagPanel.setLayout(new BoxLayout(tagPanel, BoxLayout.Y_AXIS));
        tagPanel.setBackground(BG_COLOR);
        addFormSection(panel, "Tags", tagPanel, false);
        refreshTagPanel();

        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setPreferredSize(new Dimension(400, 100));
        addFormField(panel, "Mô tả", descScroll, false);

        return wrapper;
    }

    private void refreshAuthorPanel() {
        authorPanel.removeAll();

        for (Integer authorId : selectedAuthorIds) {
            for (AuthorDTO author : authors) {
                if (author.getAuthorId() == authorId) {
                    JPanel authorChip = createRemovableChip(author.getAuthorName(), () -> {
                        selectedAuthorIds.remove(authorId);
                        refreshAuthorPanel();
                    });
                    authorPanel.add(authorChip);
                    authorPanel.add(Box.createVerticalStrut(5));
                    break;
                }
            }
        }

        JPanel authorButtonPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        authorButtonPanel.setBackground(BG_COLOR);
        authorButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        authorButtonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JButton addAuthorButton = new JButton("+ Thêm tác giả");
        styleAuthorActionButton(addAuthorButton);
        addAuthorButton.addActionListener(e -> showAuthorSelector());
        authorButtonPanel.add(addAuthorButton);

        JButton quickAddAuthorButton = new JButton("+ Thêm nhanh");
        styleAuthorActionButton(quickAddAuthorButton);
        quickAddAuthorButton.addActionListener(e -> quickAddAuthor());
        authorButtonPanel.add(quickAddAuthorButton);
        authorPanel.add(authorButtonPanel);

        authorPanel.revalidate();
        authorPanel.repaint();
    }

    private void styleAuthorActionButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setForeground(BUTTON_COLOR);
        button.setBackground(BG_COLOR);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BUTTON_COLOR, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(0, 34));
    }

    private void refreshTagPanel() {
        tagPanel.removeAll();

        JPanel chipsPanel = new JPanel(new GridLayout(0, 3, 8, 8));
        chipsPanel.setBackground(BG_COLOR);
        chipsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (String tag : selectedTags) {
            JPanel chip = createRemovableChip(tag, () -> {
                selectedTags.remove(tag);
                refreshTagPanel();
            });
            chipsPanel.add(chip);
        }

        tagPanel.add(chipsPanel);
        tagPanel.add(Box.createVerticalStrut(8));

        JPanel buttonPanel = new JPanel(new GridLayout(0, 4, 8, 8));
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton selectTagButton = new JButton("Chọn tags");
        selectTagButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        selectTagButton.setForeground(BUTTON_COLOR);
        selectTagButton.setBackground(BG_COLOR);
        selectTagButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BUTTON_COLOR, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        selectTagButton.setFocusPainted(false);
        selectTagButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        selectTagButton.addActionListener(e -> showTagSelector());

        JButton quickAddButton = new JButton("+ Thêm nhanh");
        quickAddButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        quickAddButton.setForeground(Color.decode("#666666"));
        quickAddButton.setBackground(BG_COLOR);
        quickAddButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        quickAddButton.setFocusPainted(false);
        quickAddButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        quickAddButton.addActionListener(e -> quickAddTag());

        buttonPanel.add(selectTagButton);
        buttonPanel.add(quickAddButton);

        tagPanel.add(buttonPanel);

        tagPanel.revalidate();
        tagPanel.repaint();
    }

    private JPanel createRemovableChip(String text, Runnable onRemove) {
        JPanel chip = new JPanel(new BorderLayout(8, 0));
        chip.setBackground(Color.decode("#E8F5E9"));
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BUTTON_COLOR, 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        chip.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(truncateChipText(text));
        label.setToolTipText(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(Color.decode("#2E7D32"));

        JButton removeButton = new JButton("×");
        removeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        removeButton.setForeground(Color.decode("#2E7D32"));
        removeButton.setBackground(Color.decode("#E8F5E9"));
        removeButton.setBorder(null);
        removeButton.setFocusPainted(false);
        removeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        removeButton.setPreferredSize(new Dimension(20, 20));
        removeButton.addActionListener(e -> onRemove.run());

        chip.add(label, BorderLayout.CENTER);
        chip.add(removeButton, BorderLayout.EAST);

        return chip;
    }

    private void showAuthorSelector() {
        JDialog dialog = new JDialog(this, "Chọn tác giả", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(450, 450);
        dialog.setLocationRelativeTo(this);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        contentPanel.setBackground(BG_COLOR);

        JLabel title = new JLabel("Chọn tác giả cho sách");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JPanel topPanel = new JPanel(new BorderLayout(0, 8));
        topPanel.setBackground(BG_COLOR);
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(searchField, BorderLayout.CENTER);
        contentPanel.add(topPanel, BorderLayout.NORTH);

        JPanel listContainer = new JPanel(new BorderLayout());
        listContainer.setBackground(BG_COLOR);

        Map<Integer, JCheckBox> allCheckBoxes = new HashMap<>();

        for (AuthorDTO author : authors) {
            JCheckBox cb = new JCheckBox(author.getAuthorName());
            cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            cb.setBackground(BG_COLOR);
            cb.setSelected(selectedAuthorIds.contains(author.getAuthorId()));
            cb.setAlignmentX(Component.LEFT_ALIGNMENT);
            cb.putClientProperty("authorId", author.getAuthorId());
            cb.putClientProperty("authorName", author.getAuthorName().toLowerCase());
            allCheckBoxes.put(author.getAuthorId(), cb);
        }

        Runnable rebuildList = () -> {
            String keyword = searchField.getText().toLowerCase().trim();

            JPanel listPanel = new JPanel();
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            listPanel.setBackground(BG_COLOR);

            for (JCheckBox checkBox : allCheckBoxes.values()) {
                String authorName = (String) checkBox.getClientProperty("authorName");
                boolean matches = keyword.isEmpty() || (authorName != null && authorName.contains(keyword));

                if (matches) {
                    listPanel.add(checkBox);
                    listPanel.add(Box.createVerticalStrut(5));
                }
            }

            listContainer.removeAll();
            JScrollPane scrollPane = new JScrollPane(listPanel);
            scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            listContainer.add(scrollPane, BorderLayout.CENTER);
            listContainer.revalidate();
            listContainer.repaint();
        };

        rebuildList.run();

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                rebuildList.run();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                rebuildList.run();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                rebuildList.run();
            }
        });

        contentPanel.add(listContainer, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(BG_COLOR);

        JButton cancelButton = createStyledButton("Hủy", Color.decode("#757575"));
        cancelButton.addActionListener(e -> dialog.dispose());

        JButton okButton = createStyledButton("Xác nhận", BUTTON_COLOR);
        okButton.addActionListener(e -> {
            selectedAuthorIds.clear();
            for (Map.Entry<Integer, JCheckBox> entry : allCheckBoxes.entrySet()) {
                if (entry.getValue().isSelected()) {
                    selectedAuthorIds.add(entry.getKey());
                }
            }
            refreshAuthorPanel();
            dialog.dispose();
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(contentPanel);
        dialog.setVisible(true);
    }

    private String truncateChipText(String text) {
        if (text == null) {
            return "";
        }
        int maxLength = 24;
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }


    private void showTagSelector() {
        JDialog dialog = new JDialog(this, "Chọn tags", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        contentPanel.setBackground(BG_COLOR);

        JLabel title = new JLabel("Chọn tags cho sách");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        contentPanel.add(title, BorderLayout.NORTH);

        JPanel chipsPanel = new JPanel(new GridLayout(0, 3, 8, 8));
        chipsPanel.setBackground(BG_COLOR);

        Map<String, JToggleButton> toggleButtons = new HashMap<>();

        for (String tag : allTags) {
            JToggleButton chip = createSelectableTagChip(tag);
            chip.setSelected(selectedTags.contains(tag));
            toggleButtons.put(tag, chip);
            chipsPanel.add(chip);
        }

        JScrollPane scrollPane = new JScrollPane(chipsPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(BG_COLOR);

        JButton cancelButton = createStyledButton("Hủy", Color.decode("#757575"));
        cancelButton.addActionListener(e -> dialog.dispose());

        JButton okButton = createStyledButton("Xác nhận", BUTTON_COLOR);
        okButton.addActionListener(e -> {
            selectedTags.clear();
            for (Map.Entry<String, JToggleButton> entry : toggleButtons.entrySet()) {
                if (entry.getValue().isSelected()) {
                    selectedTags.add(entry.getKey());
                }
            }
            refreshTagPanel();
            dialog.dispose();
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(contentPanel);
        dialog.setVisible(true);
    }

    private void quickAddTag() {
        String newTag = JOptionPane.showInputDialog(this,
                "Nhập tên tag mới:",
                "Thêm tag nhanh",
                JOptionPane.PLAIN_MESSAGE);

        if (newTag != null && !newTag.trim().isEmpty()) {
            newTag = newTag.trim();
            allTags.add(newTag);
            selectedTags.add(newTag);
            refreshTagPanel();
        }
    }

    private JToggleButton createSelectableTagChip(String text) {
        JToggleButton chip = new JToggleButton(text);
        chip.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chip.setFocusPainted(false);
        chip.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chip.setBorder(BorderFactory.createEmptyBorder(6, 15, 6, 15));

        chip.setBackground(Color.decode("#F5F5F5"));
        chip.setForeground(Color.decode("#333333"));

        chip.addItemListener(e -> {
            if (chip.isSelected()) {
                chip.setBackground(BUTTON_COLOR);
                chip.setForeground(Color.WHITE);
            } else {
                chip.setBackground(Color.decode("#F5F5F5"));
                chip.setForeground(Color.decode("#333333"));
            }
        });

        return chip;
    }

    private void openImageChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn ảnh bìa sách");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image files (*.jpg, *.jpeg, *.png, *.webp)", "jpg", "jpeg", "png", "webp"));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            setSelectedImage(chooser.getSelectedFile());
        }
    }

    private boolean setSelectedImage(File file) {
        if (file == null || !file.exists()) {
            return false;
        }

        if (!isAllowedImage(file)) {
            JOptionPane.showMessageDialog(this,
                    "Chỉ chấp nhận định dạng ảnh: .jpg, .jpeg, .png, .webp",
                    "Định dạng không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        ImageIcon icon = new ImageIcon(file.getAbsolutePath());
        if (icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Không thể đọc file ảnh đã chọn.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        selectedImagePath = file.getAbsolutePath();
        Image preview = icon.getImage().getScaledInstance(180, 250, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(preview));
        imageLabel.setText("");
        return true;
    }

    private boolean isAllowedImage(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp");
    }

    private void addFormField(JPanel parent, String labelText, Component field, boolean required) {
        JPanel row = new JPanel(new BorderLayout(10, 5));
        row.setBackground(BG_COLOR);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, field instanceof JScrollPane ? 120 : 50));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setPreferredSize(new Dimension(120, 20));
        if (required) {
            label.setForeground(Color.decode("#333333"));
        }

        if (field instanceof JTextField) {
            JTextField tf = (JTextField) field;
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
        }

        row.add(label, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);

        parent.add(row);
        parent.add(Box.createVerticalStrut(12));
    }

    private JPanel createQuickAddField(JComboBox<String> combo, ActionListener listener) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBackground(BG_COLOR);
        JButton button = new JButton("+ Thêm nhanh");
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setForeground(BUTTON_COLOR);
        button.setBackground(BG_COLOR);
        button.setBorder(BorderFactory.createLineBorder(BUTTON_COLOR));
        button.addActionListener(listener);
        panel.add(combo, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
        return panel;
    }

    private void quickAddAuthor() {
        JTextField name = new JTextField();
        JTextField nationality = new JTextField();
        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("Tên tác giả:"));
        form.add(name);
        form.add(new JLabel("Quốc tịch:"));
        form.add(nationality);

        int result = JOptionPane.showConfirmDialog(this, form, "Thêm tác giả nhanh",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        AuthorDTO author = new AuthorDTO();
        author.setAuthorName(name.getText().trim());
        author.setNationality(nationality.getText().trim());
        String message = authorBUS.addAuthor(author);
        if (!message.toLowerCase().contains("thành công")) {
            JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        authors = authorBUS.selectAllAuthors();
        for (AuthorDTO item : authors) {
            if (item.getAuthorName().equalsIgnoreCase(author.getAuthorName())) {
                selectedAuthorIds.add(item.getAuthorId());
                break;
            }
        }
        refreshAuthorPanel();
    }

    private void quickAddCategory() {
        String name = JOptionPane.showInputDialog(this, "Tên thể loại:", "Thêm thể loại nhanh",
                JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) return;

        CategoryDTO category = new CategoryDTO();
        category.setCategoryName(name.trim());
        String message = categoryBUS.addCategory(category);
        if (!message.toLowerCase().contains("thành công")) {
            JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        categories = categoryBUS.selectAllCategories();
        categoryCombo.setModel(new DefaultComboBoxModel<>(getCategoryNames()));
        categoryCombo.setSelectedItem(name.trim());
    }

    private void quickAddSupplier() {
        JTextField name = new JTextField();
        JTextField phone = new JTextField();
        JTextField address = new JTextField();
        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Tên nhà cung cấp:"));
        form.add(name);
        form.add(new JLabel("Số điện thoại:"));
        form.add(phone);
        form.add(new JLabel("Địa chỉ:"));
        form.add(address);

        int result = JOptionPane.showConfirmDialog(this, form, "Thêm nhà cung cấp nhanh",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        SupplierDTO supplier = new SupplierDTO();
        supplier.setSupplierName(name.getText().trim());
        supplier.setSupplierPhone(phone.getText().trim());
        supplier.setSupplierAddress(address.getText().trim());
        String message = supplierBUS.addSupplier(supplier);
        if (!message.toLowerCase().contains("thành công")) {
            JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        suppliers = supplierBUS.selectAll();
        supplierCombo.setModel(new DefaultComboBoxModel<>(getSupplierNames()));
        supplierCombo.setSelectedItem(name.getText().trim());
    }

    private void addFormSection(JPanel parent, String labelText, JPanel content, boolean required) {
        JPanel row = new JPanel(new BorderLayout(10, 5));
        row.setBackground(BG_COLOR);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setPreferredSize(new Dimension(120, 20));
        label.setVerticalAlignment(JLabel.TOP);

        row.add(label, BorderLayout.WEST);
        row.add(content, BorderLayout.CENTER);

        parent.add(row);
        parent.add(Box.createVerticalStrut(12));
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JButton cancelButton = createStyledButton("Hủy", Color.decode("#757575"));
        cancelButton.addActionListener(e -> dispose());

        JButton saveButton = createStyledButton("Hoàn thành", BUTTON_COLOR);
        saveButton.addActionListener(e -> saveBook());

        panel.add(cancelButton);
        panel.add(saveButton);

        return panel;
    }

    private void saveBook() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập tên sách!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus();
            return;
        }

        if (selectedAuthorIds.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn ít nhất một tác giả!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (categoryCombo.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn thể loại!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (supplierCombo.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn nhà cung cấp!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double coverPrice;
        int publicationYear;
        try {
            coverPrice = coverPriceField.getText().trim().isEmpty() ? 0 : Double.parseDouble(coverPriceField.getText().trim());
            publicationYear = yearField.getText().trim().isEmpty() ? 0 : Integer.parseInt(yearField.getText().trim());
            if (coverPrice < 0 || publicationYear < 0 || publicationYear > 3000) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Giá bìa và năm xuất bản không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn lưu thông tin sách này?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        if (book == null) {
            book = new BookDTO();
            book.setBookId(0);
            book.setBookName(nameField.getText().trim());
            book.setCoverPrice(coverPrice);
            book.setPublicationYear(publicationYear);
            book.setSellingPrice(coverPrice);
            book.setQuantity(0);
            book.setTranslator(translatorField.getText().trim().isEmpty() ? null : translatorField.getText().trim());
            
            // Logic to copy image to data/book_covers and save only filename
            String finalImageName = null;
            if (selectedImagePath != null && !selectedImagePath.trim().isEmpty()) {
                java.io.File sourceFile = new java.io.File(selectedImagePath);
                java.io.File destFolder = new java.io.File("data/book_covers");
                if (!destFolder.exists()) destFolder.mkdirs();
                
                if (sourceFile.exists()) {
                    if (sourceFile.getAbsolutePath().startsWith(destFolder.getAbsolutePath())) {
                        finalImageName = sourceFile.getName();
                    } else {
                        finalImageName = System.currentTimeMillis() + "_" + sourceFile.getName();
                        java.io.File destFile = new java.io.File(destFolder, finalImageName);
                        try {
                            java.nio.file.Files.copy(sourceFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        } catch (java.io.IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
            book.setImage(finalImageName);
            
            book.setDescription(descriptionArea.getText().trim().isEmpty() ? null : descriptionArea.getText().trim());
            book.setStatus(activeRadio.isSelected() ? 1 : 0);
            book.setCategoryId(categories.get(categoryCombo.getSelectedIndex() - 1).getCategoryId());
            book.setSupplierId(suppliers.get(supplierCombo.getSelectedIndex() - 1).getSupplierId());
            book.setTagDetail(selectedTags.isEmpty() ? null : String.join(",", selectedTags));
            book.getAuthorIdsList().addAll(selectedAuthorIds);
        } else {
            book.setBookName(nameField.getText().trim());
            // Giữ nguyên giá bìa và giá bán khi sửa sách
            book.setPublicationYear(publicationYear);
            book.setTranslator(translatorField.getText().trim().isEmpty() ? null : translatorField.getText().trim());
            book.setDescription(descriptionArea.getText().trim().isEmpty() ? null : descriptionArea.getText().trim());
            book.setStatus(activeRadio.isSelected() ? 1 : 0);
            book.setCategoryId(categories.get(categoryCombo.getSelectedIndex() - 1).getCategoryId());
            book.setSupplierId(suppliers.get(supplierCombo.getSelectedIndex() - 1).getSupplierId());
            book.setTagDetail(selectedTags.isEmpty() ? null : String.join(",", selectedTags));
            book.getAuthorIdsList().clear();
            book.getAuthorIdsList().addAll(selectedAuthorIds);
        }

        saved = true;
        dispose();
    }

    private void loadBookData() {
        nameField.setText(book.getBookName());
        translatorField.setText(book.getTranslator() != null ? book.getTranslator() : "");
        coverPriceField.setText(book.getCoverPrice() == 0 ? "" : String.valueOf((long) book.getCoverPrice()));
        coverPriceField.setEditable(false);
        coverPriceField.setFocusable(false);
        coverPriceField.setBackground(new Color(245, 245, 245));
        coverPriceField.setForeground(Color.DARK_GRAY);
        coverPriceField.setToolTipText("Không thể thay đổi giá bìa của sách đã tạo!");
        yearField.setText(book.getPublicationYear() == 0 ? "" : String.valueOf(book.getPublicationYear()));
        descriptionArea.setText(book.getDescription() != null ? book.getDescription() : "");
        if (book.getImage() != null && !book.getImage().trim().isEmpty()) {
            File imageFile = new File("data/book_covers/" + book.getImage());
            if (!imageFile.exists()) {
                imageFile = new File(book.getImage());
            }
            if (imageFile.exists()) {
                selectedImagePath = imageFile.getAbsolutePath();
                setSelectedImage(imageFile);
            }
        }

        if (book.getAuthorIdsList() != null) {
            selectedAuthorIds.addAll(book.getAuthorIdsList());
            refreshAuthorPanel();
        }

        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getCategoryId() == book.getCategoryId()) {
                categoryCombo.setSelectedIndex(i + 1);
                break;
            }
        }

        for (int i = 0; i < suppliers.size(); i++) {
            if (suppliers.get(i).getSupplierId() == book.getSupplierId()) {
                supplierCombo.setSelectedIndex(i + 1);
                break;
            }
        }

        if (book.getStatus() == 1) {
            activeRadio.setSelected(true);
        } else {
            inactiveRadio.setSelected(true);
        }

        if (book.getTagDetail() != null && !book.getTagDetail().isEmpty()) {
            selectedTags.addAll(Arrays.asList(book.getTagDetail().split(",")));

            Set<String> trimmed = new HashSet<>();
            for (String tag : selectedTags) {
                trimmed.add(tag.trim());
            }
            selectedTags = trimmed;
            refreshTagPanel();
        }
    }

    private String[] getCategoryNames() {
        String[] names = new String[categories.size() + 1];
        names[0] = "-- Chọn thể loại --";
        for (int i = 0; i < categories.size(); i++) {
            names[i + 1] = categories.get(i).getCategoryName();
        }
        return names;
    }

    private String[] getSupplierNames() {
        String[] names = new String[suppliers.size() + 1];
        names[0] = "-- Chọn nhà cung cấp --";
        for (int i = 0; i < suppliers.size(); i++) {
            names[i + 1] = suppliers.get(i).getSupplierName();
        }
        return names;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private Image createBookPlaceholder(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();

        g2d.setColor(Color.decode("#E0E0E0"));
        g2d.fillRect(0, 0, width, height);

        g2d.setColor(Color.decode("#BDBDBD"));
        g2d.drawRect(0, 0, width - 1, height - 1);

        g2d.setColor(Color.decode("#9E9E9E"));
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 50));
        String text = "📖";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        int y = ((height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, x, y);

        g2d.dispose();
        return img;
    }

    public boolean isSaved() {
        return saved;
    }

    public BookDTO getBook() {
        return book;
    }
}