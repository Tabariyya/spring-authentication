package com.tabariyya.authentication.otp;

public abstract class SmsOtpSender implements OtpSender {

    // SMS messages must be short — no HTML, no multi-line formatting.
    public static final String DEFAULT_TEMPLATE = "Your verification code is: ${code}";

    private final String template;

    protected SmsOtpSender(String template) {
        this.template = template;
    }

    protected SmsOtpSender() {
        this(DEFAULT_TEMPLATE);
    }

    @Override
    public String channel() {
        return "sms";
    }

    @Override
    public String verifyAccountSubject() {
        return null;
    }

    @Override
    public String verifyAccountContent(String code) {
        return template.replace("${code}", code);
    }

    @Override
    public String forgotPasswordSubject() {
        return null;
    }

    @Override
    public String forgotPasswordContent(String code) {
        return template.replace("${code}", code);
    }

    @Override
    public void send(String recipient, String subject, String content) {
        sendSms(recipient, content);
    }

    // Implement this with your SMS gateway (Twilio, AWS SNS, etc.)
    protected abstract void sendSms(String phoneNumber, String message);
}
