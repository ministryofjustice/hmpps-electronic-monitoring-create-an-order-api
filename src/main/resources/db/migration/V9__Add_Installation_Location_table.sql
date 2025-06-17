CREATE TABLE installation_location
(
    id             UUID NOT NULL,
    version_id     UUID NOT NULL,
    location VARCHAR(255),
    CONSTRAINT pk_installation_location PRIMARY KEY (id)
);


ALTER TABLE installation_location
    ADD CONSTRAINT uc_installation_location_version UNIQUE (version_id);

ALTER TABLE installation_location
    ADD CONSTRAINT FK_installation_location_ON_VERSION FOREIGN KEY (version_id) REFERENCES order_version (id);