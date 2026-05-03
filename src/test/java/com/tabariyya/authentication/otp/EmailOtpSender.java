package com.tabariyya.authentication.otp;

import com.tabariyya.utils.mail.MailUtils;

import java.util.ArrayList;
import java.util.Collections;

public class EmailOtpSender implements OtpSender {

    // Default plain-text template. Pass an HTML string to the constructor for rich emails.
    public static final String DEFAULT_TEMPLATE =
            "Hello,\n" +
            "\n" +
            "Your verification code is:\n" +
            "${code}\n" +
            "\n" +
            "This code expires in a few minutes.\n" +
            "\n" +
            "Regards\n";

    private final MailUtils mailUtils;
    private final String subject;
    private final String template;

    public EmailOtpSender(MailUtils mailUtils, String subject, String template) {
        this.mailUtils = mailUtils;
        this.subject = subject;
        this.template = template;
    }

    public EmailOtpSender(MailUtils mailUtils) {
        this(mailUtils, "Verification Code", DEFAULT_TEMPLATE);
    }

    @Override
    public String channel() {
        return "email";
    }

    @Override
    public void send(String recipient, String code) {
        String body = template.replace("${code}", code);
        mailUtils.sendEmail(
                Collections.singletonList(recipient),
                new ArrayList<String>(),
                new ArrayList<String>(),
                subject,
                body
        );
    }
}
