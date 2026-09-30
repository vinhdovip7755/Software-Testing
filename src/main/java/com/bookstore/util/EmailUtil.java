package com.bookstore.util;

import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailUtil {
    // Replace with the generated App Password from Google Account
    private static final String SENDER_EMAIL = "hotro.adminhethong@gmail.com";
    private static final String APP_PASSWORD = "yvvh qlcf pvuq vaeg";

    public static String generateOTP() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        return String.format("%06d", number);
    }

    private static Session getSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
            }
        });
    }

    public static boolean sendOTP(String recipientEmail, String otpCode) {
        try {
            MimeMessage message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(SENDER_EMAIL));
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
            message.setFrom(new InternetAddress(SENDER_EMAIL));
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
