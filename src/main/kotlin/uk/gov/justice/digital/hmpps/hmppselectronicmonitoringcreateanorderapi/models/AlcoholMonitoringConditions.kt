package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringInstallationLocationType
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

  @Column(name = "END_DATE", nullable = true)
  var endDate: ZonedDateTime? = null,

  @Enumerated(EnumType.STRING)
  @Column(name = "INSTALLATION_LOCATION", nullable = true)
  var installationLocation: AlcoholMonitoringInstallationLocationType? = null,

  @Column(name = "INSTALLATION_ADDRESS_ID", nullable = true, unique = true)
  var installationAddressId: UUID? = null,

  @Column(name = "PRISON_NAME", nullable = true)
  var prisonName: String? = null,

  @Column(name = "PROBATION_OFFICE_NAME", nullable = true)
  var probationOfficeName: String? = null,

  @OneToOne(optional = true)
  @JoinColumn(name = "VERSION_ID", updatable = false, insertable = false)
  private val version: OrderVersion? = null,

  @Schema(hidden = true)
  @OneToOne(optional = true)
  @JoinColumn(
    name = "INSTALLATION_ADDRESS_ID",
    updatable = false,
    insertable = false,
    nullable = true,
  )
  private val address: Address? = null,
)
