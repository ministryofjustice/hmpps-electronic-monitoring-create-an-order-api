package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import java.time.ZonedDateTime
import java.util.*

data class UpdateMandatoryAttendanceDto(
  val id: UUID? = null,

  val startDate: ZonedDateTime? = null,

  var endDate: ZonedDateTime? = null,

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
