package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(name = "CURFEW_RELEASE_DATE")
data class CurfewReleaseDateConditions(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "ORDER_ID", nullable = false, unique = true)
  val orderId: UUID,

  @field:NotNull(message = "Enter curfew release date")
  @field:Future(message = "Curfew release date must be in the future")
  @Column(name = "RELEASE_DATE", nullable = true)
  var releaseDate: ZonedDateTime? = null,

  @field:NotNull(message = "Enter start time")
  @field:Size(min = 1, message = "Enter start time")
  @Column(name = "START_TIME", nullable = true)
  var startTime: String? = null,

  @field:NotNull(message = "Enter end time")
  @field:Size(min = 1, message = "Enter end time")
  @Column(name = "END_TIME", nullable = true)
  var endTime: String? = null,

  @Enumerated(EnumType.STRING)
  @field:NotNull(message = "Curfew address is required")
  @Column(name = "CURFEW_ADDRESS", nullable = true)
  var curfewAddress: AddressType? = null,

  @OneToOne
  @JoinColumn(name = "ORDER_ID", updatable = false, insertable = false)
  private val order: Order? = null,
)
