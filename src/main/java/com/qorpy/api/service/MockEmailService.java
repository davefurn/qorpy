package com.qorpy.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MockEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from-address:noreply@qorpy.com}")
    private String fromAddress;

    @Override
    public void sendTemporaryPassword(String toEmail, String fullName, String tempPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("Your QORPY Admin Portal Temporary Password");

            // Construct the email body
            String emailBody = String.format(
                    "Hello %s,\n\n" +
                            "Your temporary password is: %s\n\n" +
                            "Please log in and change it immediately.\n\n" +
                            "Best regards,\nThe Qorpy Team",
                    fullName, tempPassword
            );
            message.setText(emailBody);

            mailSender.send(message);
            log.info("Temporary password email successfully sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send temporary password email to: {}", toEmail, e);
            // Note: You can choose to throw a custom BusinessException here if you
            // want the user creation to fail when the email fails to send.
        }
    }
}