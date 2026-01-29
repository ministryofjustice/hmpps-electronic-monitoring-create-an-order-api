CREATE TABLE details_of_installation
(
    id            UUID NOT NULL,
    version_id    UUID NOT NULL,
    risk_category VARCHAR(255),
    risk_details  VARCHAR(255),
    CONSTRAINT pk_details_of_installation PRIMARY KEY (id)
);

ALTER TABLE details_of_installation
    ADD CONSTRAINT uc_details_of_installation_version UNIQUE (version_id);

ALTER TABLE details_of_installation
    ADD CONSTRAINT FK_DETAILS_OF_INSTALLATION_ON_VERSION FOREIGN KEY (version_id) REFERENCES order_version (id);