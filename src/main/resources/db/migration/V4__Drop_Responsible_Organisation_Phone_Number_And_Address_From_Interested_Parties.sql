ALTER TABLE interested_parties
    DROP COLUMN IF EXISTS responsible_organisation_phone_number,
    DROP COLUMN IF EXISTS responsible_organisation_address_id ;

DELETE FROM address
WHERE address_type = 'RESPONSIBLE_ORGANISATION';