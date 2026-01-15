CREATE TABLE dapo
(
    id         UUID NOT NULL,
    version_id UUID NOT NULL,
    clause     VARCHAR(20),
    date       TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_dapo PRIMARY KEY (id)
);

CREATE TABLE offence
(
    id           UUID NOT NULL,
    version_id   UUID NOT NULL,
    offence_type VARCHAR(255),
    offence_date TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_offence PRIMARY KEY (id)
);

ALTER TABLE dapo
    ADD CONSTRAINT FK_DAPO_ON_VERSION FOREIGN KEY (version_id) REFERENCES order_version (id);

ALTER TABLE offence
    ADD CONSTRAINT FK_OFFENCE_ON_VERSION FOREIGN KEY (version_id) REFERENCES order_version (id);