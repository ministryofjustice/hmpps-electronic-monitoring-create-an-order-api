UPDATE order_version
SET tags = CASE when tags is null or tags = '' then concat(tags, 'Youth YOI') ELSE concat(tags, ',Youth YOI') END
FROM interested_parties, device_wearer
WHERE order_version.id = interested_parties.version_id
AND order_version.id = device_wearer.version_id
  AND order_version.status = 'SUBMITTED'
  AND interested_parties.notifying_organisation = 'PRISON'
  AND device_wearer.adult_at_time_of_installation = false;