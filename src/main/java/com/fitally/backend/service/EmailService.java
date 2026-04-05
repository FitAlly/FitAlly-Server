package com.fitally.backend.service;

public interface EmailService {
    void sendPasswordResetCode(String to, String code);
}
