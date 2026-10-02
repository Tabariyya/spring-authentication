package com.tabariyya.authentication.otp;

public abstract class SmsOtpSender implements OtpSender {

    @Override
    public String channel() {
        return "sms";
    }

    @Override
    public void send(String recipient, String subject, String content) {
        sendSms(recipient, content);
    }

    // Implement this with your SMS gateway (Twilio, AWS SNS, etc.)
    protected abstract void sendSms(String phoneNumber, String message);
}
