CREATE TABLE order_parameters
(
    id         UUID NOT NULL,
    version_id UUID NOT NULL,
    have_photo BOOLEAN,
    CONSTRAINT pk_order_parameters PRIMARY KEY (id)
);

ALTER TABLE order_parameters
    ADD CONSTRAINT uc_order_parameters_version UNIQUE (version_id);

ALTER TABLE order_parameters
    ADD CONSTRAINT FK_ORDER_PARAMETERS_ON_VERSION FOREIGN KEY (version_id) REFERENCES order_version (id);