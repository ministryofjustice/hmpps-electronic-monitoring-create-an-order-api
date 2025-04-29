ALTER TABLE order_version
    ADD fms_result_date TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE fms_submission_result
    ADD submission_date TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE fms_submission_result
    ALTER COLUMN submission_date SET NOT NULL;