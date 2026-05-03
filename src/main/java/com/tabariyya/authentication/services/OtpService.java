package com.tabariyya.authentication.services;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.function.LongSupplier;

public class OtpService {

    private final byte[] masterKey;
    private final int timeStep;
    private final int digits;
    private final LongSupplier clock;

    public OtpService(String masterKey, int timeStep, int digits) {
        this(masterKey, timeStep, digits, System::currentTimeMillis);
    }

    // Package-private — used by tests to inject a fixed clock.
    OtpService(String masterKey, int timeStep, int digits, LongSupplier clock) {
        this.masterKey = masterKey.getBytes(StandardCharsets.UTF_8);
        this.timeStep = timeStep;
        this.digits = digits;
        this.clock = clock;
    }

    public String generateCode(String email) {
        byte[] secret = deriveSecret(email.toLowerCase());
        long t = clock.getAsLong() / 1000L / timeStep;
        return computeTotp(secret, t);
    }

    public boolean verifyCode(String email, String code) {
        byte[] secret = deriveSecret(email.toLowerCase());
        long t = clock.getAsLong() / 1000L / timeStep;
        for (long window = -1; window <= 1; window++) {
            if (computeTotp(secret, t + window).equals(code)) {
                return true;
            }
        }
        return false;
    }

    // Derives a per-email TOTP secret from the server master key.
    // The same email always produces the same secret, so no storage is needed.
    private byte[] deriveSecret(String email) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(masterKey, "HmacSHA256"));
            return mac.doFinal(email.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("TOTP secret derivation failed", e);
        }
    }

    private String computeTotp(byte[] secret, long timeCounter) {
        try {
            byte[] msg = ByteBuffer.allocate(8).putLong(timeCounter).array();
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secret, "HmacSHA1"));
            byte[] hmac = mac.doFinal(msg);

            int offset = hmac[hmac.length - 1] & 0x0f;
            int binary = ((hmac[offset] & 0x7f) << 24)
                       | ((hmac[offset + 1] & 0xff) << 16)
                       | ((hmac[offset + 2] & 0xff) << 8)
                       |  (hmac[offset + 3] & 0xff);

            int otp = binary % (int) Math.pow(10, digits);
            return String.format("%0" + digits + "d", otp);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("TOTP computation failed", e);
        }
    }
}
