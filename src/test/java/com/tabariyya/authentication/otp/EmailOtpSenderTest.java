package com.tabariyya.authentication.otp;

import com.tabariyya.utils.mail.MailUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailOtpSenderTest {

    @Mock
    MailUtils mailUtils;

    @Test
    void channel_returnsEmail() {
        assertEquals("email", new EmailOtpSender(mailUtils).channel());
    }

    @Test
    void send_replacesCodePlaceholderInDefaultTemplate() {
        EmailOtpSender sender = new EmailOtpSender(mailUtils);
        sender.send("alice@example.com", "482931");

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailUtils).sendEmail(any(), any(), any(), any(), bodyCaptor.capture());

        assertTrue(bodyCaptor.getValue().contains("482931"));
        assertFalse(bodyCaptor.getValue().contains("${code}"));
    }

    @Test
    void send_usesCustomTemplate() {
        EmailOtpSender sender = new EmailOtpSender(mailUtils, "Login Code", "Code: ${code} — expires soon.");
        sender.send("alice@example.com", "111222");

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailUtils).sendEmail(any(), any(), any(), any(), bodyCaptor.capture());

        assertEquals("Code: 111222 — expires soon.", bodyCaptor.getValue());
    }

    @Test
    void send_usesConfiguredSubject() {
        EmailOtpSender sender = new EmailOtpSender(mailUtils, "My Custom Subject", "Code: ${code}");
        sender.send("alice@example.com", "000000");

        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailUtils).sendEmail(any(), any(), any(), subjectCaptor.capture(), any());

        assertEquals("My Custom Subject", subjectCaptor.getValue());
    }

    @Test
    void send_deliversToCorrectRecipient() {
        EmailOtpSender sender = new EmailOtpSender(mailUtils);
        sender.send("bob@example.com", "123456");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> toCaptor = ArgumentCaptor.forClass(List.class);
        verify(mailUtils).sendEmail(toCaptor.capture(), any(), any(), any(), any());

        assertEquals(1, toCaptor.getValue().size());
        assertEquals("bob@example.com", toCaptor.getValue().get(0));
    }
}
