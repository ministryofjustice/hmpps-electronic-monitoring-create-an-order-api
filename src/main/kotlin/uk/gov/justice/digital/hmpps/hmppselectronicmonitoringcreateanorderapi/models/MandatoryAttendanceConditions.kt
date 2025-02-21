package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import java.time.LocalDate
import java.util.*

@Entity
@Table(name = "MANDATORY_ATTENDANCE")
data class MandatoryAttendanceConditions(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "VERSION_ID", nullable = false, unique = false)
  val versionId: UUID,

  @Column(name = "START_DATE", nullable = false)
  @field:NotNull(
    message = ValidationErrors.MandatoryAttendance.START_DATE_REQUIRED,
  )
  var startDate: LocalDate? = null,

  @Column(name = "END_DATE", nullable = true)
  var endDate: LocalDate? = null,

  @Column(name = "PURPOSE", nullable = false)
  @field:NotNull(message = ValidationErrors.MandatoryAttendance.PURPOSE_REQUIRED)
  var purpose: String? = null,

  @Column(name = "APPOINTMENT_DAY", nullable = false)
  @field:NotNull(message = ValidationErrors.MandatoryAttendance.APPOINTMENT_DAY_REQUIRED)
  var appointmentDay: String? = null,

  @Column(name = "START_TIME", nullable = false)
  @field:NotNull(message = ValidationErrors.MandatoryAttendance.START_TIME_REQUIRED)
  var startTime: String? = null,

  @Column(name = "END_TIME", nullable = false)
  @field:NotNull(message = ValidationErrors.MandatoryAttendance.END_TIME_REQUIRED)
  var endTime: String? = null,

  @Column(name = "ADDRESS_LINE_1", nullable = false)
  @field:NotNull(message = ValidationErrors.Address.ADDRESS_1_REQUIRED)
  var addressLine1: String? = null,

  @Column(name = "ADDRESS_LINE_2", nullable = false)
  var addressLine2: String? = null,

  @Column(name = "ADDRESS_LINE_3", nullable = true)
  @field:NotNull(message = ValidationErrors.Address.ADDRESS_3_REQUIRED)
  var addressLine3: String? = null,

  @Column(name = "ADDRESS_LINE_4", nullable = true)
  var addressLine4: String? = null,

  @Column(name = "POSTCODE", nullable = true)
  @field:NotNull(message = ValidationErrors.Address.POSTCODE_REQUIRED)
  var postcode: String? = null,

  @Schema(hidden = true)
  @ManyToOne(optional = true)
  @JoinColumn(name = "VERSION_ID", updatable = false, insertable = false)
  private val version: OrderVersion? = null,
)
