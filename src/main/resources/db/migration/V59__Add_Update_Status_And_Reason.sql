CREATE TABLE status_update
(
    id                         UUID                        NOT NULL,
    version_id                 UUID                        NOT NULL,
    status                     VARCHAR(255)                NOT NULL,
    date_time_of_status_change TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_status_update PRIMARY KEY (id)
);

CREATE TABLE status_update_reason
(
    id               UUID         NOT NULL,
    status_update_id UUID         NOT NULL,
    section          VARCHAR(255) NOT NULL,
    details          VARCHAR(255),
    CONSTRAINT pk_status_update_reason PRIMARY KEY (id)
);

ALTER TABLE status_update
    ADD CONSTRAINT FK_STATUS_UPDATE_ON_VERSION FOREIGN KEY (version_id) REFERENCES order_version (id);

ALTER TABLE status_update_reason
    ADD CONSTRAINT FK_STATUS_UPDATE_REASON_ON_STATUS_UPDATE FOREIGN KEY (status_update_id) REFERENCES status_update (id);