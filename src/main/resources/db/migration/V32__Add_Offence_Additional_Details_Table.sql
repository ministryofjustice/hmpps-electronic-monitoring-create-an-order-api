CREATE TABLE offence_additional_details
(
    id UUID NOT NULL,
    version_id UUID NOT NULL,
    additional_details VARCHAR(255),
    CONSTRAINT pk_offence_additional_details PRIMARY KEY (id)
);

ALTER TABLE offence_additional_details
    ADD CONSTRAINT uc_offence_details_version UNIQUE (version_id);

ALTER TABLE offence_additional_details
    ADD CONSTRAINT  fk_offence_detail_on_version FOREIGN KEY (version_id) REFERENCES order_version (id);