ALTER TABLE order_version
    ADD COLUMN last_updated_date TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN owner_cohort VARCHAR(255),
    ADD COLUMN last_updated_by VARCHAR(255);