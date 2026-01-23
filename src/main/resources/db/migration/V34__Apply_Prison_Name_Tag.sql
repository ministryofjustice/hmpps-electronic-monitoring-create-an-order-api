UPDATE order_version
SET tags = CASE when tags is null then concat(tags, interested_parties.notifying_organisation_name) ELSE concat(tags, ',', interested_parties.notifying_organisation_name) END
FROM interested_parties
WHERE order_version.id = interested_parties.version_id AND order_version.status = 'SUBMITTED'
  AND interested_parties.notifying_organisation = 'PRISON';