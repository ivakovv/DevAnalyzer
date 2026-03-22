package ru.devanalyzer.gateway_service.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:DevAnalyzer}")
    private String appName;

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Сброс пароля - " + appName);
            helper.setText(buildPasswordResetEmailTemplate(resetLink), true);

            try {
                ClassPathResource logoResource = new ClassPathResource("templates/logo/logo.png");
                if (logoResource.exists()) {
                    helper.addInline("logo", logoResource);
                }
            } catch (Exception e) {
                log.warn("Logo file not found, email will be sent without logo", e);
            }

            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildPasswordResetEmailTemplate(String resetLink) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/password-reset-email.html");
            String template = Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);
            
            return template
                    .replace("{{appName}}", appName)
                    .replace("{{resetLink}}", resetLink);
        } catch (IOException e) {
            log.error("Failed to load email template", e);
            throw new RuntimeException("Failed to load email template", e);
        }
    }
}
