CREATE TABLE probation_delivery_unit
(
    id             UUID NOT NULL,
    version_id     UUID NOT NULL,
    unit VARCHAR(255),
    CONSTRAINT pk_probation_delivery_unit PRIMARY KEY (id)
);


ALTER TABLE probation_delivery_unit
    ADD CONSTRAINT uc_probation_delivery_unit_version UNIQUE (version_id);

ALTER TABLE probation_delivery_unit
    ADD CONSTRAINT FK_PROBATION_DELIVERY_UNIT_ON_VERSION FOREIGN KEY (version_id) REFERENCES order_version (id);