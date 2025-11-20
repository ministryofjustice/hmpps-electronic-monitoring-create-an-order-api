package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data

import java.util.UUID

object ValidationErrors {
  object Address {
    const val ADDRESS_1_REQUIRED: String = "Enter address line 1, typically the building and street"
    const val ADDRESS_1_MAX_LENGTH: String = "Address line 1 must be 200 characters or less"
    const val ADDRESS_2_MAX_LENGTH: String = "Address line 2 must be 200 characters or less"
    const val ADDRESS_3_REQUIRED: String = "Enter town or city"
    const val ADDRESS_3_MAX_LENGTH: String = "Town or city must be 200 characters or less"
    const val ADDRESS_4_MAX_LENGTH: String = "County must be 200 characters or less"
    const val POSTCODE_REQUIRED: String = "Enter postcode"
    const val POSTCODE_MUST_BE_VALID: String = "Enter a full UK postcode"
    const val POSTCODE_MAX_LENGTH: String = "Postcode must be 200 characters or less"
  }

  object AlcoholMonitoring {
    const val MONITORING_TYPE_REQUIRED: String = "Select what alcohol monitoring the device wearer needs"
    const val START_DATE_REQUIRED: String = "Enter date alcohol monitoring starts"
    const val END_DATE_MUST_BE_IN_FUTURE: String = "Date alcohol monitoring ends must be in the future"
    const val END_DATE_MUST_BE_AFTER_START_DATE: String =
      "Date alcohol monitoring ends must be after the date alcohol monitoring starts"
  }

  object CurfewConditions {
    const val START_DATE_REQUIRED: String = "Enter date curfew starts"
    const val END_DATE_REQUIRED: String = "Enter date curfew ends"
    const val END_DATE_MUST_BE_IN_FUTURE: String = "Date curfew ends must be in the future"
    const val END_DATE_MUST_BE_AFTER_START_DATE: String = "Date curfew ends must be after the date curfew starts"
    const val ADDRESS_REQUIRED: String = "Select where the device wearer will be during curfew hours"
  }

  object CurfewReleaseDateConditions {
    const val START_DATE_REQUIRED: String = "Enter date device wearer is released from custody"
    const val START_TIME_REQUIRED: String = "Enter time curfew starts on day of release"
    const val END_TIME_REQUIRED: String = "Enter time curfew ends on day after release"
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
    const val FIRST_NAME_MAX_LENGTH: String = "First name must be 200 characters or less"
    const val LAST_NAME_REQUIRED: String = "Enter device wearer's last name"
    const val LAST_NAME_MAX_LENGTH: String = "Last name must be 200 characters or less"
    const val ALIAS_MAX_LENGTH: String = "Preferred name must be 200 characters or less"
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
    const val DESCRIPTION_REQUIRED: String = "Exclusion zone description is required"
    const val DURATION_REQUIRED: String = "Exclusion zone duration is required"
    const val END_DATE_MUST_BE_IN_FUTURE: String = "Exclusion zone end date must be in the future"
    const val START_DATE_REQUIRED: String = "Exclusion zone start date is required"
    const val TYPE_REQUIRED: String = "Exclusion Zone type is required"
    const val INVALID_MAP_FILE_EXTENSION: String = "Select a PDF, PNG, JPEG or JPG"

    // Will be used once ELM-4162 is live
    const val NAME_REQUIRED = "Enter the name of the exclusion zone"
  }

  object IdentityNumbers {
    const val NOMIS_ID_MAX_LENGTH: String = "Nomis ID must be 200 characters or less"
    const val PNC_ID_MAX_LENGTH: String = "PNC ID must be 200 characters or less"
    const val DELIUS_ID_MAX_LENGTH: String = "Delius ID must be 200 characters or less"
    const val PRISON_NUMBER_MAX_LENGTH: String = "Prison number must be 200 characters or less"
    const val HOME_OFFICE_REFERENCE_NUMBER_MAX_LENGTH: String = "Nomis ID must be 200 characters or less"
  }

  object InterestedParties {
    const val NOTIFYING_ORGANISATION_REQUIRED: String = "Select the organisation you are part of"
    const val NOTIFYING_ORGANISATION_NAME_REQUIRED: String = "Select the name of the organisation you are part of"
    const val RESPONSIBLE_ORGANISATION_REQUIRED: String = "Select the responsible officer's organisation"
    const val RESPONSIBLE_ORGANISATION_REGION_REQUIRED: String = "Select the probation region"
    const val TEAM_EMAIL_REQUIRED: String = "Enter your team's email address"
    const val TEAM_EMAIL_MAX_LENGTH: String = "Team email address must be 200 characters or less"
    const val RESPONSIBLE_OFFICER_FULL_NAME_REQUIRED: String = "Enter the Responsible Officer's full name"
    const val RESPONSIBLE_OFFICER_NAME_MAX_LENGTH: String = "Responsible Officer's name must be 200 characters or less"
    const val RESPONSIBLE_OFFICER_TELEPHONE_NUMBER_REQUIRED: String = "Enter the Responsible Officer's telephone number"
    const val RESPONSIBLE_OFFICER_TELEPHONE_NUMBER_MAX_LENGTH: String =
      "Responsible Officer's telephone number must be 200 characters or less"
    const val RESPONSIBLE_ORGANISATION_TELEPHONE_NUMBER_REQUIRED: String =
      "Enter the Responsible Organisation's telephone number"
    const val RESPONSIBLE_ORGANISATION_EMAIL_REQUIRED: String =
      "Enter the Responsible Organisation's email address"
    const val RESPONSIBLE_ORGANISATION_EMAIL_MAX_LENGTH: String =
      "Responsible Officer's email address must be 200 characters or less"
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
    const val END_DATE_MUST_BE_IN_FUTURE: String = "End date must be in the future"
    const val END_DATE_MUST_BE_AFTER_START_DATE: String =
      "Date attendance monitoring ends must be after the date attendance monitoring starts"
    const val PURPOSE_REQUIRED: String = "Enter what the appointment is for"
    const val PURPOSE_MAX_LENGTH: String = "Appointment purpose must be 200 characters or less"
    const val APPOINTMENT_DAY_REQUIRED: String = "Enter the day and frequency of the appointment"
    const val APPOINTMENT_DAY_MAX_LENGTH: String = "Appointment day and frequency must be 200 characters or less"
    const val START_TIME_REQUIRED: String = "Enter time appointment starts"
    const val END_TIME_REQUIRED: String = "Enter time appointment ends"
  }

  object MonitoringConditions {
    const val ORDER_TYPE_REQUIRED: String = "Select order type"
    const val START_DATE_REQUIRED: String = "Enter start date for monitoring"
    const val TYPE_REQUIRED: String = "Select order type"
    const val END_DATE_MUST_BE_AFTER_START_DATE: String = "End date must be after start date"
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
    const val FULL_NAME_MAX_LENGTH: String = "Full name must be 200 characters or less"
    const val RELATIONSHIP_REQUIRED: String = "Select their relationship to the device wearer"
    const val RELATIONSHIP_DETAILS_REQUIRED: String = "Enter details of their relationship"
    const val RELATIONSHIP_DETAILS_MAX_LENGTH: String = "Relationship description must be 200 characters or less"
  }

  object TrailMonitoringConditions {
    const val START_DATE_REQUIRED: String = "Enter date trail monitoring starts"
    const val END_DATE_REQUIRED: String = "Enter date trail monitoring ends"
    const val END_DATE_MUST_BE_IN_FUTURE: String = "End date must be in the future"
    const val END_DATE_MUST_BE_AFTER_START_DATE: String =
      "Date trail monitoring ends must be after the date trail monitoring starts"
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
    const val APPOINTMENT_DATE_REQUIRED: String = "Enter date of installation"
    const val APPOINTMENT_DATE_MUST_BE_IN_FUTURE: String = "Date of installation must be in the future"
  }

  object AdditionalDocuments {
    const val INVALID_LICENSE_FILE_EXTENSION: String = "Select a PDF or Word document"
    const val INVALID_PHOTO_ID_FILE_EXTENSION: String = "Select a PDF, PNG, JPEG or JPG"
    const val HAVE_PHOTO_REQUIRED: String = "Select if you have a photo to upload"
  }
}
