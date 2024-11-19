package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(name = "CURFEW")
data class CurfewConditions(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "ORDER_ID", nullable = false, unique = true)
  val orderId: UUID,

  @field:NotNull(message = "Enter curfew start day")
  @Column(name = "START_DATE", nullable = true)
  var startDate: ZonedDateTime? = null,

  @field:Future(message = "Curfew end day must be in the future")
  @Column(name = "END_DATE", nullable = true)
  var endDate: ZonedDateTime? = null,

  @field:NotNull(message = "Curfew address is required")
  @Column(name = "CURFEW_ADDRESS", nullable = true)
  @field:Size(min = 1, message = "Curfew address is required")
  var curfewAddress: String? = null,

  @Schema(hidden = true)
  @OneToOne
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
