package com.qorpy.api.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MockEmailService implements EmailService {

    private final Resend resend;

    @Value("${app.mail.from-address:noreply@univaciti.com}")
    private String fromAddress;

    @Override
    public void sendTemporaryPassword(String toEmail, String fullName, String tempPassword) {
        String content = """
                <p>Hello %s,</p>
                <p>An account has been created or reset for you on the QORPY Admin Portal.</p>
                <div style="background-color: #f8fafc; border: 1px solid #e2e8f0; padding: 16px; border-radius: 6px; margin: 20px 0;">
                    <p style="margin: 0;"><strong>Temporary Password:</strong> <code style="background: #e2e8f0; padding: 4px 8px; border-radius: 4px; font-family: monospace; font-size: 16px;">%s</code></p>
                </div>
                <p style="color: #6b7280; font-size: 14px;">For security purposes, please log in and change this password immediately upon your first access.</p>
                """.formatted(fullName, tempPassword);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromAddress)
                .to(toEmail)
                .subject("Your QORPY Admin Portal Temporary Password")

                .html(buildEmailTemplate("Account Access Details", content))
                .build();

        try {
            CreateEmailResponse response = resend.emails().send(params);
            log.info("Temporary password email successfully sent to: {}. Email ID: {}", toEmail, response.getId());
        } catch (ResendException e) {
            log.error("Failed to send temporary password email to: {}", toEmail, e);
        }
    }

    private String buildEmailTemplate(String title, String content) {
        String template = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin: 0; padding: 0; background-color: #f3f4f6; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
                    <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="padding: 40px 20px;">
                        <tr>
                            <td align="center">
                                <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);">
                                    
                                    <tr>
                                        <td style="background-color: #0f172a; padding: 24px 32px; text-align: center;">
                                            <h1 style="color: #ffffff; margin: 0; font-size: 20px; font-weight: 600; letter-spacing: 0.5px;">QORPY Admin Portal</h1>
                                        </td>
                                    </tr>
                                    
                                    <tr>
                                        <td style="padding: 40px 32px; color: #334155; font-size: 16px; line-height: 1.6;">
                                            <h2 style="color: #0f172a; margin-top: 0; font-size: 20px;">{{TITLE}}</h2>
                                            {{CONTENT}}
                                        </td>
                                    </tr>
                                    
                                    <tr>
                                        <td style="background-color: #f8fafc; border-top: 1px solid #e2e8f0; padding: 24px 32px; text-align: center;">
                                            <p style="margin: 0; color: #64748b; font-size: 13px;">&copy; 2026 Univaciti. All rights reserved.</p>
                                            <p style="margin: 8px 0 0 0; color: #94a3b8; font-size: 12px;">This is an automated message. Please do not reply directly to this email.</p>
                                        </td>
                                    </tr>
                                    
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """;

        return template
                .replace("{{TITLE}}", title)
                .replace("{{CONTENT}}", content);
    }
}