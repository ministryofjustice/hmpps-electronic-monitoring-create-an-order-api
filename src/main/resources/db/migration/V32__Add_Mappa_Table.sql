CREATE TABLE mappa
(
    id         UUID NOT NULL,
    version_id UUID NOT NULL,
    level      VARCHAR(255),
    category   VARCHAR(255),
    CONSTRAINT pk_mappa PRIMARY KEY (id)
);

ALTER TABLE mappa
    ADD CONSTRAINT FK_MAPPA_ON_VERSION FOREIGN KEY (version_id) REFERENCES order_version (id);