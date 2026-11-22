package com.bookstore.util;

import java.util.Properties;
import java.security.SecureRandom;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailUtil {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String UPPER_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER_CHARS = "abcdefghijkmnopqrstuvwxyz";
    private static final String DIGIT_CHARS = "23456789";
    private static final String ALL_PASSWORD_CHARS = UPPER_CHARS + LOWER_CHARS + DIGIT_CHARS;
    private static final int TEMP_PASSWORD_LENGTH = 12;

    public static String generateOTP() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    public static String generateTemporaryPassword() {
        char[] chars = new char[TEMP_PASSWORD_LENGTH];
        chars[0] = UPPER_CHARS.charAt(SECURE_RANDOM.nextInt(UPPER_CHARS.length()));
        chars[1] = LOWER_CHARS.charAt(SECURE_RANDOM.nextInt(LOWER_CHARS.length()));
        chars[2] = DIGIT_CHARS.charAt(SECURE_RANDOM.nextInt(DIGIT_CHARS.length()));
        for (int i = 3; i < TEMP_PASSWORD_LENGTH; i++) {
            chars[i] = ALL_PASSWORD_CHARS.charAt(SECURE_RANDOM.nextInt(ALL_PASSWORD_CHARS.length()));
        }
        for (int i = chars.length - 1; i > 0; i--) {
            int swapIndex = SECURE_RANDOM.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[swapIndex];
            chars[swapIndex] = tmp;
        }
        return new String(chars);
    }

    private static Session getSession() {
        String senderEmail = setting("bookstore.smtp.username", "BOOKSTORE_SMTP_USERNAME");
        String appPassword = setting("bookstore.smtp.password", "BOOKSTORE_SMTP_PASSWORD");
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.checkserveridentity", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        return Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, appPassword);
            }
        });
    }

    private static InternetAddress parseSingleAddress(String address) throws MessagingException {
        if (address == null || address.isBlank()
                || address.contains(",") || address.contains(";")
                || address.contains("\r") || address.contains("\n")) {
            throw new MessagingException("Địa chỉ email không hợp lệ.");
        }
        return new InternetAddress(address.trim(), true);
    }

    private static String setting(String property, String environmentVariable) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            value = System.getenv(environmentVariable);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Chưa cấu hình SMTP: " + environmentVariable);
        }
        return value;
    }

    public static boolean sendOTP(String recipientEmail, String otpCode) {
        try {
            String sender = setting("bookstore.smtp.username", "BOOKSTORE_SMTP_USERNAME");
            MimeMessage message = new MimeMessage(getSession());
            message.setFrom(parseSingleAddress(sender));
            message.setRecipient(Message.RecipientType.TO, parseSingleAddress(recipientEmail));
            message.setSubject("Mã xác thực cấp lại mật khẩu - Phần mềm Quản lý Cửa hàng bán sách", "UTF-8");
            message.setText("Xin chào,\n\nMã xác thực (OTP) của bạn là: " + otpCode +
                    "\n\nVui lòng không chia sẻ mã này cho bất kỳ ai. Mã có hiệu lực trong thời gian ngắn.\n\nTrân trọng.", "UTF-8");

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean sendNewPassword(String recipientEmail, String newPassword) {
        try {
            String sender = setting("bookstore.smtp.username", "BOOKSTORE_SMTP_USERNAME");
            MimeMessage message = new MimeMessage(getSession());
            message.setFrom(parseSingleAddress(sender));
            message.setRecipient(Message.RecipientType.TO, parseSingleAddress(recipientEmail));
            message.setSubject("Cấp lại mật khẩu - Phần mềm Quản lý Cửa hàng bán sách", "UTF-8");
            message.setText("Xin chào,\n\nMật khẩu mới của bạn là: " + newPassword +
                    "\n\nVui lòng sử dụng mật khẩu này để đăng nhập và bạn có thể đổi lại mật khẩu nếu muốn.\n\nTrân trọng.", "UTF-8");

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}
