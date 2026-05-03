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
    public void send(String recipient, String code) {
        sendSms(recipient, template.replace("${code}", code));
    }

    // Implement this with your SMS gateway (Twilio, AWS SNS, etc.)
    protected abstract void sendSms(String phoneNumber, String message);
}
