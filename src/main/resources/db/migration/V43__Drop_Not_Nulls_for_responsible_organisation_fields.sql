ALTER Table interested_parties
    ALTER COLUMN responsible_officer_name DROP NOT NULL,
    ALTER COLUMN responsible_organisation_region DROP NOT NULL,
    ALTER COLUMN responsible_organisation_email DROP NOT NULL;
