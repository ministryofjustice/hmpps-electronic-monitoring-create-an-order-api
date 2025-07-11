ALTER TABLE order_version
    ADD COLUMN data_dictionary_version VARCHAR(50) NULL;

UPDATE order_version
    SET data_dictionary_version = 'DDV4'
WHERE data_dictionary_version IS NULL;

ALTER TABLE order_version
    ALTER COLUMN data_dictionary_version SET NOT NULL;