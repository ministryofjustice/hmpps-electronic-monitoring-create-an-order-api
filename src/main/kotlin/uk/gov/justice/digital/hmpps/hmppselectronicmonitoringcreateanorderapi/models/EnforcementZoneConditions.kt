package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import net.minidev.json.annotate.JsonIgnore
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(name = "ENFORCEMENT_ZONE")
data class EnforcementZoneConditions(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "ORDER_ID", nullable = false, unique = false)
  val orderId: UUID,

  @Column(name = "ZONE_TYPE", nullable = false)
  @Enumerated(EnumType.STRING)
  @field:NotNull(message = "Enforcement zone type is required")
  var zoneType: EnforcementZoneType? = null,

  @field:NotNull(message = "Enforcement zone start date is required")
  @Column(name = "START_DATE", nullable = false)
  var startDate: ZonedDateTime? = null,

  @field:Future(message = "Enforcement zone end date must be in the future")
  @Column(name = "END_DATE", nullable = true)
  var endDate: ZonedDateTime? = null,

  @field:NotNull(message = "Enforcement zone description is required")
  @field:Size(min = 1, message = "Enforcement zone description is required")
  @Column(name = "DESCRIPTION", nullable = true)
  var description: String? = null,

  @field:NotNull(message = "Enforcement zone duration is required")
  @field:Size(min = 1, message = "Enforcement zone duration is required")
  @Column(name = "DURATION", nullable = true)
  var duration: String? = null,

  @Column(name = "FILE_NAME", nullable = true)
  var fileName: String? = null,

  @Column(name = "FILE_ID", nullable = true)
  var fileId: UUID? = null,

  @Column(name = "ZONE_ID", nullable = true)
  var zoneId: Int? = null,

  @JsonIgnore
  var zoneLocation: String? = "",

  @ManyToOne(optional = true)
  @JoinColumn(name = "ORDER_ID", updatable = false, insertable = false)
  private val order: Order? = null,
) {
  @AssertTrue(message = "End date must be after start date")
  fun isEndDate(): Boolean {
    if (this.endDate != null && this.startDate != null) {
      return this.endDate!! > this.startDate
    }
    return true
  }
}
