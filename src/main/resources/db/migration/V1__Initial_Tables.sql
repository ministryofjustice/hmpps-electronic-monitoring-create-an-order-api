CREATE TABLE additional_documents
(
    id         UUID NOT NULL,
    version_id UUID NOT NULL,
    file_name  VARCHAR(255),
    file_type  VARCHAR(255),
    CONSTRAINT pk_additional_documents PRIMARY KEY (id)
);

CREATE TABLE address
(
    id             UUID         NOT NULL,
    version_id     UUID         NOT NULL,
    address_type   VARCHAR(255) NOT NULL,
    address_usage  VARCHAR(255) NOT NULL,
    address_line_1 VARCHAR(255) NOT NULL,
    address_line_2 VARCHAR(255) NOT NULL,
    address_line_3 VARCHAR(255) NOT NULL,
    address_line_4 VARCHAR(255) NOT NULL,
    postcode       VARCHAR(255) NOT NULL,
    CONSTRAINT pk_address PRIMARY KEY (id)
);

CREATE TABLE alcohol_monitoring
(
    id                      UUID NOT NULL,
    version_id              UUID NOT NULL,
    monitoring_type         VARCHAR(255),
    start_date              TIMESTAMP WITHOUT TIME ZONE,
    end_date                TIMESTAMP WITHOUT TIME ZONE,
    installation_location   VARCHAR(255),
    installation_address_id UUID,
    prison_name             VARCHAR(255),
    probation_office_name   VARCHAR(255),
    CONSTRAINT pk_alcohol_monitoring PRIMARY KEY (id)
);

CREATE TABLE contact_details
(
    id             UUID NOT NULL,
    version_id     UUID NOT NULL,
    contact_number VARCHAR(255),
    CONSTRAINT pk_contact_details PRIMARY KEY (id)
);

CREATE TABLE curfew
(
    id                 UUID         NOT NULL,
    version_id         UUID         NOT NULL,
    start_date         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_date           TIMESTAMP WITHOUT TIME ZONE,
    curfew_address     VARCHAR(255) NOT NULL,
    curfew_description VARCHAR(255),
    CONSTRAINT pk_curfew PRIMARY KEY (id)
);

CREATE TABLE curfew_release_date
(
    id             UUID         NOT NULL,
    version_id     UUID         NOT NULL,
    release_date   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    start_time     VARCHAR(255) NOT NULL,
    end_time       VARCHAR(255) NOT NULL,
    curfew_address VARCHAR(255) NOT NULL,
    CONSTRAINT pk_curfew_release_date PRIMARY KEY (id)
);

CREATE TABLE curfew_timetable
(
    id             UUID         NOT NULL,
    version_id     UUID         NOT NULL,
    day_of_week    VARCHAR(255) NOT NULL,
    start_time     VARCHAR(255) NOT NULL,
    end_time       VARCHAR(255) NOT NULL,
    curfew_address VARCHAR(255) NOT NULL,
    CONSTRAINT pk_curfew_timetable PRIMARY KEY (id)
);

CREATE TABLE device_wearer
(
    id                            UUID NOT NULL,
    version_id                    UUID NOT NULL,
    nomis_id                      VARCHAR(255),
    pnc_id                        VARCHAR(255),
    delius_id                     VARCHAR(255),
    home_office_reference_number  VARCHAR(255),
    prison_number                 VARCHAR(255),
    first_name                    VARCHAR(255),
    last_name                     VARCHAR(255),
    alias                         VARCHAR(255),
    adult_at_time_of_installation BOOLEAN,
    sex                           VARCHAR(255),
    gender                        VARCHAR(255),
    language                      VARCHAR(255),
    interpreter_required          BOOLEAN,
    date_of_birth                 TIMESTAMP WITHOUT TIME ZONE,
    disabilities                  VARCHAR(255),
    no_fixed_abode                BOOLEAN,
    CONSTRAINT pk_device_wearer PRIMARY KEY (id)
);

CREATE TABLE enforcement_zone
(
    id            UUID         NOT NULL,
    version_id    UUID         NOT NULL,
    zone_type     VARCHAR(255) NOT NULL,
    start_date    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_date      TIMESTAMP WITHOUT TIME ZONE,
    description   VARCHAR(255) NOT NULL,
    duration      VARCHAR(255) NOT NULL,
    file_name     VARCHAR(255),
    file_id       UUID,
    zone_id       INTEGER,
    zone_location VARCHAR(255),
    CONSTRAINT pk_enforcement_zone PRIMARY KEY (id)
);

CREATE TABLE fms_attachment_submission_result
(
    id                       UUID         NOT NULL,
    status                   VARCHAR(255) NOT NULL,
    sys_id                   VARCHAR(255) NOT NULL,
    file_type                VARCHAR(255) NOT NULL,
    attachment_id            VARCHAR(255) NOT NULL,
    error                    VARCHAR(255) NOT NULL,
    fms_attachment_result_id UUID,
    CONSTRAINT pk_fms_attachment_submission_result PRIMARY KEY (id)
);

CREATE TABLE fms_device_wearer_submission_result
(
    id               UUID            NOT NULL,
    status           VARCHAR(255)    NOT NULL,
    payload          VARCHAR(409600) NOT NULL,
    device_wearer_id VARCHAR(255)    NOT NULL,
    error            VARCHAR(255)    NOT NULL,
    CONSTRAINT pk_fms_device_wearer_submission_result PRIMARY KEY (id)
);

CREATE TABLE fms_monitoring_order_submission_result
(
    id                  UUID            NOT NULL,
    status              VARCHAR(255)    NOT NULL,
    payload             VARCHAR(409600) NOT NULL,
    monitoring_order_id VARCHAR(255)    NOT NULL,
    error               VARCHAR(255)    NOT NULL,
    CONSTRAINT pk_fms_monitoring_order_submission_result PRIMARY KEY (id)
);

CREATE TABLE fms_submission_result
(
    id                             UUID         NOT NULL,
    order_id                       UUID         NOT NULL,
    submission_strategy            VARCHAR(255) NOT NULL,
    fms_order_source               VARCHAR(255) NOT NULL,
    fms_device_wearer_result_id    UUID,
    fms_monitoring_order_result_id UUID,
    CONSTRAINT pk_fms_submission_result PRIMARY KEY (id)
);

CREATE TABLE installation_and_risk
(
    id              UUID NOT NULL,
    version_id      UUID NOT NULL,
    offence         VARCHAR(255),
    risk_category   VARCHAR(255),
    risk_details    VARCHAR(255),
    mappa_level     VARCHAR(255),
    mappa_case_type VARCHAR(255),
    CONSTRAINT pk_installation_and_risk PRIMARY KEY (id)
);

CREATE TABLE interested_parties
(
    id                                    UUID         NOT NULL,
    version_id                            UUID         NOT NULL,
    responsible_officer_name              VARCHAR(255) NOT NULL,
    responsible_officer_phone_number      VARCHAR(255),
    responsible_organisation              VARCHAR(255),
    responsible_organisation_region       VARCHAR(255) NOT NULL,
    responsible_organisation_phone_number VARCHAR(255),
    responsible_organisation_email        VARCHAR(255) NOT NULL,
    notifying_organisation                VARCHAR(255) NOT NULL,
    notifying_organisation_name           VARCHAR(255) NOT NULL,
    notifying_organisation_email          VARCHAR(255) NOT NULL,
    responsible_organisation_address_id   UUID,
    CONSTRAINT pk_interested_parties PRIMARY KEY (id)
);

CREATE TABLE mandatory_attendance
(
    id              UUID         NOT NULL,
    version_id      UUID         NOT NULL,
    start_date      date         NOT NULL,
    end_date        date,
    purpose         VARCHAR(255) NOT NULL,
    appointment_day VARCHAR(255) NOT NULL,
    start_time      VARCHAR(255) NOT NULL,
    end_time        VARCHAR(255) NOT NULL,
    address_line_1  VARCHAR(255) NOT NULL,
    address_line_2  VARCHAR(255) NOT NULL,
    address_line_3  VARCHAR(255) NOT NULL,
    address_line_4  VARCHAR(255),
    postcode        VARCHAR(255) NOT NULL,
    CONSTRAINT pk_mandatory_attendance PRIMARY KEY (id)
);

CREATE TABLE monitoring_conditions
(
    id                     UUID NOT NULL,
    version_id             UUID NOT NULL,
    start_date             TIMESTAMP WITHOUT TIME ZONE,
    end_date               TIMESTAMP WITHOUT TIME ZONE,
    order_type             VARCHAR(255),
    order_type_description VARCHAR(255),
    case_id                VARCHAR(255),
    condition_type         VARCHAR(255),
    curfew                 BOOLEAN,
    exclusion_zone         BOOLEAN,
    trail                  BOOLEAN,
    mandatory_attendance   BOOLEAN,
    alcohol                BOOLEAN,
    sentence_type          VARCHAR(255),
    issp                   VARCHAR(255),
    hdc                    VARCHAR(255),
    prarr                  VARCHAR(255),
    CONSTRAINT pk_monitoring_conditions PRIMARY KEY (id)
);

CREATE TABLE order_version
(
    id            UUID         NOT NULL,
    order_id      UUID         NOT NULL,
    version_id    INTEGER      NOT NULL,
    user_name     VARCHAR(255) NOT NULL,
    status        VARCHAR(255) NOT NULL,
    type          VARCHAR(255) NOT NULL,
    fms_result_id UUID,
    CONSTRAINT pk_order_version PRIMARY KEY (id)
);

CREATE TABLE orders
(
    id UUID NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id)
);

CREATE TABLE responsible_adult
(
    id                         UUID NOT NULL,
    version_id                 UUID NOT NULL,
    full_name                  VARCHAR(255),
    relationship               VARCHAR(255),
    other_relationship_details VARCHAR(255),
    contact_number             VARCHAR(255),
    CONSTRAINT pk_responsible_adult PRIMARY KEY (id)
);

CREATE TABLE trail_monitoring
(
    id         UUID NOT NULL,
    version_id UUID NOT NULL,
    start_date TIMESTAMP WITHOUT TIME ZONE,
    end_date   TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_trail_monitoring PRIMARY KEY (id)
);

CREATE TABLE variation_details
(
    id             UUID         NOT NULL,
    version_id     UUID         NOT NULL,
    variation_type VARCHAR(255) NOT NULL,
    variation_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_variation_details PRIMARY KEY (id)
);

ALTER TABLE alcohol_monitoring
    ADD CONSTRAINT uc_alcohol_monitoring_installation_address UNIQUE (installation_address_id);

ALTER TABLE alcohol_monitoring
    ADD CONSTRAINT uc_alcohol_monitoring_version UNIQUE (version_id);

ALTER TABLE contact_details
    ADD CONSTRAINT uc_contact_details_version UNIQUE (version_id);

ALTER TABLE curfew_release_date
    ADD CONSTRAINT uc_curfew_release_date_version UNIQUE (version_id);

ALTER TABLE curfew
    ADD CONSTRAINT uc_curfew_version UNIQUE (version_id);

ALTER TABLE device_wearer
    ADD CONSTRAINT uc_device_wearer_version UNIQUE (version_id);

ALTER TABLE address
    ADD CONSTRAINT uc_e5ad4b6797b6dc882b7042be1 UNIQUE (version_id, address_type);

ALTER TABLE order_version
    ADD CONSTRAINT uc_f64759c215a0ce8152705d2ac UNIQUE (order_id, version_id);

ALTER TABLE fms_submission_result
    ADD CONSTRAINT uc_fms_submission_result_fms_device_wearer_result UNIQUE (fms_device_wearer_result_id);

ALTER TABLE fms_submission_result
    ADD CONSTRAINT uc_fms_submission_result_fms_monitoring_order_result UNIQUE (fms_monitoring_order_result_id);

ALTER TABLE installation_and_risk
    ADD CONSTRAINT uc_installation_and_risk_version UNIQUE (version_id);

ALTER TABLE interested_parties
    ADD CONSTRAINT uc_interested_parties_responsible_organisation_address UNIQUE (responsible_organisation_address_id);

ALTER TABLE interested_parties
    ADD CONSTRAINT uc_interested_parties_version UNIQUE (version_id);

ALTER TABLE monitoring_conditions
    ADD CONSTRAINT uc_monitoring_conditions_version UNIQUE (version_id);

ALTER TABLE responsible_adult
    ADD CONSTRAINT uc_responsible_adult_version UNIQUE (version_id);

ALTER TABLE trail_monitoring
    ADD CONSTRAINT uc_trail_monitoring_version UNIQUE (version_id);

ALTER TABLE variation_details
    ADD CONSTRAINT uc_variation_details_version UNIQUE (version_id);

ALTER TABLE additional_documents
    ADD CONSTRAINT FK_ADDITIONAL_DOCUMENTS_ON_VERSION FOREIGN KEY (version_id) REFERENCES order_version (id);

ALTER TABLE address
    ADD CONSTRAINT FK_ADDRESS_ON_VERSION FOREIGN KEY (version_id) REFERENCES order_version (id);

ALTER TABLE alcohol_monitoring
    ADD CONSTRAINT FK_ALCOHOL_MONITORING_ON_INSTALLATION_ADDRESS FOREIGN KEY (installation_address_id) REFERENCES address (id);

ALTER TABLE alcohol_monitoring
    ADD CONSTRAINT FK_ALCOHOL_MONITORING_ON_VERSION FOREIGN KEY (version_id) REFERENCES order_version (id);

ALTER TABLE contact_details
    ADD CONSTRAINT FK_CONTACT_DETAILS_ON_VERSION FOREIGN KEY (version_id) REFERENCES order_version (id);

ALTER TABLE curfew
    ADD CONSTRAINT FK_CURFEW_ON_VERSION FOREIGN KEY (version_id) REFERENCES order_version (id);

ALTER TABLE curfew_release_date
    ADD CONSTRAINT FK_CURFEW_RELEASE_DATE_ON_VERSION FOREIGN KEY (version_id) REFERENCES order_version (id);

ALTER TABLE curfew_timetable
    ADD CONSTRAINT FK_CURFEW_TIMETABLE_ON_VERSION FOREIGN KEY (version_id) REFERENCES order_version (id);

ALTER TABLE device_wearer
    ADD CONSTRAINT FK_DEVICE_WEARER_ON_VERSION FOREIGN KEY (version_id) REFERENCES order_version (id);

ALTER TABLE enforcement_zone
    ADD CONSTRAINT FK_ENFORCEMENT_ZONE_ON_VERSION FOREIGN KEY (version_id) REFERENCES order_version (id);

ALTER TABLE fms_attachment_submission_result
    ADD CONSTRAINT FK_FMS_ATTACHMENT_SUBMISSION_RESULT_ON_FMS_ATTACHMENT_RESULT FOREIGN KEY (fms_attachment_result_id) REFERENCES fms_submission_result (id);

ALTER TABLE fms_submission_result
    ADD CONSTRAINT FK_FMS_SUBMISSION_RESULT_ON_FMS_DEVICE_WEARER_RESULT FOREIGN KEY (fms_device_wearer_result_id) REFERENCES fms_device_wearer_submission_result (id);

ALTER TABLE fms_submission_result
    ADD CONSTRAINT FK_FMS_SUBMISSION_RESULT_ON_FMS_MONITORING_ORDER_RESULT FOREIGN KEY (fms_monitoring_order_result_id) REFERENCES fms_monitoring_order_submission_result (id);

ALTER TABLE installation_and_risk
    ADD CONSTRAINT FK_INSTALLATION_AND_RISK_ON_VERSION FOREIGN KEY (version_id) REFERENCES order_version (id);

ALTER TABLE interested_parties
    ADD CONSTRAINT FK_INTERESTED_PARTIES_ON_RESPONSIBLE_ORGANISATION_ADDRESS FOREIGN KEY (responsible_organisation_address_id) REFERENCES address (id);

ALTER TABLE interested_parties
    ADD CONSTRAINT FK_INTERESTED_PARTIES_ON_VERSION FOREIGN KEY (version_id) REFERENCES order_version (id);

ALTER TABLE mandatory_attendance
    ADD CONSTRAINT FK_MANDATORY_ATTENDANCE_ON_VERSION FOREIGN KEY (version_id) REFERENCES order_version (id);

ALTER TABLE monitoring_conditions
    ADD CONSTRAINT FK_MONITORING_CONDITIONS_ON_VERSION FOREIGN KEY (version_id) REFERENCES order_version (id);

ALTER TABLE order_version
    ADD CONSTRAINT FK_ORDER_VERSION_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id);

ALTER TABLE responsible_adult
    ADD CONSTRAINT FK_RESPONSIBLE_ADULT_ON_VERSION FOREIGN KEY (version_id) REFERENCES order_version (id);

ALTER TABLE trail_monitoring
    ADD CONSTRAINT FK_TRAIL_MONITORING_ON_VERSION FOREIGN KEY (version_id) REFERENCES order_version (id);

ALTER TABLE variation_details
    ADD CONSTRAINT FK_VARIATION_DETAILS_ON_VERSION FOREIGN KEY (version_id) REFERENCES order_version (id);