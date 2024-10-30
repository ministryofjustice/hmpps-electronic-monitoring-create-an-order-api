package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.*

@Entity
@Table(name = "MANDATORY_ATTENDANCE")
data class MandatoryAttendanceConditions(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "ORDER_ID", nullable = false, unique = true)
  val orderId: UUID,

  @Column(name = "START_DATE", nullable = true)
  var startDate: LocalDate? = null,

  @Column(name = "END_DATE", nullable = true)
  var endDate: LocalDate? = null,

  @Column(name = "PURPOSE", nullable = true)
  var purpose: String? = null,

  @Column(name = "APPOINTMENT_DAY", nullable = true)
  var appointmentDay: String? = null,

  @Column(name = "START_TIME", nullable = true)
  var startTime: String? = null,

  @Column(name = "END_TIME", nullable = true)
  var endTime: String? = null,

  @Column(name = "ADDRESS_LINE_1", nullable = true)
  var addressLine1: String? = null,

  @Column(name = "ADDRESS_LINE_2", nullable = true)
  var addressLine2: String? = null,

  @Column(name = "ADDRESS_LINE_3", nullable = true)
  var addressLine3: String? = null,

  @Column(name = "ADDRESS_LINE_4", nullable = true)
  var addressLine4: String? = null,

  @Column(name = "POSTCODE", nullable = true)
  var postcode: String? = null,

  @ManyToOne(optional = true)
  @JoinColumn(name = "ORDER_ID", updatable = false, insertable = false)
  private val order: Order? = null,
) {
  val isValid: Boolean
    get() = (
      startDate != null &&
        !purpose.isNullOrEmpty() &&
        !appointmentDay.isNullOrEmpty() &&
        startTime != null &&
        endTime != null &&
        !addressLine1.isNullOrEmpty() &&
        !addressLine2.isNullOrEmpty() &&
        !postcode.isNullOrEmpty()
      )
}
