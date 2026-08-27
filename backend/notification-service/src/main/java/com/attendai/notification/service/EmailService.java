package com.attendai.notification.service;

import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Low-level email sender. Wraps JavaMailSender with error handling and async dispatch.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send an HTML email asynchronously.
     *
     * @return true on success, false on failure (caller logs the error)
     */
    @Async
    public java.util.concurrent.CompletableFuture<Boolean> sendHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email sent to {} subject='{}'", to, subject);
            return java.util.concurrent.CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            log.error("Failed to send email to {} — {}", to, e.getMessage());
            return java.util.concurrent.CompletableFuture.completedFuture(false);
        }
    }
}
