package com.fitally.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService{

    private final JavaMailSender javaMailSender;

    @Value("S{app.mail.from}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendPasswordResetCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("[FITALLY] 비밀번호찾기 인증코드");
        message.setText(
                "안녕하세요.\n\n" +
                "비밀번호 찾기 인증코드는 아래와 같습니다.\n\n" +
                code + "\n\n" +
                "인증코드 유효시간이 지나면 다시 요청해주세요."
        );
        javaMailSender.send(message);
    }
}
