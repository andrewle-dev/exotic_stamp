-- Mail queue module
CREATE TABLE mail_jobs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient       VARCHAR(255) NOT NULL,
    subject         VARCHAR(255) NOT NULL,
    body            TEXT NOT NULL,
    content_type    VARCHAR(10) NOT NULL DEFAULT 'HTML',
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count     INT NOT NULL DEFAULT 0,
    max_retries     INT NOT NULL DEFAULT 3,
    next_retry_at   TIMESTAMP,
    last_error      TEXT,
    scheduled_at    TIMESTAMP NOT NULL,
    processed_at    TIMESTAMP,
    dedup_key       VARCHAR(255) UNIQUE,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL
);

CREATE INDEX idx_mail_jobs_status_next_retry
    ON mail_jobs (status, next_retry_at)
    WHERE status IN ('PENDING', 'PROCESSING');
