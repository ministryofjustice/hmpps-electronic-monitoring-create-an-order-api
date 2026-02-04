ALTER TABLE mappa
    DROP COLUMN is_mappa;

ALTER TABLE mappa
    ADD is_mappa VARCHAR(255);