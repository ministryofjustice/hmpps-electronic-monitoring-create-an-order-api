UPDATE monitoring_conditions
SET condition_type = 'REQUIREMENT_OF_A_COMMUNITY_ORDER'
WHERE monitoring_conditions.condition_type = 'REQUIREMENT_OF_COMMUNITY_ORDER';