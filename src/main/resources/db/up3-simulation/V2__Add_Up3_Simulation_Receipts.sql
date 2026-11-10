CREATE TABLE up3_simulation_return_event
(
    event_id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    target_order_version_id UUID NOT NULL,
    simulated_submission_version_id UUID NOT NULL,
    case_id VARCHAR(255) NOT NULL,
    payload_hash VARCHAR(64) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(50) NOT NULL,
    queued_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    publication_attempts INTEGER NOT NULL DEFAULT 0,
    processing_failures INTEGER NOT NULL DEFAULT 0,
    next_publication_attempt_at TIMESTAMP WITH TIME ZONE,
    failure_stage VARCHAR(50),
    error_code VARCHAR(255)
);

CREATE INDEX up3_simulation_return_event_pending_idx
    ON up3_simulation_return_event (status, next_publication_attempt_at, created_at);
CREATE INDEX up3_simulation_return_event_order_idx ON up3_simulation_return_event (order_id);

-- Keep creation identities after a test draft is deleted; retries must never recreate that order.
CREATE TABLE up3_simulation_creation
(
    creation_request_id UUID PRIMARY KEY,
    payload_hash VARCHAR(64) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    requested_state VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    order_id UUID UNIQUE,
    created_order_version_id UUID,
    return_event_id UUID
);
