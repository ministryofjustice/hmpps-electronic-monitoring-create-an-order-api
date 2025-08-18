package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Future
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(name = "ALCOHOL_MONITORING")
data class AlcoholMonitoringConditions(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "VERSION_ID", nullable = false, unique = true)
  val versionId: UUID,

  @Enumerated(EnumType.STRING)
  @Column(name = "MONITORING_TYPE", nullable = true)
  var monitoringType: AlcoholMonitoringType? = null,

  @Column(name = "START_DATE", nullable = true)
  var startDate: ZonedDateTime? = null,

  @field:Future(message = ValidationErrors.AlcoholMonitoring.END_DATE_MUST_BE_IN_FUTURE)
  @Column(name = "END_DATE", nullable = true)
  var endDate: ZonedDateTime? = null,

  @OneToOne(optional = true)
  @JoinColumn(name = "VERSION_ID", updatable = false, insertable = false)
  private val version: OrderVersion? = null,

) {
  @AssertTrue(message = ValidationErrors.AlcoholMonitoring.END_DATE_MUST_BE_AFTER_START_DATE)
  fun isEndDate(): Boolean {
    if (this.endDate != null && this.startDate != null) {
      return this.endDate!! > this.startDate
    }
    return true
  }
}
