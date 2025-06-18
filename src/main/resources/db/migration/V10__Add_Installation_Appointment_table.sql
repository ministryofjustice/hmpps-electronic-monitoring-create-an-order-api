CREATE TABLE installation_appointment
(
    id             UUID NOT NULL,
    version_id     UUID NOT NULL,
    place_name VARCHAR(255),
    appointment_date         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_installation_appointment PRIMARY KEY (id)
);


ALTER TABLE installation_appointment
    ADD CONSTRAINT uc_installation_appointment_version UNIQUE (version_id);

ALTER TABLE installation_appointment
    ADD CONSTRAINT FK_installation_appointment_ON_VERSION FOREIGN KEY (version_id) REFERENCES order_version (id);