INSERT INTO status_update (id, order_id, status, date_time_of_status_change, status_updated_by)
SELECT gen_random_uuid(),
       ov.order_id,
       'SUBMITTED',
       COALESCE(ov.fms_result_date, ov.last_updated_datetime, CURRENT_TIMESTAMP),
       ov.last_updated_by
FROM order_version ov
WHERE ov.status = 'SUBMITTED';
