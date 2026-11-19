package com.sinch.interview.trust.api;

import com.sinch.interview.trust.domain.AccountType;
import com.sinch.interview.trust.domain.Project;
import com.sinch.interview.trust.domain.TrustLevel;

import java.time.Instant;

public record ProjectResponse(
        String id,
        String name,
        AccountType accountType,
        TrustLevel trustLevel,
        boolean blocked,
        Instant lastEventAt,
        long version,
        Instant updatedAt
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getAccountType(),
                project.getTrustLevel(),
                project.isBlocked(),
                project.getLastEventAt(),
                project.getVersion(),
                project.getUpdatedAt()
        );
    }
}
