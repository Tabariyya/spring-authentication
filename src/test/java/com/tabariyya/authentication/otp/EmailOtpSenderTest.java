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
    void send_usesGivenSubjectAndContent() {
        new EmailOtpSender(mailUtils).send("alice@example.com", "Login Code", "Code: 111222");

        verify(mailUtils).sendEmail(any(), any(), any(), eq("Login Code"), eq("Code: 111222"));
    }

    @Test
    void send_deliversToCorrectRecipient() {
        new EmailOtpSender(mailUtils).send("bob@example.com", "Subject", "Content");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> toCaptor = ArgumentCaptor.forClass(List.class);
        verify(mailUtils).sendEmail(toCaptor.capture(), any(), any(), any(), any());

        assertEquals(1, toCaptor.getValue().size());
        assertEquals("bob@example.com", toCaptor.getValue().get(0));
    }
}
