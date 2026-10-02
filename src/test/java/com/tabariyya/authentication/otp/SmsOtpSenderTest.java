package com.tabariyya.authentication.otp;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SmsOtpSenderTest {

    // Concrete test double that captures outgoing SMS calls.
    static class CapturingSmsOtpSender extends SmsOtpSender {
        final List<String[]> calls = new ArrayList<>();

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
    void send_deliversContentAsMessage() {
        CapturingSmsOtpSender sender = new CapturingSmsOtpSender();
        sender.send("+9725xxxxxxx", null, "Code: 382910");

        assertEquals("Code: 382910", sender.lastMessage());
    }

    @Test
    void send_deliversToCorrectNumber() {
        CapturingSmsOtpSender sender = new CapturingSmsOtpSender();
        sender.send("+9721111111", null, "Code: 999999");

        assertEquals("+9721111111", sender.lastPhone());
    }

    @Test
    void send_multipleCalls_eachDeliveredIndependently() {
        CapturingSmsOtpSender sender = new CapturingSmsOtpSender();
        sender.send("+111", null, "Code: 111111");
        sender.send("+222", null, "Code: 222222");

        assertEquals(2, sender.calls.size());
        assertEquals("Code: 111111", sender.calls.get(0)[1]);
        assertEquals("Code: 222222", sender.calls.get(1)[1]);
    }
}
