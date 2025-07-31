package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.ZonedDateTime
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
  var startDate: ZonedDateTime? = null,

  @Column(name = "END_DATE", nullable = true)
  var endDate: ZonedDateTime? = null,

  @Column(name = "PURPOSE", nullable = false)
  var purpose: String? = null,

  @Column(name = "APPOINTMENT_DAY", nullable = false)
  var appointmentDay: String? = null,

  @Column(name = "START_TIME", nullable = false)
  var startTime: String? = null,

  @Column(name = "END_TIME", nullable = false)
  var endTime: String? = null,

  @Column(name = "ADDRESS_LINE_1", nullable = false)
  var addressLine1: String? = null,

  @Column(name = "ADDRESS_LINE_2", nullable = false)
  var addressLine2: String? = null,

  @Column(name = "ADDRESS_LINE_3", nullable = true)
  var addressLine3: String? = null,

  @Column(name = "ADDRESS_LINE_4", nullable = true)
  var addressLine4: String? = null,

  @Column(name = "POSTCODE", nullable = true)
  var postcode: String? = null,

  @Schema(hidden = true)
  @ManyToOne(optional = true)
  @JoinColumn(name = "VERSION_ID", updatable = false, insertable = false)
  private val version: OrderVersion? = null,
)
