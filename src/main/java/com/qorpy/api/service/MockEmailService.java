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
        String emailBody = String.format(
                "Hello %s,\n\n" +
                        "Your temporary password is: %s\n\n" +
                        "Please log in and change it immediately.\n\n" +
                        "Best regards,\nThe Qorpy Team",
                fullName, tempPassword
        );

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromAddress)
                .to(toEmail)
                .subject("Your QORPY Admin Portal Temporary Password")
                .text(emailBody)
                .build();

        try {
            CreateEmailResponse response = resend.emails().send(params);
            log.info("Temporary password email successfully sent to: {}. Email ID: {}", toEmail, response.getId());
        } catch (ResendException e) {
            log.error("Failed to send temporary password email to: {}", toEmail, e);
        }
    }
}