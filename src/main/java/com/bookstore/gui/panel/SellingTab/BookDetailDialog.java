package com.bookstore.gui.panel.SellingTab;

import com.bookstore.bus.SupplierBUS;
import com.bookstore.dto.BookDTO;
import com.bookstore.dto.SupplierDTO;
import com.bookstore.util.AppConstant;
import com.bookstore.util.MoneyFormatter;

import javax.swing.*;
import java.awt.*;

public class BookDetailDialog extends JDialog {
    private BookDTO book;
    private SupplierBUS supplierBUS;

    public BookDetailDialog(JFrame parent, BookDTO book) {
        super(parent, "Chi Tiết Sản Phẩm", true);
        this.book = book;
        this.supplierBUS = new SupplierBUS();
        initUI();
    }

    public void initUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 700);
        setResizable(false);
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setAlwaysOnTop(true);
        setLocationRelativeTo(getParent());

        JPanel pHeader = new JPanel(new BorderLayout());
        pHeader.setPreferredSize(new Dimension(getWidth(), 60));
        pHeader.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        pHeader.setBackground(Color.decode(AppConstant.GREEN_COLOR_CODE));

        JLabel lbHeader = new JLabel("Chi Tiết Sản Phẩm", SwingConstants.CENTER);
        lbHeader.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 22));
        lbHeader.setForeground(Color.WHITE);
        pHeader.add(lbHeader, BorderLayout.CENTER);

        JPanel mainContent = new JPanel();
        mainContent.setBackground(Color.WHITE);
        mainContent.setLayout(new BorderLayout(10,10));
        mainContent.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        JPanel contentContainer = new JPanel();
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));
        contentContainer.setOpaque(false);

        JPanel pTopContent = new JPanel(new BorderLayout(15,0));
        pTopContent.setOpaque(false);

        JPanel pImageWrapper = new JPanel(new BorderLayout());
        pImageWrapper.setOpaque(false);

        JLabel lbImage = new JLabel();
        lbImage.setOpaque(true);
        lbImage.setBackground(Color.decode("#06b962"));
        lbImage.setPreferredSize(new Dimension(200, 300));
        lbImage.setHorizontalAlignment(SwingConstants.CENTER);

        String imageName = book.getImage();
        if (imageName != null && !imageName.trim().isEmpty()) {
            String imagePath = "data/book_covers/" + imageName;
            java.io.File imageFile = new java.io.File(imagePath);
            if (!imageFile.exists()) {
                imagePath = imageName;
                imageFile = new java.io.File(imagePath);
            }
            
            if (imageFile.exists()) {
                ImageIcon icon = new ImageIcon(imagePath);
                if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
                    Image img = icon.getImage().getScaledInstance(200, 300, Image.SCALE_SMOOTH);
                    lbImage.setIcon(new ImageIcon(img));
                    lbImage.setText("");
                } else {
                    lbImage.setText("Không có ảnh");
                }
            } else {
                lbImage.setText("Không có ảnh");
            }
        } else {
            lbImage.setText("Chưa Cập nhật");
        }

        pImageWrapper.add(lbImage, BorderLayout.NORTH);
        pTopContent.add(pImageWrapper, BorderLayout.WEST);

        JPanel pBookDetail = new JPanel(new GridBagLayout());
        pBookDetail.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0,0,5,0);
        gbc.weightx = 1.0;

        int row = 0;

        SupplierDTO supplier = supplierBUS.getById(book.getSupplierId());
        String supplierName = (supplier != null) ? supplier.getSupplierName() : "Không rõ";

        addDetailRow(pBookDetail, gbc, row++,"Tên sản phẩm: ", book.getBookName());
        addDetailRow(pBookDetail, gbc, row++,"Thể loại: ", book.getCategoryName());
        addDetailRow(pBookDetail, gbc, row++, "Tags: ", book.getTagDetail());
        addDetailRow(pBookDetail, gbc, row++, "Giá bán:", MoneyFormatter.toVND(book.getSellingPrice()));
        addDetailRow(pBookDetail, gbc, row++, "Số lượng tồn:", String.valueOf(book.getQuantity()));
        addDetailRow(pBookDetail, gbc, row++, "Tác giả:", book.getAuthorsName());
        addDetailRow(pBookDetail, gbc, row++, "Dịch giả:", book.getTranslator() == null ? "Không có" : book.getTranslator());
        addDetailRow(pBookDetail, gbc, row++, "Nhà cung cấp:", supplierName);

        JPanel pDetailWrapper = new JPanel(new BorderLayout());
        pDetailWrapper.setOpaque(false);
        pDetailWrapper.add(pBookDetail, BorderLayout.NORTH);

        pTopContent.add(pDetailWrapper, BorderLayout.CENTER);

        JPanel pDescription = new JPanel(new BorderLayout(5,5));
        pDescription.setOpaque(false);
        pDescription.setPreferredSize(new Dimension(getWidth(), 200));
        pDescription.setMaximumSize(new Dimension(getWidth(), 150));

        JLabel lbDescTitle = new JLabel("Mô tả:");
        lbDescTitle.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        lbDescTitle.setForeground(Color.BLACK);

        String descText = book.getDescription();
        if (descText == null || descText.isEmpty()) {
            descText = "Sản phẩm chưa có mô tả chi tiết.";
        }
        JTextArea txtDesc = new JTextArea(descText);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        txtDesc.setEditable(false);
        txtDesc.setOpaque(false);
        txtDesc.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(txtDesc);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);

        pDescription.add(lbDescTitle, BorderLayout.NORTH);
        pDescription.add(scrollPane, BorderLayout.CENTER);

        contentContainer.add(pTopContent);
        contentContainer.add(pDescription);

        mainContent.add(contentContainer, BorderLayout.NORTH);

        add(pHeader, BorderLayout.NORTH);
        add(mainContent, BorderLayout.CENTER);
    }

    private void addDetailRow(JPanel panel, GridBagConstraints gbc, int row, String title, String value) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;

        JLabel lbTitle = new JLabel(title);
        lbTitle.setFont(new Font(AppConstant.FONT_NAME, Font.BOLD, 14));
        lbTitle.setForeground(Color.BLACK);
        lbTitle.setPreferredSize(new Dimension(110, 20));
        lbTitle.setVerticalAlignment(SwingConstants.TOP);
        panel.add(lbTitle, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1.0;

        JTextArea txtValue = new JTextArea(value);
        txtValue.setFont(new Font(AppConstant.FONT_NAME, Font.PLAIN, 14));
        txtValue.setForeground(Color.BLACK);
        txtValue.setLineWrap(true);
        txtValue.setWrapStyleWord(true);
        txtValue.setEditable(false);
        txtValue.setOpaque(false);
        txtValue.setBorder(null);

        panel.add(txtValue, gbc);
    }
}
