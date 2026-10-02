package com.tabariyya.authentication.otp;

public interface OtpContentProvider {

    String verifyAccountSubject(String channel);

    String verifyAccountContent(String channel, String code);

    String forgotPasswordSubject(String channel);

    String forgotPasswordContent(String channel, String code);

}
