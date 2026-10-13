package dev.jason.gboardpatches.extension.webclipboard;

import org.junit.Assert;
import org.junit.Test;

public final class PairingAttemptLimiterTest {
    @Test
    public void locksOutPeerAfterFreeAttemptsWithDoublingLockouts() {
        PairingAttemptLimiter limiter = new PairingAttemptLimiter();
        long now = 1_000_000L;
        for (int attempt = 0; attempt < PairingAttemptLimiter.FREE_ATTEMPTS_PER_PEER - 1; attempt++) {
            limiter.recordFailure("10.0.0.2", now);
            Assert.assertFalse(limiter.isLockedOut("10.0.0.2", now));
        }

        limiter.recordFailure("10.0.0.2", now);
        Assert.assertTrue(limiter.isLockedOut("10.0.0.2", now));
        Assert.assertFalse(limiter.isLockedOut("10.0.0.3", now));
        Assert.assertFalse(limiter.isLockedOut(
                "10.0.0.2", now + PairingAttemptLimiter.BASE_LOCKOUT_MS));

        long later = now + PairingAttemptLimiter.GLOBAL_WINDOW_MS;
        limiter.recordFailure("10.0.0.2", later);
        Assert.assertTrue(limiter.isLockedOut(
                "10.0.0.2", later + PairingAttemptLimiter.BASE_LOCKOUT_MS));
        Assert.assertFalse(limiter.isLockedOut(
                "10.0.0.2", later + 2 * PairingAttemptLimiter.BASE_LOCKOUT_MS));
    }

    @Test
    public void successClearsPeerHistory() {
        PairingAttemptLimiter limiter = new PairingAttemptLimiter();
        for (int attempt = 0; attempt < PairingAttemptLimiter.FREE_ATTEMPTS_PER_PEER; attempt++) {
            limiter.recordFailure("10.0.0.2", 0L);
        }
        Assert.assertTrue(limiter.isLockedOut("10.0.0.2", 1L));

        limiter.recordSuccess("10.0.0.2");

        Assert.assertFalse(limiter.isLockedOut("10.0.0.2", 1L));
    }

    @Test
    public void globalLimitStopsGuessesSpreadAcrossAddresses() {
        PairingAttemptLimiter limiter = new PairingAttemptLimiter();
        for (int peer = 0; peer < PairingAttemptLimiter.GLOBAL_FAILURE_LIMIT; peer++) {
            limiter.recordFailure("10.0.1." + peer, 0L);
        }

        Assert.assertTrue(limiter.isLockedOut("10.0.2.1", 1L));
        Assert.assertFalse(limiter.isLockedOut(
                "10.0.2.1", PairingAttemptLimiter.GLOBAL_WINDOW_MS));
    }
}
