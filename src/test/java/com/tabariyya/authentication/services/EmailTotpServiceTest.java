package com.tabariyya.authentication.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailTotpServiceTest {

    private static final String MASTER_KEY = "test-secret-key";
    private static final int TIME_STEP   = 300; // 5-minute window
    private static final int DIGITS      = 6;

    // Fixed epoch second whose window counter is 1_000_000 (clean number, no edge).
    // T = floor(300_000_000_000 ms / 1000 / 300) = 1_000_000
    private static final long FIXED_MS = 300_000_000_000L;

    private EmailTotpService service(long fixedMillis) {
        return new EmailTotpService(MASTER_KEY, TIME_STEP, DIGITS, () -> fixedMillis);
    }

    // ── happy path ──────────────────────────────────────────────────────────────

    @Test
    void sameEmailAndCode_accepted() {
        EmailTotpService svc = service(FIXED_MS);
        String code = svc.generateCode("alice@example.com");
        assertTrue(svc.verifyCode("alice@example.com", code));
    }

    @Test
    void emailIsCaseInsensitive() {
        EmailTotpService svc = service(FIXED_MS);
        String code = svc.generateCode("Alice@Example.COM");
        assertTrue(svc.verifyCode("alice@example.com", code));
    }

    @Test
    void codeIsAlwaysSixDigits() {
        // Run over many windows to catch codes that would be < 100_000 without zero-padding.
        for (int i = 0; i < 200; i++) {
            long ms = FIXED_MS + (long) i * TIME_STEP * 1000L;
            String code = service(ms).generateCode("test@example.com");
            assertEquals(DIGITS, code.length(), "Code length must be exactly 6 digits");
            assertTrue(code.matches("\\d{6}"), "Code must contain only digits");
        }
    }

    // ── window tolerance ────────────────────────────────────────────────────────

    @Test
    void previousWindowCode_accepted() {
        // Code generated one window ago should still verify (email delivery tolerance).
        EmailTotpService past    = service(FIXED_MS - TIME_STEP * 1000L);
        EmailTotpService present = service(FIXED_MS);
        String oldCode = past.generateCode("alice@example.com");
        assertTrue(present.verifyCode("alice@example.com", oldCode));
    }

    @Test
    void nextWindowCode_accepted() {
        // Code generated one window ahead still verifies (clock-skew tolerance).
        EmailTotpService future  = service(FIXED_MS + TIME_STEP * 1000L);
        EmailTotpService present = service(FIXED_MS);
        String futureCode = future.generateCode("alice@example.com");
        assertTrue(present.verifyCode("alice@example.com", futureCode));
    }

    @Test
    void expiredCode_twoWindowsOld_rejected() {
        EmailTotpService past    = service(FIXED_MS - 2L * TIME_STEP * 1000L);
        EmailTotpService present = service(FIXED_MS);
        String expiredCode = past.generateCode("alice@example.com");
        assertFalse(present.verifyCode("alice@example.com", expiredCode));
    }

    @Test
    void codeExpires_afterToleranceWindow() {
        // User receives the code at T=0 but waits too long before submitting.
        // Within tolerance (T + 1 window): still valid.
        // Beyond tolerance (T + 2 windows): rejected.
        EmailTotpService atGeneration          = service(FIXED_MS);
        EmailTotpService withinTolerance       = service(FIXED_MS + TIME_STEP * 1000L);
        EmailTotpService beyondTolerance       = service(FIXED_MS + 2L * TIME_STEP * 1000L);

        String code = atGeneration.generateCode("alice@example.com");

        assertTrue(withinTolerance.verifyCode("alice@example.com", code),  "should still be valid within tolerance");
        assertFalse(beyondTolerance.verifyCode("alice@example.com", code), "should be rejected after tolerance window");
    }

    // ── rejection cases ─────────────────────────────────────────────────────────

    @Test
    void wrongCode_rejected() {
        EmailTotpService svc  = service(FIXED_MS);
        String code = svc.generateCode("alice@example.com");
        String wrong = code.equals("000000") ? "000001" : "000000";
        assertFalse(svc.verifyCode("alice@example.com", wrong));
    }

    @Test
    void codeFromDifferentEmail_rejected() {
        EmailTotpService svc = service(FIXED_MS);
        String codeForAlice = svc.generateCode("alice@example.com");
        assertFalse(svc.verifyCode("bob@example.com", codeForAlice));
    }

    @Test
    void codeFromDifferentMasterKey_rejected() {
        EmailTotpService svcA = new EmailTotpService("key-a", TIME_STEP, DIGITS, () -> FIXED_MS);
        EmailTotpService svcB = new EmailTotpService("key-b", TIME_STEP, DIGITS, () -> FIXED_MS);
        String code = svcA.generateCode("alice@example.com");
        assertFalse(svcB.verifyCode("alice@example.com", code));
    }

    @Test
    void emptyCode_rejected() {
        EmailTotpService svc = service(FIXED_MS);
        assertFalse(svc.verifyCode("alice@example.com", ""));
    }

    @Test
    void nullCode_rejected() {
        EmailTotpService svc = service(FIXED_MS);
        assertFalse(svc.verifyCode("alice@example.com", null));
    }
}
