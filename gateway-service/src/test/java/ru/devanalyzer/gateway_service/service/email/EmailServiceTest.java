package ru.devanalyzer.gateway_service.service.email;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@devanalyzer.com");
        ReflectionTestUtils.setField(emailService, "appName", "DevAnalyzer");
    }

    @Test
    void sendPasswordResetEmail_Success() {

        String toEmail = "user@example.com";
        String resetLink = "http://localhost:3000/reset-password?token=test-token";
        MimeMessage mimeMessage = new MimeMessage((Session) null);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);


        emailService.sendPasswordResetEmail(toEmail, resetLink);


        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordResetEmail_MessagingError_ThrowsRuntimeException() {

        String toEmail = "user@example.com";
        String resetLink = "http://localhost:3000/reset-password?token=test-token";

        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mail server error"));


        assertThatCode(() -> emailService.sendPasswordResetEmail(toEmail, resetLink))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Mail server error");
    }

    @Test
    void sendEmailFallback_LogsError() throws Exception {

        String toEmail = "user@example.com";
        String resetLink = "http://localhost:3000/reset-password?token=test-token";
        Throwable cause = new RuntimeException("All retries failed");

        // рефлексия для вызова приватного метода
        var method = EmailService.class.getDeclaredMethod("sendEmailFallback", String.class, String.class, Throwable.class);
        method.setAccessible(true);


        assertThatCode(() -> method.invoke(emailService, toEmail, resetLink, cause))
                .doesNotThrowAnyException();

        // mailSender не должен вызываться
        verifyNoInteractions(mailSender);
    }

    @Test
    void buildPasswordResetEmailTemplate_ReplacesPlaceholders() throws Exception {

        String resetLink = "http://localhost:3000/reset-password?token=test-token";

        // рефлексия для вызова приватного метода
        var method = EmailService.class.getDeclaredMethod("buildPasswordResetEmailTemplate", String.class);
        method.setAccessible(true);


        String result = (String) method.invoke(emailService, resetLink);


        assertThat(result).contains("DevAnalyzer");
        assertThat(result).contains(resetLink);
        assertThat(result).doesNotContain("{{appName}}");
        assertThat(result).doesNotContain("{{resetLink}}");
    }
}
