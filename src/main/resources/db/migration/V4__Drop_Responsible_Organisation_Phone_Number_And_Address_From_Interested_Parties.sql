ALTER TABLE postgres.public.interested_parties
    DROP COLUMN IF EXISTS responsible_organisation_phone_number,
    DROP COLUMN IF EXISTS responsible_organisation_address_id ;