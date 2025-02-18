package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import java.time.LocalDate

data class UpdateMandatoryAttendanceDto(
  val startDate: LocalDate? = null,

  var endDate: LocalDate? = null,

  var purpose: String? = null,

  var appointmentDay: String? = null,

  var startTime: String? = null,

  var endTime: String? = null,

  var addressLine1: String? = null,

  var addressLine2: String? = null,

  var addressLine3: String? = null,

  var addressLine4: String? = null,

  var postcode: String? = null,
)
