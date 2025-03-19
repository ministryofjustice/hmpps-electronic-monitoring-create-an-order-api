package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data

object ValidationErrors {
  const val NOTIFYING_ORGANISATION_REQUIRED: String = "Notifying Organisation is required"
  const val NOTIFYING_ORGANISATION_NAME_REQUIRED: String = "Notifying Organisation Name is required"
  const val RESPONSIBLE_ORGANISATION_REQUIRED: String = "Responsible Organisation is required"
  const val RESPONSIBLE_ORGANISATION_REGION_REQUIRED: String = "Responsible Organisation Region is required"

  object Address {
    const val ADDRESS_1_REQUIRED: String = "Enter address line 1, typically the building and street"
    const val ADDRESS_3_REQUIRED: String = "Enter town or city"
    const val POSTCODE_REQUIRED: String = "Enter postcode"
    const val POSTCODE_MUST_BE_VALID: String = "Enter a full UK postcode"
  }

  object AlcoholMonitoring {
    const val MONITORING_TYPE_REQUIRED: String = "Select what alcohol monitoring the device wearer needs"
    const val START_DATE_REQUIRED: String = "Enter date alcohol monitoring starts"
    const val END_DATE_MUST_BE_IN_FUTURE: String = "Date alcohol monitoring ends must be in the future"
    const val INSTALLATION_LOCATION_REQUIRED: String = "Select the address of the base station "
    const val PRISON_NAME_REQUIRED_LOCATION_IS_PRISON: String = "Enter prison name"
    const val OFFICE_NAME_REQUIRED_LOCATION_IS_PROBATION_OFFICE: String = "Enter probation office name"
  }

  object CurfewConditions {
    const val START_DATE_REQUIRED: String = "Enter date curfew starts"
    const val END_DATE_MUST_BE_IN_FUTURE: String = "Date curfew ends must be in the future"
    const val END_DATE_MUST_BE_AFTER_START_DATE: String = "Date curfew ends be after the date curfew starts"
    const val ADDRESS_REQUIRED: String = "Select where the device wearer will be during curfew hours"
  }

  object CurfewReleaseDateConditions {
    const val START_DATE_REQUIRED: String = "Enter date device wearer is released from custody"
    const val START_TIME_REQUIRED: String = "Enter time curfew starts on day of release"
    const val END_TIME_REQUIRED: String = "Enter time curfew ends on day of release"
    const val ADDRESS_REQUIRED: String =
      "Select the address the device wearer will be during curfew hours on the day of release"
  }

  object CurfewTimetable {
    const val START_TIME_REQUIRED: String = "Enter time curfew starts"
    const val END_TIME_REQUIRED: String = "Enter time curfew ends"
    const val ADDRESS_REQUIRED: String = "Select where the device wearer will be during curfew hours"
  }

  object DeviceWearer {
    const val FIRST_NAME_REQUIRED: String = "Enter device wearer's first name"
    const val LAST_NAME_REQUIRED: String = "Enter device wearer's last name"
    const val IS_ADULT_REQUIRED: String = "Select yes if a responsible adult is required"
    const val SEX_REQUIRED: String = "Select the device wearer's sex, or select 'Not able to provide this information'"
    const val GENDER_REQUIRED: String =
      "Select the device wearer's gender, or select 'Not able to provide this information'"
    const val DOB_REQUIRED: String = "Enter date of birth"
    const val DOB_MUST_BE_IN_PAST: String = "Date of birth must be in the past"
    const val INTERPRETER_REQUIRED: String = "Select yes if the device wearer requires an interpreter"
    const val LANGUAGE_REQUIRED: String = "Select the language required"
  }

  // This may be "Exclusion Zone" in the front end, but also "Inclusion Zone"?
  object EnforcementZone {
    const val DESCRIPTION_REQUIRED: String = "Enforcement zone description is required"
    const val DURATION_REQUIRED: String = "Enforcement zone duration is required"
    const val END_DATE_MUST_BE_IN_FUTURE: String = "Enforcement zone end date must be in the future"
    const val START_DATE_REQUIRED: String = "Enforcement zone start date is required"
    const val TYPE_REQUIRED: String = "Enforcement Zone type is required"
  }

  object InstallationAndRisk {
    const val RISK_CATEGORY_VALID: String = "Risk categories must be a valid risk category"
  }

  object MandatoryAttendance {
    const val START_DATE_REQUIRED: String = "Enter date mandatory attendance monitoring starts"
    const val PURPOSE_REQUIRED: String = "Enter what the appointment is for"
    const val APPOINTMENT_DAY_REQUIRED: String = "Enter the day the appointment is on"
    const val START_TIME_REQUIRED: String = "Enter time appointment starts"
    const val END_TIME_REQUIRED: String = "Enter time appointment ends"
  }

  object MonitoringConditions {
    const val MONITORING_TYPE_MINIMUM_ONE: String = "Select monitoring required"
    const val ORDER_TYPE_REQUIRED: String = "Select order type"
    const val START_DATE_REQUIRED: String = "Enter start date for monitoring"
    const val TYPE_REQUIRED: String = "Select order type"
    const val END_DATE_MUST_BE_AFTER_START_DATE: String = "End date must be after start date"
  }

  object NoFixedAbode {
    const val NO_FIXED_ABODE_REQUIRED: String = "Select yes if the device wearer has a fixed address"
  }

  object ResponsibleAdult {
    const val FIRST_NAME_REQUIRED: String = "Enter responsible adult's first name"
    const val LAST_NAME_REQUIRED: String = "Enter responsible adult's last name"
    const val FULL_NAME_REQUIRED: String = "Enter responsible adult's full name"
    const val RELATIONSHIP_REQUIRED: String = "Enter details of their relationship"
    const val RELATIONSHIP_DETAILS_REQUIRED: String = "Select their relationship to the device wearer"
  }

  object TrailMonitoringConditions {
    const val START_DATE_REQUIRED: String = "Enter date trail monitoring starts"
    const val END_DATE_MUST_BE_IN_FUTURE: String = "End date must be in the future"
  }

  object VariationDetails {
    const val TYPE_REQUIRED: String = "Variation type is required"
    const val DATE_REQUIRED: String = "Variation date is required"
    const val TYPE_MUST_BE_VALID: String = "Variation type must be a valid variation type"
    const val DATE_MUST_BE_VALID: String = "Variation date must be a valid date"
  }
}
