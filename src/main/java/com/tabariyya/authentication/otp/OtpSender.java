package com.tabariyya.authentication.otp;

public interface OtpSender {

    String channel();

    String verifyAccountSubject();

    String verifyAccountContent(String code);

    String forgotPasswordSubject();

    String forgotPasswordContent(String code);

    void send(String recipient, String subject, String content);

}
