package com.sinch.interview.trust.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "block_audit")
public class BlockAudit {

    @Id
    private UUID id;

    @Column(name = "project_id", length = 64, nullable = false)
    private String projectId;

    @Enumerated(EnumType.STRING)
    @Column(length = 32, nullable = false)
    private AuditAction action;

    @Column(length = 128, nullable = false)
    private String actor;

    @Column(length = 512, nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_trust_level", length = 32, nullable = false)
    private TrustLevel previousTrustLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_trust_level", length = 32, nullable = false)
    private TrustLevel newTrustLevel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected BlockAudit() {
    }

    public BlockAudit(
            String projectId,
            AuditAction action,
            String actor,
            String reason,
            TrustLevel previousTrustLevel,
            TrustLevel newTrustLevel
    ) {
        this.id = UUID.randomUUID();
        this.projectId = projectId;
        this.action = action;
        this.actor = actor;
        this.reason = reason;
        this.previousTrustLevel = previousTrustLevel;
        this.newTrustLevel = newTrustLevel;
    }

    @PrePersist
    void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public String getProjectId() {
        return projectId;
    }

    public AuditAction getAction() {
        return action;
    }

    public String getActor() {
        return actor;
    }

    public String getReason() {
        return reason;
    }

    public TrustLevel getPreviousTrustLevel() {
        return previousTrustLevel;
    }

    public TrustLevel getNewTrustLevel() {
        return newTrustLevel;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
