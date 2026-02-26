
ALTER TABLE interested_parties
    ADD COLUMN responsible_officer_first_name varchar(255),
    ADD COLUMN responsible_officer_last_name varchar(255),
    ADD COLUMN responsible_officer_email varchar(255);