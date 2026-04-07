package com.qorpy.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MockEmailService implements EmailService {

    @Override
    public void sendTemporaryPassword(String toEmail, String fullName, String tempPassword) {
        log.info("===== MOCK EMAIL =====");
        log.info("To: {}", toEmail);
        log.info("Subject: Your QORPY Admin Portal Temporary Password");
        log.info("Body:");
        log.info("Hello {},", fullName);
        log.info("Your temporary password is: {}", tempPassword);
        log.info("Please log in and change it immediately.");
        log.info("=======================");
    }
}