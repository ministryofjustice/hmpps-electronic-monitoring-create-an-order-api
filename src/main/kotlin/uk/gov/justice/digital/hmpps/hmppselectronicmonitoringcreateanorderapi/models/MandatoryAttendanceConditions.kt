package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.util.*

@Entity
@Table(name = "MANDATORY_ATTENDANCE")
data class MandatoryAttendanceConditions(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "ORDER_ID", nullable = false, unique = false)
  val orderId: UUID,

  @Column(name = "START_DATE", nullable = false)
  @field:NotNull(
    message = "Please enter a mandatory attendance monitoring start date date to continue to the next page",
  )
  var startDate: LocalDate? = null,

  @Column(name = "END_DATE", nullable = true)
  var endDate: LocalDate? = null,

  @Column(name = "PURPOSE", nullable = false)
  @field:NotNull(message = "Please provide appointment details to continue to the next page")
  var purpose: String? = null,

  @Column(name = "APPOINTMENT_DAY", nullable = false)
  @field:NotNull(message = "Please provide appointment day to continue to the next page")
  var appointmentDay: String? = null,

  @Column(name = "START_TIME", nullable = false)
  @field:NotNull(message = "Please provide appointment start time to continue to the next page")
  var startTime: String? = null,

  @Column(name = "END_TIME", nullable = false)
  @field:NotNull(message = "Please provide appointment end time to continue to the next page")
  var endTime: String? = null,

  @Column(name = "ADDRESS_LINE_1", nullable = false)
  @field:NotNull(message = "Please provide an appointment location")
  var addressLine1: String? = null,

  @Column(name = "ADDRESS_LINE_2", nullable = false)
  @field:NotNull(message = "Please provide an appointment location")
  var addressLine2: String? = null,

  @Column(name = "ADDRESS_LINE_3", nullable = true)
  var addressLine3: String? = null,

  @Column(name = "ADDRESS_LINE_4", nullable = true)
  var addressLine4: String? = null,

  @Column(name = "POSTCODE", nullable = true)
  @field:NotNull(message = "Please provide a postcode")
  var postcode: String? = null,

  @Schema(hidden = true)
  @ManyToOne(optional = true)
  @JoinColumn(name = "ORDER_ID", updatable = false, insertable = false)
  private val order: Order? = null,
)
