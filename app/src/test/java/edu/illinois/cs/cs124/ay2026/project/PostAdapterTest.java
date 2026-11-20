package edu.illinois.cs.cs124.ay2026.project;

import org.junit.Test;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertEquals;

public class PostAdapterTest {

    // ── formatTimeRemaining ───────────────────────────────────────────────────

    /** Helper: compute createdAtMs given how many milliseconds remain until expiry. */
    private static long createdAtForRemaining(long remainingMs) {
        long nowMs = 0; // fixed reference point
        long expiresAtMs = nowMs + remainingMs;
        return expiresAtMs - TimeUnit.DAYS.toMillis(7);
    }

    @Test
    public void formatTimeRemaining_freshPost_showsDaysLeft() {
        // A post created right now has 7 days remaining.
        long nowMs = System.currentTimeMillis();
        String result = PostAdapter.PostViewHolder.formatTimeRemaining(nowMs, nowMs);
        assertEquals("7d left", result);
    }

    @Test
    public void formatTimeRemaining_threeDaysRemaining_showsDays() {
        long remaining = TimeUnit.DAYS.toMillis(3);
        long nowMs = 0;
        long createdAtMs = createdAtForRemaining(remaining);
        assertEquals("3d left", PostAdapter.PostViewHolder.formatTimeRemaining(createdAtMs, nowMs));
    }

    @Test
    public void formatTimeRemaining_exactlyFortyEightHoursRemaining_showsDays() {
        // 48 h is the boundary: >= 48 h → days branch.
        long remaining = TimeUnit.HOURS.toMillis(48);
        long nowMs = 0;
        long createdAtMs = createdAtForRemaining(remaining);
        assertEquals("2d left", PostAdapter.PostViewHolder.formatTimeRemaining(createdAtMs, nowMs));
    }

    @Test
    public void formatTimeRemaining_justUnderFortyEightHours_showsHours() {
        // 47 h 59 m → hoursRemaining = 47, so hours branch.
        long remaining = TimeUnit.HOURS.toMillis(47) + TimeUnit.MINUTES.toMillis(59);
        long nowMs = 0;
        long createdAtMs = createdAtForRemaining(remaining);
        assertEquals("47h left", PostAdapter.PostViewHolder.formatTimeRemaining(createdAtMs, nowMs));
    }

    @Test
    public void formatTimeRemaining_oneHourRemaining_showsHours() {
        long remaining = TimeUnit.HOURS.toMillis(1);
        long nowMs = 0;
        long createdAtMs = createdAtForRemaining(remaining);
        assertEquals("1h left", PostAdapter.PostViewHolder.formatTimeRemaining(createdAtMs, nowMs));
    }

    @Test
    public void formatTimeRemaining_thirtyMinutesRemaining_showsLessThanOneHour() {
        long remaining = TimeUnit.MINUTES.toMillis(30);
        long nowMs = 0;
        long createdAtMs = createdAtForRemaining(remaining);
        assertEquals("< 1h left", PostAdapter.PostViewHolder.formatTimeRemaining(createdAtMs, nowMs));
    }

    @Test
    public void formatTimeRemaining_expired_showsLessThanOneHour() {
        // Posts should be filtered out by Firestore before reaching the adapter,
        // but the formatter should degrade gracefully rather than crash.
        long remaining = -TimeUnit.HOURS.toMillis(1); // already expired
        long nowMs = 0;
        long createdAtMs = createdAtForRemaining(remaining);
        assertEquals("< 1h left", PostAdapter.PostViewHolder.formatTimeRemaining(createdAtMs, nowMs));
    }

    // ── formatPartnerCount ────────────────────────────────────────────────────

    @Test
    public void formatPartnerCount_one_isSingular() {
        assertEquals("Looking for 1 partner", PostAdapter.PostViewHolder.formatPartnerCount(1));
    }

    @Test
    public void formatPartnerCount_two_isPlural() {
        assertEquals("Looking for 2 partners", PostAdapter.PostViewHolder.formatPartnerCount(2));
    }

    @Test
    public void formatPartnerCount_zero_isPlural() {
        // Edge case: zero partners uses the plural branch.
        assertEquals("Looking for 0 partners", PostAdapter.PostViewHolder.formatPartnerCount(0));
    }

    @Test
    public void formatPartnerCount_large_isPlural() {
        assertEquals("Looking for 10 partners", PostAdapter.PostViewHolder.formatPartnerCount(10));
    }
}