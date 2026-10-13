package dev.jason.gboardpatches.extension.webclipboard;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Slows down pairing code guessing. Each address gets a few free attempts, then lockouts
 * that double up to a cap. A global cap also limits guesses spread across many addresses.
 */
final class PairingAttemptLimiter {
    static final int FREE_ATTEMPTS_PER_PEER = 5;
    static final long BASE_LOCKOUT_MS = 30_000L;
    static final long MAX_LOCKOUT_MS = 15L * 60_000L;
    static final int GLOBAL_FAILURE_LIMIT = 30;
    static final long GLOBAL_WINDOW_MS = 60_000L;
    private static final int MAX_TRACKED_PEERS = 1024;

    private final Map<String, PeerState> peers = new HashMap<>();
    private final ArrayDeque<Long> recentFailures = new ArrayDeque<>();

    synchronized boolean isLockedOut(String peer, long nowMs) {
        pruneRecentFailures(nowMs);
        if (recentFailures.size() >= GLOBAL_FAILURE_LIMIT) {
            return true;
        }
        PeerState state = peers.get(peer);
        return state != null && nowMs < state.lockedUntilMs;
    }

    synchronized void recordFailure(String peer, long nowMs) {
        recentFailures.addLast(nowMs);
        pruneRecentFailures(nowMs);
        if (!peers.containsKey(peer) && peers.size() >= MAX_TRACKED_PEERS) {
            pruneExpiredPeers(nowMs);
            if (peers.size() >= MAX_TRACKED_PEERS) {
                // The global cap still applies to untracked addresses.
                return;
            }
        }
        PeerState state = peers.get(peer);
        if (state == null) {
            state = new PeerState();
            peers.put(peer, state);
        }
        state.failures++;
        int lockoutsSoFar = state.failures - FREE_ATTEMPTS_PER_PEER;
        if (lockoutsSoFar >= 0) {
            long lockoutMs = BASE_LOCKOUT_MS << Math.min(lockoutsSoFar, 10);
            state.lockedUntilMs = nowMs + Math.min(lockoutMs, MAX_LOCKOUT_MS);
        }
    }

    synchronized void recordSuccess(String peer) {
        peers.remove(peer);
    }

    private void pruneRecentFailures(long nowMs) {
        while (!recentFailures.isEmpty()
                && nowMs - recentFailures.peekFirst() >= GLOBAL_WINDOW_MS) {
            recentFailures.removeFirst();
        }
    }

    private void pruneExpiredPeers(long nowMs) {
        Iterator<PeerState> iterator = peers.values().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().lockedUntilMs <= nowMs) {
                iterator.remove();
            }
        }
    }

    private static final class PeerState {
        int failures;
        long lockedUntilMs;
    }
}
