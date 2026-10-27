package com.group6.nhms.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailUtil {

    private static final Logger log = LoggerFactory.getLogger(EmailUtil.class);

    private final JavaMailSender mailSender;

    public EmailUtil(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Gửi Email HTML (Hóa đơn, Khôi phục mật khẩu, Thông báo)
     */
    public void sendHtmlEmail(String toEmail, String subject, String htmlContent) throws MessagingException {
        if (mailSender == null) {
            log.warn("JavaMailSender chưa được cấu hình. Bỏ qua việc gửi email tới: {}", toEmail);
            return;
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}
