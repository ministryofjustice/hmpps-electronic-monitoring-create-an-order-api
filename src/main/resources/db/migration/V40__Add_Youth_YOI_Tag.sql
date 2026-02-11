UPDATE order_version
SET tags = CASE when tags is null then concat(tags, 'Youth YOI') ELSE concat(tags, ',Youth YOI') END
FROM interested_parties, responsible_adult
WHERE order_version.id = interested_parties.version_id
AND order_version.id = responsible_adult.version_id
  AND order_version.status = 'SUBMITTED'
  AND interested_parties.notifying_organisation = 'PRISON';