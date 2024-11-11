package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.converter.ArrayToStringConverter
import java.util.*

@Entity
@Table(name = "INSTALLATION_AND_RISK")
data class InstallationAndRisk(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "ORDER_ID", nullable = false, unique = true)
  val orderId: UUID? = null,

  @Column(name = "OFFENCE", nullable = true)
  var offence: String? = "",

  @Convert(converter = ArrayToStringConverter::class)
  @Column(name = "RISK_CATEGORY", nullable = true)
  var riskCategory: Array<String>? = null,

  @Column(name = "RISK_DETAILS", nullable = true)
  var riskDetails: String? = "",

  @Column(name = "MAPPA_LEVEL", nullable = true)
  var mappaLevel: String? = "",

  @Column(name = "MAPPA_CASE_TYPE", nullable = true)
  var mappaCaseType: String? = "",

  @OneToOne
  @JoinColumn(name = "ORDER_ID", updatable = false, insertable = false)
  private val order: Order? = null,
)
