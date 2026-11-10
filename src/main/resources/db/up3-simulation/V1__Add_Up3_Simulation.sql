CREATE TABLE up3_simulation
(
    order_id UUID PRIMARY KEY REFERENCES orders(id) ON DELETE CASCADE,
    version_id UUID NOT NULL,
    case_id VARCHAR(255) NOT NULL UNIQUE,
    case_state VARCHAR(50) NOT NULL,
    enrolled_by VARCHAR(255) NOT NULL,
    enrolled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_processed_event_id UUID,
    captured_user_emails INTEGER NOT NULL DEFAULT 0,
    captured_no_emails INTEGER NOT NULL DEFAULT 0
);
