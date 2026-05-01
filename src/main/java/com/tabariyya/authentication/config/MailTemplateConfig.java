package com.tabariyya.authentication.config;

public final class MailTemplateConfig {

    public static final String FORGOT_PASSWORD =
            "Hello,\n" +
            "\n" +
            "Use this code to reset your password:\n" +
            "${token}\n" +
            "\n" +
            "Regards\n";

    public static final String INVITATION =
            "Hello,\n" +
            "\n" +
            "Please register using the following link:\n" +
            "${token}\n" +
            "\n" +
            "Thank you!\n";

    public static String getForgotPassword() {
        return FORGOT_PASSWORD;
    }

    public static String getInvitation() {
        return INVITATION;
    }

}
