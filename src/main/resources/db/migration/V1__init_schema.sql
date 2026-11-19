CREATE TABLE projects (
    id              VARCHAR(64) PRIMARY KEY,
    name            VARCHAR(128) NOT NULL,
    account_type    VARCHAR(32)  NOT NULL,
    trust_level     VARCHAR(32)  NOT NULL,
    blocked         BOOLEAN      NOT NULL DEFAULT FALSE,
    last_event_at   TIMESTAMPTZ,
    version         BIGINT       NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE block_audit (
    id                    UUID PRIMARY KEY,
    project_id            VARCHAR(64)  NOT NULL REFERENCES projects(id),
    action                VARCHAR(32)  NOT NULL,
    actor                 VARCHAR(128) NOT NULL,
    reason                VARCHAR(512) NOT NULL,
    previous_trust_level  VARCHAR(32)  NOT NULL,
    new_trust_level       VARCHAR(32)  NOT NULL,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_block_audit_project_created ON block_audit (project_id, created_at DESC);

CREATE TABLE outbox_events (
    id               UUID PRIMARY KEY,
    aggregate_id     VARCHAR(64)  NOT NULL,
    target           VARCHAR(64)  NOT NULL,
    event_type       VARCHAR(64)  NOT NULL,
    payload          TEXT         NOT NULL,
    status           VARCHAR(32)  NOT NULL DEFAULT 'PENDING',
    attempts         INT          NOT NULL DEFAULT 0,
    next_attempt_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    last_error       VARCHAR(512),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_outbox_events_dispatch ON outbox_events (status, next_attempt_at);
