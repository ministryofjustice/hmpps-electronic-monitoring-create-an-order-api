CREATE TABLE order_parameters
(
    id         UUID NOT NULL,
    version_id UUID NOT NULL,
    have_photo BOOLEAN,
    CONSTRAINT pk_order_parameters PRIMARY KEY (id)
);