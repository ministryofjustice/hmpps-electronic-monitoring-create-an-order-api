CREATE TABLE fms_order_details
(
    case_id               VARCHAR(255)            NOT NULL,
    device_wearer_as_json          JSONB NOT NULL,
    monitoring_order_as_json          JSONB NOT NULL,
    CONSTRAINT pk_fms_order_details PRIMARY KEY (case_id)
);