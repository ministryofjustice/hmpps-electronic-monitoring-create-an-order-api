-- https://dsdmoj.atlassian.net/browse/ELM-4016
ALTER TABLE alcohol_monitoring
    DROP CONSTRAINT IF EXISTS fk_alcohol_monitoring_on_installation_address,
    DROP COLUMN IF EXISTS installation_address_id,
    DROP COLUMN IF EXISTS installation_location,
    DROP COLUMN IF EXISTS prison_name,
    DROP COLUMN IF EXISTS probation_office_name;