package com.tabariyya.authentication.otp;

import com.tabariyya.authentication.otp.SmsOtpSender;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SmsOtpSenderTest {

    // Concrete test double that captures outgoing SMS calls.
    static class CapturingSmsOtpSender extends SmsOtpSender {
        final List<String[]> calls = new ArrayList<>();

        CapturingSmsOtpSender() { super(); }
        CapturingSmsOtpSender(String template) { super(template); }

        @Override
        protected void sendSms(String phoneNumber, String message) {
            calls.add(new String[]{phoneNumber, message});
        }

        String lastPhone()   { return calls.get(calls.size() - 1)[0]; }
        String lastMessage() { return calls.get(calls.size() - 1)[1]; }
    }

    @Test
    void channel_returnsSms() {
        assertEquals("sms", new CapturingSmsOtpSender().channel());
    }

    @Test
    void send_defaultTemplate_containsCode() {
        CapturingSmsOtpSender sender = new CapturingSmsOtpSender();
        sender.send("+9725xxxxxxx", "382910");

        assertTrue(sender.lastMessage().contains("382910"));
        assertFalse(sender.lastMessage().contains("${code}"));
    }

    @Test
    void send_customTemplate_replacesPlaceholder() {
        CapturingSmsOtpSender sender = new CapturingSmsOtpSender("OTP: ${code}");
        sender.send("+9725xxxxxxx", "000001");

        assertEquals("OTP: 000001", sender.lastMessage());
    }

    @Test
    void send_deliversToCorrectNumber() {
        CapturingSmsOtpSender sender = new CapturingSmsOtpSender();
        sender.send("+9721111111", "999999");

        assertEquals("+9721111111", sender.lastPhone());
    }

    @Test
    void send_multipleCalls_eachDeliveredIndependently() {
        CapturingSmsOtpSender sender = new CapturingSmsOtpSender("Code: ${code}");
        sender.send("+111", "111111");
        sender.send("+222", "222222");

        assertEquals(2, sender.calls.size());
        assertEquals("Code: 111111", sender.calls.get(0)[1]);
        assertEquals("Code: 222222", sender.calls.get(1)[1]);
    }
}
