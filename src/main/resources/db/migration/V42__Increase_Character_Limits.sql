ALTER Table curfew
    ALTER   COLUMN curfew_additional_details TYPE VARCHAR(500);

ALTER Table installation_and_risk
    ALTER   COLUMN risk_category TYPE VARCHAR(500),
    ALTER   COLUMN offence_additional_details TYPE VARCHAR(500),
    ALTER   COLUMN risk_details TYPE VARCHAR(500);

ALTER Table installation_and_risk
    ALTER   COLUMN risk_details TYPE VARCHAR(500);

ALTER Table offence_additional_details
    ALTER   COLUMN additional_details TYPE VARCHAR(500);


ALTER Table enforcement_zone
    ALTER   COLUMN description TYPE VARCHAR(500);