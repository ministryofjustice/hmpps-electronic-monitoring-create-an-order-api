ALTER TABLE status_update
    ADD COLUMN status_updated_by VARCHAR(255);

-- Status updates now belong to the order rather than to a single order version.
ALTER TABLE status_update
    ADD COLUMN order_id UUID;

UPDATE status_update
SET order_id = (SELECT ov.order_id FROM order_version ov WHERE ov.id = status_update.version_id);

ALTER TABLE status_update
    ALTER COLUMN order_id SET NOT NULL;

ALTER TABLE status_update
    DROP CONSTRAINT FK_STATUS_UPDATE_ON_VERSION;

ALTER TABLE status_update
    DROP COLUMN version_id;

-- Status updates are no longer owned by Order in JPA, so the database cascades deletes instead.
ALTER TABLE status_update
    ADD CONSTRAINT FK_STATUS_UPDATE_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE;

ALTER TABLE status_update_reason
    DROP CONSTRAINT FK_STATUS_UPDATE_REASON_ON_STATUS_UPDATE;

ALTER TABLE status_update_reason
    ADD CONSTRAINT FK_STATUS_UPDATE_REASON_ON_STATUS_UPDATE FOREIGN KEY (status_update_id) REFERENCES status_update (id) ON DELETE CASCADE;

CREATE INDEX idx_status_update_order_id ON status_update (order_id);
