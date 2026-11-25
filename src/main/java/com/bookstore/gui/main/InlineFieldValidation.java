package com.bookstore.gui.main;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.function.Supplier;
import java.util.regex.Pattern;

final class InlineFieldValidation {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Color ERROR_COLOR = Color.decode("#B42318");
    private static final Color SUCCESS_COLOR = Color.decode("#087443");

    private final JTextComponent field;
    private final Supplier<String> validationMessage;
    private final JLabel feedback = new JLabel(" ");
    private boolean touched;

    InlineFieldValidation(JTextComponent field, Supplier<String> validationMessage) {
        this.field = field;
        this.validationMessage = validationMessage;
        feedback.setName(field.getName() + ".feedback");
        feedback.setFont(feedback.getFont().deriveFont(12f));
        onChange(field, () -> {
            if (field.isEnabled() && field.isEditable()) {
                validate();
            }
        });
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent event) {
                if (!touched && !event.isTemporary() && field.isEnabled() && field.isEditable() && field.isShowing()) {
                    validate();
                }
            }
        });
    }

    JLabel feedback() {
        return feedback;
    }

    boolean validate() {
        touched = true;
        String message = validationMessage.get();
        showError(message);
        return message == null;
    }

    void refreshIfTouched() {
        if (touched) {
            validate();
        }
    }

    void clear() {
        touched = false;
        showError(null);
    }

    void showError(String message) {
        field.putClientProperty(FlatClientProperties.OUTLINE, message == null ? null : "error");
        field.getAccessibleContext().setAccessibleDescription(message);
        feedback.setForeground(ERROR_COLOR);
        feedback.setText(message == null ? " " : message);
    }

    void showSuccess(String message) {
        clear();
        feedback.setForeground(SUCCESS_COLOR);
        feedback.setText(message);
        field.getAccessibleContext().setAccessibleDescription(message);
    }

    static String emailError(String email) {
        String trimmedEmail = email.trim();
        if (trimmedEmail.isEmpty()) {
            return "Vui lòng nhập Email!";
        }
        return EMAIL_PATTERN.matcher(trimmedEmail).matches() ? null : "Định dạng Email không hợp lệ!";
    }

    static void onChange(JTextComponent field, Runnable action) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                action.run();
            }
        });
    }
}
