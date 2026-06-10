package com.tabariyya.authentication.services;

import com.tabariyya.authentication.otp.OtpPurpose;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OtpServiceTest {

    private static final String MASTER_KEY = "test-secret-key";
    private static final int TIME_STEP = 300; // 5-minute window
    private static final int DIGITS = 6;

    // Fixed epoch second whose window counter is 1_000_000 (clean number, no edge).
    // T = floor(300_000_000_000 ms / 1000 / 300) = 1_000_000
    private static final long FIXED_MS = 300_000_000_000L;

    private OtpService service(long fixedMillis) {
        return new OtpService(MASTER_KEY, TIME_STEP, DIGITS, () -> fixedMillis);
    }

    // ── happy path ──────────────────────────────────────────────────────────────

    @Test
    void sameEmailPurposeAndCode_accepted() {
        OtpService svc = service(FIXED_MS);
        String code = svc.generateCode("alice@example.com", OtpPurpose.VERIFY_ACCOUNT);
        assertTrue(svc.verifyCode("alice@example.com", OtpPurpose.VERIFY_ACCOUNT, code));
    }

    @Test
    void emailIsCaseInsensitive() {
        OtpService svc = service(FIXED_MS);
        String code = svc.generateCode("Alice@Example.COM", OtpPurpose.VERIFY_ACCOUNT);
        assertTrue(svc.verifyCode("alice@example.com", OtpPurpose.VERIFY_ACCOUNT, code));
    }

    @Test
    void differentPurposesGenerateDifferentCodes() {
        OtpService svc = service(FIXED_MS);

        String verifyAccountCode = svc.generateCode("alice@example.com", OtpPurpose.VERIFY_ACCOUNT);
        String forgotPasswordCode = svc.generateCode("alice@example.com", OtpPurpose.FORGOT_PASSWORD);

        assertNotEquals(verifyAccountCode, forgotPasswordCode);
    }

    @Test
    void codeFromDifferentPurpose_rejected() {
        OtpService svc = service(FIXED_MS);

        String verifyAccountCode = svc.generateCode("alice@example.com", OtpPurpose.VERIFY_ACCOUNT);

        assertFalse(svc.verifyCode("alice@example.com", OtpPurpose.FORGOT_PASSWORD, verifyAccountCode));
    }

    @Test
    void codeIsAlwaysSixDigits() {
        // Run over many windows to catch codes that would be < 100_000 without zero-padding.
        for (int i = 0; i < 200; i++) {
            long ms = FIXED_MS + (long) i * TIME_STEP * 1000L;
            String code = service(ms).generateCode("test@example.com", OtpPurpose.VERIFY_ACCOUNT);
            assertEquals(DIGITS, code.length(), "Code length must be exactly 6 digits");
            assertTrue(code.matches("\\d{6}"), "Code must contain only digits");
        }
    }

    // ── window tolerance ────────────────────────────────────────────────────────

    @Test
    void previousWindowCode_accepted() {
        // Code generated one window ago should still verify (email delivery tolerance).
        OtpService past = service(FIXED_MS - TIME_STEP * 1000L);
        OtpService present = service(FIXED_MS);

        String oldCode = past.generateCode("alice@example.com", OtpPurpose.VERIFY_ACCOUNT);

        assertTrue(present.verifyCode("alice@example.com", OtpPurpose.VERIFY_ACCOUNT, oldCode));
    }

    @Test
    void nextWindowCode_accepted() {
        // Code generated one window ahead still verifies (clock-skew tolerance).
        OtpService future = service(FIXED_MS + TIME_STEP * 1000L);
        OtpService present = service(FIXED_MS);

        String futureCode = future.generateCode("alice@example.com", OtpPurpose.VERIFY_ACCOUNT);

        assertTrue(present.verifyCode("alice@example.com", OtpPurpose.VERIFY_ACCOUNT, futureCode));
    }

    @Test
    void expiredCode_twoWindowsOld_rejected() {
        OtpService past = service(FIXED_MS - 2L * TIME_STEP * 1000L);
        OtpService present = service(FIXED_MS);

        String expiredCode = past.generateCode("alice@example.com", OtpPurpose.VERIFY_ACCOUNT);

        assertFalse(present.verifyCode("alice@example.com", OtpPurpose.VERIFY_ACCOUNT, expiredCode));
    }

    @Test
    void codeExpires_afterToleranceWindow() {
        // User receives the code at T=0 but waits too long before submitting.
        // Within tolerance (T + 1 window): still valid.
        // Beyond tolerance (T + 2 windows): rejected.
        OtpService atGeneration = service(FIXED_MS);
        OtpService withinTolerance = service(FIXED_MS + TIME_STEP * 1000L);
        OtpService beyondTolerance = service(FIXED_MS + 2L * TIME_STEP * 1000L);

        String code = atGeneration.generateCode("alice@example.com", OtpPurpose.VERIFY_ACCOUNT);

        assertTrue(withinTolerance.verifyCode("alice@example.com", OtpPurpose.VERIFY_ACCOUNT, code),
                "should still be valid within tolerance");
        assertFalse(beyondTolerance.verifyCode("alice@example.com", OtpPurpose.VERIFY_ACCOUNT, code),
                "should be rejected after tolerance window");
    }

    // ── rejection cases ─────────────────────────────────────────────────────────

    @Test
    void wrongCode_rejected() {
        OtpService svc = service(FIXED_MS);

        String code = svc.generateCode("alice@example.com", OtpPurpose.VERIFY_ACCOUNT);
        String wrong = code.equals("000000") ? "000001" : "000000";

        assertFalse(svc.verifyCode("alice@example.com", OtpPurpose.VERIFY_ACCOUNT, wrong));
    }

    @Test
    void codeFromDifferentEmail_rejected() {
        OtpService svc = service(FIXED_MS);

        String codeForAlice = svc.generateCode("alice@example.com", OtpPurpose.VERIFY_ACCOUNT);

        assertFalse(svc.verifyCode("bob@example.com", OtpPurpose.VERIFY_ACCOUNT, codeForAlice));
    }

    @Test
    void codeFromDifferentMasterKey_rejected() {
        OtpService svcA = new OtpService("key-a", TIME_STEP, DIGITS, () -> FIXED_MS);
        OtpService svcB = new OtpService("key-b", TIME_STEP, DIGITS, () -> FIXED_MS);

        String code = svcA.generateCode("alice@example.com", OtpPurpose.VERIFY_ACCOUNT);

        assertFalse(svcB.verifyCode("alice@example.com", OtpPurpose.VERIFY_ACCOUNT, code));
    }

    @Test
    void emptyCode_rejected() {
        OtpService svc = service(FIXED_MS);

        assertFalse(svc.verifyCode("alice@example.com", OtpPurpose.VERIFY_ACCOUNT, ""));
    }

    @Test
    void nullCode_rejected() {
        OtpService svc = service(FIXED_MS);

        assertFalse(svc.verifyCode("alice@example.com", OtpPurpose.VERIFY_ACCOUNT, null));
    }
}