ALTER TABLE interested_parties
    ALTER COLUMN notifying_organisation DROP NOT NULL,
    ALTER COLUMN notifying_organisation_name DROP NOT NULL,
    ALTER COLUMN notifying_organisation_email DROP NOT NULL;