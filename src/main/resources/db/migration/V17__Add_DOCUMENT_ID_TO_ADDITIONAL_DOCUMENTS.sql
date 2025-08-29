ALTER TABLE additional_documents
    ADD COLUMN document_id UUID NULL;

UPDATE additional_documents
    SET document_id = id
WHERE document_id IS NULL;

ALTER TABLE additional_documents
    ALTER COLUMN document_id SET NOT NULL;