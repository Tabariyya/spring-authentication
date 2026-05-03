package com.tabariyya.authentication.otp;

public interface OtpSender {

    String channel();

    void send(String recipient, String code);

}
