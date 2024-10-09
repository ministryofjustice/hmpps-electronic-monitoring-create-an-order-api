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
@Table(name = "ENFORCEMENT_ZONE")
data class EnforcementZoneConditions(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "ORDER_ID", nullable = false, unique = true)
  val orderId: UUID,

  @Column(name = "ZONE_TYPE", nullable = true)
  var zoneType: String? = null,

  @Column(name = "START_DATE", nullable = true)
  var startDate: LocalDate? = null,

  @Column(name = "END_DATE", nullable = true)
  var endDate: LocalDate? = null,

  @Column(name = "DESCRIPTION", nullable = true)
  var description: String? = null,

  @Column(name = "DURATION", nullable = true)
  var duration: String? = null,

  @Column(name = "FILE_NAME", nullable = true)
  var fileName: String? = null,

  @Column(name = "FILE_ID", nullable = true)
  var fileId: UUID? = null,

  @Column(name = "ZONE_ID", nullable = true)
  var zoneId: Number? = null,

  @ManyToOne(optional = true)
  @JoinColumn(name = "ORDER_ID", updatable = false, insertable = false)
  private val order: OrderForm? = null,
)
