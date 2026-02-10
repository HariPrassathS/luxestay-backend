package com.hotel.service;

import com.hotel.domain.entity.EmailLog;
import com.hotel.domain.entity.User;
import com.hotel.repository.EmailLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for sending email notifications asynchronously.
 * Emails are logged to the database and sent via SMTP.
 * If email fails, login still succeeds - email is non-blocking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;

    @Value("${spring.mail.username:noreply@luxestay.com}")
    private String fromEmail;

    @Value("${app.name:LuxeStay}")
    private String appName;

    @Value("${app.url:http://localhost:5500}")
    private String appUrl;

    @Value("${spring.mail.enabled:true}")
    private boolean emailEnabled;

    /**
     * Send first login notification email asynchronously.
     * This email welcomes the user and prompts them to set up their password.
     */
    @Async
    @Transactional
    public void sendFirstLoginEmail(User user, String ipAddress, String userAgent) {
        log.info("Sending first login email to: {}", user.getEmail());

        String subject = "Welcome to " + appName + " - Complete Your Account Setup";
        String htmlContent = buildFirstLoginEmailContent(user, ipAddress, userAgent);

        sendEmail(user.getEmail(), user.getFullName(), subject, htmlContent, EmailLog.EmailType.FIRST_LOGIN);
    }

    /**
     * Send login notification for security awareness.
     */
    @Async
    @Transactional
    public void sendLoginNotificationEmail(User user, String ipAddress, String userAgent) {
        log.info("Sending login notification email to: {}", user.getEmail());

        String subject = "New Login to Your " + appName + " Account";
        String htmlContent = buildLoginNotificationEmailContent(user, ipAddress, userAgent);

        sendEmail(user.getEmail(), user.getFullName(), subject, htmlContent, EmailLog.EmailType.LOGIN_NOTIFICATION);
    }

    /**
     * Send password changed confirmation email.
     */
    @Async
    @Transactional
    public void sendPasswordChangedEmail(User user, String ipAddress) {
        log.info("Sending password changed email to: {}", user.getEmail());

        String subject = "Your " + appName + " Password Has Been Changed";
        String htmlContent = buildPasswordChangedEmailContent(user, ipAddress);

        sendEmail(user.getEmail(), user.getFullName(), subject, htmlContent, EmailLog.EmailType.PASSWORD_CHANGED);
    }

    /**
     * Send security alert email (e.g., multiple failed attempts).
     */
    @Async
    @Transactional
    public void sendSecurityAlertEmail(User user, String alertType, String details) {
        log.info("Sending security alert email to: {} - {}", user.getEmail(), alertType);

        String subject = "Security Alert - " + appName;
        String htmlContent = buildSecurityAlertEmailContent(user, alertType, details);

        sendEmail(user.getEmail(), user.getFullName(), subject, htmlContent, EmailLog.EmailType.SECURITY_ALERT);
    }

    /**
     * Core email sending method with logging.
     */
    private void sendEmail(String toEmail, String toName, String subject, String htmlContent, EmailLog.EmailType emailType) {
        // Create email log entry
        EmailLog emailLog = EmailLog.builder()
                .recipientEmail(toEmail)
                .recipientName(toName)
                .emailType(emailType)
                .subject(subject)
                .status(EmailLog.EmailStatus.PENDING)
                .build();
        emailLog = emailLogRepository.save(emailLog);

        // Check if email is disabled (dev mode)
        if (!emailEnabled) {
            log.info("Email sending disabled. Would have sent [{}] to {}", subject, toEmail);
            emailLog.setStatus(EmailLog.EmailStatus.SENT);
            emailLog.setSentAt(LocalDateTime.now());
            emailLog.setErrorMessage("Email disabled - logged only");
            emailLogRepository.save(emailLog);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, appName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            emailLog.setStatus(EmailLog.EmailStatus.SENT);
            emailLog.setSentAt(LocalDateTime.now());
            log.info("Email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            emailLog.setStatus(EmailLog.EmailStatus.FAILED);
            emailLog.setErrorMessage(e.getMessage());
            emailLog.setRetryCount(emailLog.getRetryCount() + 1);
        }

        emailLogRepository.save(emailLog);
    }

    // ==================== Email Template Builders ====================

    private String buildFirstLoginEmailContent(User user, String ipAddress, String userAgent) {
        String formattedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm"));
        String passwordSetupUrl = appUrl + "/set-password.html";

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;">
                <div style="background: linear-gradient(135deg, #1a1a2e 0%%, #16213e 100%%); padding: 30px; border-radius: 10px 10px 0 0; text-align: center;">
                    <h1 style="color: #d4af37; margin: 0; font-size: 28px;">💎 %s</h1>
                </div>
                <div style="background: #ffffff; padding: 30px; border-radius: 0 0 10px 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                    <h2 style="color: #1a1a2e; margin-top: 0;">Welcome, %s! 🎉</h2>
                    
                    <p>We detected your <strong>first login</strong> to your LuxeStay account. Welcome aboard!</p>
                    
                    <div style="background: #f8f9fa; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #d4af37;">
                        <p style="margin: 0;"><strong>📅 Time:</strong> %s</p>
                        <p style="margin: 5px 0 0 0;"><strong>📍 IP Address:</strong> %s</p>
                        <p style="margin: 5px 0 0 0;"><strong>🌐 Device:</strong> %s</p>
                    </div>
                    
                    <div style="background: linear-gradient(135deg, #d4af37 0%%, #c9a227 100%%); padding: 20px; border-radius: 8px; text-align: center; margin: 25px 0;">
                        <p style="color: #1a1a2e; margin: 0 0 15px 0; font-weight: 600;">Complete your account setup by setting a secure password:</p>
                        <a href="%s" style="display: inline-block; background: #1a1a2e; color: #d4af37; padding: 12px 30px; text-decoration: none; border-radius: 5px; font-weight: 600;">Set Your Password</a>
                    </div>
                    
                    <div style="background: #fff3cd; padding: 15px; border-radius: 8px; margin: 20px 0;">
                        <p style="margin: 0; color: #856404;"><strong>🔒 Security Tip:</strong> Choose a strong password with at least 8 characters, including uppercase, lowercase, numbers, and special characters.</p>
                    </div>
                    
                    <p style="color: #666; font-size: 14px;">If you did not log in, please contact our support team immediately.</p>
                    
                    <hr style="border: none; border-top: 1px solid #eee; margin: 25px 0;">
                    
                    <p style="color: #999; font-size: 12px; text-align: center;">
                        This is an automated message from %s. Please do not reply.<br>
                        © 2026 %s. All rights reserved.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(appName, user.getFirstName(), formattedTime, ipAddress != null ? ipAddress : "Unknown", 
                    truncateUserAgent(userAgent), passwordSetupUrl, appName, appName);
    }

    private String buildLoginNotificationEmailContent(User user, String ipAddress, String userAgent) {
        String formattedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm"));

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
            </head>
            <body style="font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;">
                <div style="background: linear-gradient(135deg, #1a1a2e 0%%, #16213e 100%%); padding: 30px; border-radius: 10px 10px 0 0; text-align: center;">
                    <h1 style="color: #d4af37; margin: 0; font-size: 28px;">💎 %s</h1>
                </div>
                <div style="background: #ffffff; padding: 30px; border-radius: 0 0 10px 10px;">
                    <h2 style="color: #1a1a2e; margin-top: 0;">New Login Detected</h2>
                    
                    <p>Hello %s,</p>
                    <p>We noticed a new login to your %s account.</p>
                    
                    <div style="background: #f8f9fa; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #28a745;">
                        <p style="margin: 0;"><strong>📅 Time:</strong> %s</p>
                        <p style="margin: 5px 0 0 0;"><strong>📍 IP Address:</strong> %s</p>
                        <p style="margin: 5px 0 0 0;"><strong>🌐 Device:</strong> %s</p>
                    </div>
                    
                    <p>If this was you, no action is needed. If you didn't log in, please change your password immediately and contact support.</p>
                    
                    <hr style="border: none; border-top: 1px solid #eee; margin: 25px 0;">
                    
                    <p style="color: #999; font-size: 12px; text-align: center;">
                        © 2026 %s. All rights reserved.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(appName, user.getFirstName(), appName, formattedTime, 
                    ipAddress != null ? ipAddress : "Unknown", truncateUserAgent(userAgent), appName);
    }

    private String buildPasswordChangedEmailContent(User user, String ipAddress) {
        String formattedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm"));

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
            </head>
            <body style="font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;">
                <div style="background: linear-gradient(135deg, #1a1a2e 0%%, #16213e 100%%); padding: 30px; border-radius: 10px 10px 0 0; text-align: center;">
                    <h1 style="color: #d4af37; margin: 0; font-size: 28px;">💎 %s</h1>
                </div>
                <div style="background: #ffffff; padding: 30px; border-radius: 0 0 10px 10px;">
                    <h2 style="color: #1a1a2e; margin-top: 0;">Password Changed Successfully</h2>
                    
                    <p>Hello %s,</p>
                    <p>Your password has been changed successfully.</p>
                    
                    <div style="background: #d4edda; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #28a745;">
                        <p style="margin: 0; color: #155724;"><strong>✅ Password Updated</strong></p>
                        <p style="margin: 5px 0 0 0; color: #155724;">Time: %s</p>
                        <p style="margin: 5px 0 0 0; color: #155724;">IP: %s</p>
                    </div>
                    
                    <div style="background: #f8d7da; padding: 15px; border-radius: 8px; margin: 20px 0;">
                        <p style="margin: 0; color: #721c24;"><strong>⚠️ Didn't make this change?</strong></p>
                        <p style="margin: 5px 0 0 0; color: #721c24;">Contact our support team immediately if you did not change your password.</p>
                    </div>
                    
                    <hr style="border: none; border-top: 1px solid #eee; margin: 25px 0;">
                    
                    <p style="color: #999; font-size: 12px; text-align: center;">
                        © 2026 %s. All rights reserved.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(appName, user.getFirstName(), formattedTime, ipAddress != null ? ipAddress : "Unknown", appName);
    }

    private String buildSecurityAlertEmailContent(User user, String alertType, String details) {
        String formattedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm"));

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
            </head>
            <body style="font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;">
                <div style="background: linear-gradient(135deg, #dc3545 0%%, #c82333 100%%); padding: 30px; border-radius: 10px 10px 0 0; text-align: center;">
                    <h1 style="color: #ffffff; margin: 0; font-size: 28px;">🚨 Security Alert</h1>
                </div>
                <div style="background: #ffffff; padding: 30px; border-radius: 0 0 10px 10px;">
                    <h2 style="color: #dc3545; margin-top: 0;">%s</h2>
                    
                    <p>Hello %s,</p>
                    <p>We detected unusual activity on your account:</p>
                    
                    <div style="background: #f8d7da; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #dc3545;">
                        <p style="margin: 0; color: #721c24;">%s</p>
                        <p style="margin: 5px 0 0 0; color: #721c24;">Time: %s</p>
                    </div>
                    
                    <p>If this was not you, please:</p>
                    <ol>
                        <li>Change your password immediately</li>
                        <li>Enable two-factor authentication</li>
                        <li>Contact our support team</li>
                    </ol>
                    
                    <hr style="border: none; border-top: 1px solid #eee; margin: 25px 0;">
                    
                    <p style="color: #999; font-size: 12px; text-align: center;">
                        © 2026 %s. All rights reserved.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(alertType, user.getFirstName(), details, formattedTime, appName);
    }

    private String truncateUserAgent(String userAgent) {
        if (userAgent == null) return "Unknown device";
        if (userAgent.length() > 100) {
            return userAgent.substring(0, 100) + "...";
        }
        return userAgent;
    }
}
