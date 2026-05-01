package com.tabariyya.authentication.config;

public final class MailTemplateConfig {

    public static final String FORGOT_PASSWORD = """
            Hello,

            Use this code to reset your password:
            ${token}

            Regards
            """;

    public static final String INVITATION = """
            Hello,

            Please register using the following link:
            ${token}

            Thank you!
            """;

    public static String getForgotPassword() {
        return FORGOT_PASSWORD;
    }

    public static String getInvitation() {
        return INVITATION;
    }

}
