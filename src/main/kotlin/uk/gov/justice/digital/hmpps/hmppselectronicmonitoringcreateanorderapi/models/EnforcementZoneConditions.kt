package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
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

  @Column(name = "VERSION_ID", nullable = false)
  val versionId: UUID,

  @Column(name = "ZONE_TYPE", nullable = false)
  @Enumerated(EnumType.STRING)
  var zoneType: EnforcementZoneType? = null,

  @Column(name = "START_DATE", nullable = false)
  var startDate: ZonedDateTime? = null,

  @Column(name = "END_DATE", nullable = true)
  var endDate: ZonedDateTime? = null,

  @Column(name = "NAME", nullable = true)
  var name: String? = null,

  @Column(name = "DESCRIPTION", nullable = true)
  var description: String? = null,

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

  @Schema(hidden = true)
  @ManyToOne(optional = true)
  @JoinColumn(name = "VERSION_ID", updatable = false, insertable = false)
  private val version: OrderVersion? = null,
)
