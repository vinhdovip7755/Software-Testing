package com.bookstore.util;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class SearchableComboBox<E> extends JComboBox<E> {
    private List<E> originalItems;
    private boolean isFiltering = false;

    public SearchableComboBox() {
        this.originalItems = new ArrayList<>();
        initUI();
    }

    public SearchableComboBox(List<E> items) {
        this.originalItems = new ArrayList<>(items);
        initUI();
    }

    private void initUI() {
        setEditable(true);
        updateModel(originalItems);

        JTextField textEditor = (JTextField) this.getEditor().getEditorComponent();
            textEditor.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_ENTER) {
                        return;
                    }
                    SwingUtilities.invokeLater(() -> filter(textEditor.getText()));
                }
            });
    }

    private void filter(String textInput) {
        isFiltering = true;
        List<E> filteredItems = new ArrayList<>();

        for (E item : originalItems) {
            if (item.toString().toLowerCase().contains(textInput.toLowerCase())) {
                filteredItems.add(item);
            }
        }

        updateModel(filteredItems);

        JTextField textEditor = (JTextField) this.getEditor().getEditorComponent();
        textEditor.setText(textInput);

        if (!filteredItems.isEmpty()) {
            showPopup();
        } else {
            hidePopup();
        }
        isFiltering = false;
    }

    private void updateModel(List<E> items) {
        DefaultComboBoxModel<E> model = new DefaultComboBoxModel<>();
        for (E item : items) {
            model.addElement(item);
        }
        super.setModel(model);
    }

    public void updateData(List<E> newItems) {
        this.originalItems = new ArrayList<>(newItems);
        updateModel(originalItems);
    }

    public void resetSelection() {
        updateModel(originalItems);
        if (getItemCount() > 0) {
            super.setSelectedIndex(0);
            JTextField textEditor = (JTextField) this.getEditor().getEditorComponent();
            Object selectedItem = getSelectedItem();
            textEditor.setText(selectedItem != null ? selectedItem.toString() : "");
        }
    }

    @Override
    public void setSelectedItem(Object object) {
        if (isFiltering) {
            return;
        }
        super.setSelectedItem(object);
    }
}