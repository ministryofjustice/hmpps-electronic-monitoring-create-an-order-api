package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data

import java.util.UUID

object ValidationErrors {
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
    const val END_DATE_REQUIRED: String = "Enter date curfew ends"
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

    fun curfewNotFound(orderId: UUID): String = "Curfew conditions for $orderId not found"
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
    const val DISABILITIES_INVALID: String = "One or more disability values are invalid"
    const val OTHER_DISABILITY: String = "Enter device wearer's disability or health condition"
  }

  // This may be "Exclusion Zone" in the front end, but also "Inclusion Zone"?
  object EnforcementZone {
    const val DESCRIPTION_REQUIRED: String = "Enforcement zone description is required"
    const val DURATION_REQUIRED: String = "Enforcement zone duration is required"
    const val END_DATE_MUST_BE_IN_FUTURE: String = "Enforcement zone end date must be in the future"
    const val START_DATE_REQUIRED: String = "Enforcement zone start date is required"
    const val TYPE_REQUIRED: String = "Enforcement Zone type is required"
  }

  object InterestedParties {
    const val NOTIFYING_ORGANISATION_REQUIRED: String = "Select the organisation you are part of"
    const val NOTIFYING_ORGANISATION_NAME_REQUIRED: String = "Select the name of the organisation you are part of"
    const val RESPONSIBLE_ORGANISATION_REQUIRED: String = "Select the responsible officer's organisation"
    const val RESPONSIBLE_ORGANISATION_REGION_REQUIRED: String = "Select the probation region"
    const val TEAM_EMAIL_REQUIRED: String = "Enter your team's email address"
    const val RESPONSIBLE_OFFICER_FULL_NAME_REQUIRED: String = "Enter the Responsible Officer's full name"
    const val RESPONSIBLE_OFFICER_TELEPHONE_NUMBER_REQUIRED: String = "Enter the Responsible Officer's telephone number"
    const val RESPONSIBLE_ORGANISATION_TELEPHONE_NUMBER_REQUIRED: String =
      "Enter the Responsible Organisation's telephone number"
    const val RESPONSIBLE_ORGANISATION_EMAIL_REQUIRED: String = "Enter the Responsible Organisation's email address"
  }

  object ProbationDeliveryUnit {
    const val RESPONSIBLE_ORGANISATION_NOT_PROBATION: String = "Responsible organisation must be Probation"
    const val DELIVERY_UNIT_NOT_IN_REGION: String = "Select probation delivery unit within given probation region"
  }

  object InstallationAndRisk {
    const val OFFENCE_VALID: String = "Offence must be a valid offence"
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
    const val END_DATE_REQUIRED: String = "Enter end date for monitoring"
    const val TYPE_REQUIRED: String = "Select order type"
    const val END_DATE_MUST_BE_AFTER_START_DATE: String = "End date must be after start date"
    const val END_DATE_MUST_BE_IN_THE_FUTURE: String = "End date of monitoring must be in the future"
  }

  object NoFixedAbode {
    const val NO_FIXED_ABODE_REQUIRED: String = "Select yes if the device wearer has a fixed address"
  }

  object OrderSectionServiceBase {
    fun noEditableOrderExists(id: UUID) = "An editable order with $id does not exist"
  }

  object ResponsibleAdult {
    const val FIRST_NAME_REQUIRED: String = "Enter responsible adult's first name"
    const val LAST_NAME_REQUIRED: String = "Enter responsible adult's last name"
    const val FULL_NAME_REQUIRED: String = "Enter responsible adult's full name"
    const val RELATIONSHIP_REQUIRED: String = "Select their relationship to the device wearer"
    const val RELATIONSHIP_DETAILS_REQUIRED: String = "Enter details of their relationship"
  }

  object TrailMonitoringConditions {
    const val START_DATE_REQUIRED: String = "Enter date trail monitoring starts"
    const val END_DATE_REQUIRED: String = "Enter date trail monitoring ends"
    const val END_DATE_MUST_BE_IN_FUTURE: String = "End date must be in the future"
  }

  object VariationDetails {
    const val TYPE_REQUIRED: String = "Select what you have changed"
    const val DATE_REQUIRED: String = "Enter the date you want the changes to take effect"
    const val DETAILS_REQUIRED: String = "Enter information on what you have changed"
    const val TYPE_MUST_BE_VALID: String = "Variation type must be a valid variation type"
    fun typeObsolete(type: String): String = "Variation type $type is obsolete"
    const val DATE_MUST_BE_VALID: String = "Date you want changes to take effect must be a real date"
    const val DETAIL_TOO_LONG: String = "Variation details is too long"
  }

  object InstallationLocation {
    const val INSTALLATION_LOCATION_REQUIRED: String = "Select where installation will take place"
  }

  object InstallationAppointment {
    const val PLACE_NAME_REQUIRED: String = "Enter name of the place where installation takes place"
    const val APPOINT_DATE_REQUIRED: String = "Enter date of installation"
  }
}
