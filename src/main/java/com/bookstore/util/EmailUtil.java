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

    public static String generateOTP() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    private static Session getSession() {
        String senderEmail = setting("bookstore.smtp.username", "BOOKSTORE_SMTP_USERNAME");
        String appPassword = setting("bookstore.smtp.password", "BOOKSTORE_SMTP_PASSWORD");
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, appPassword);
            }
        });
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
            MimeMessage message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(setting("bookstore.smtp.username", "BOOKSTORE_SMTP_USERNAME")));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
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
            MimeMessage message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(setting("bookstore.smtp.username", "BOOKSTORE_SMTP_USERNAME")));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
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
