package com.sinch.interview.trust.events;

import java.time.Instant;

public record BillingStatusEvent(
        String projectId,
        String accountStatus,
        Instant eventTimestamp
) {
}
