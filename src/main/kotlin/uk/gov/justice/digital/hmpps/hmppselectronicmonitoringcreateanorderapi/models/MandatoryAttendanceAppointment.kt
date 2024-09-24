package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "MANDATORY_ATTENDANCE_APPOINTMENT")
data class MandatoryAttendanceAppointment(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "MANDATORY_ATTENDANCE_ID", nullable = false)
  val attendanceId: UUID,

  @Column(name = "START_TIME", nullable = true)
  var startTime: String? = null,

  @Column(name = "END_TIME", nullable = true)
  var endTime: String? = null,

  @Column(name = "INSTALLATION_DATE", nullable = true)
  var installationDate: String? = null,

  @Column(name = "PURPOSE", nullable = true)
  var purpose: String? = null,

  @Column(name = "ADDRESS_LINE_1", nullable = true)
  var addressLine1: String? = null,

  @Column(name = "CITY", nullable = true)
  var city: String? = null,

  @Column(name = "POSTCODE", nullable = true)
  var postcode: String? = null,

  @ManyToOne
  @JoinColumn(name = "MANDATORY_ATTENDANCE_ID", updatable = false, insertable = false)
  private val conditions: MandatoryAttendanceConditions? = null,
)
