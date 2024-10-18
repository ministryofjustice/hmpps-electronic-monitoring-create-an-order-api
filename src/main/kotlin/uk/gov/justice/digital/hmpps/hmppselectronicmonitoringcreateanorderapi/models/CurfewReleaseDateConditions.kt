package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
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

  @Column(name = "RELEASE_DATE", nullable = true)
  var releaseDate: ZonedDateTime? = null,

  @Column(name = "START_TIME", nullable = true)
  var startTime: String? = null,

  @Column(name = "END_TIME", nullable = true)
  var endTime: String? = null,

  @Column(name = "CURFEW_ADDRESS", nullable = true)
  var curfewAddress: UUID? = null,

  @OneToOne
  @JoinColumn(name = "ORDER_ID", updatable = false, insertable = false)
  private val order: Order? = null,
)
