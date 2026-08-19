CREATE TABLE fms_order_details
(
    case_id               VARCHAR(255)            NOT NULL,
    device_wearer_as_json          VARCHAR(409600) NOT NULL,
    monitoring_order_as_json          VARCHAR(409600) NOT NULL,
    CONSTRAINT pk_fms_order_details PRIMARY KEY (id)
);