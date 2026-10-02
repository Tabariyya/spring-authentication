package com.tabariyya.authentication.otp;

import com.tabariyya.utils.mail.MailUtils;

import java.util.ArrayList;
import java.util.Collections;

public class EmailOtpSender implements OtpSender {

    private final MailUtils mailUtils;

    public EmailOtpSender(MailUtils mailUtils) {
        this.mailUtils = mailUtils;
    }

    @Override
    public String channel() {
        return "email";
    }

    @Override
    public void send(String recipient, String subject, String content) {
        mailUtils.sendEmail(
                Collections.singletonList(recipient),
                new ArrayList<String>(),
                new ArrayList<String>(),
                subject,
                content
        );
    }
}
