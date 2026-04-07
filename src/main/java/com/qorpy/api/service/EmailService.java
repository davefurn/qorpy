package com.qorpy.api.service;

public interface EmailService {
    void sendTemporaryPassword(String toEmail, String fullName, String tempPassword);
}