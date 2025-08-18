-- https://dsdmoj.atlassian.net/browse/ELM-4016
ALTER TABLE alcohol_monitoring
DROP
CONSTRAINT fk_alcohol_monitoring_on_installation_address;

ALTER TABLE alcohol_monitoring
DROP
COLUMN installation_address_id;

ALTER TABLE alcohol_monitoring
DROP
COLUMN installation_location;

ALTER TABLE alcohol_monitoring
DROP
COLUMN prison_name;

ALTER TABLE alcohol_monitoring
DROP
COLUMN probation_office_name;