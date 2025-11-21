package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import net.minidev.json.annotate.JsonIgnore
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.interfaces.MonitoringCondition
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(name = "CURFEW")
data class CurfewConditions(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "VERSION_ID", nullable = false, unique = true)
  val versionId: UUID,

  @Column(name = "START_DATE", nullable = true)
  override var startDate: ZonedDateTime? = null,

  @Column(name = "END_DATE", nullable = true)
  override var endDate: ZonedDateTime? = null,

  @Column(name = "CURFEW_ADDRESS", nullable = true)
  var curfewAddress: String? = null,

  @Column(name = "CURFEW_ADDITIONAL_DETAILS", nullable = true)
  var curfewAdditionalDetails: String? = "",

  @JsonIgnore
  var curfewDescription: String? = "",

  @Schema(hidden = true)
  @OneToOne
  @JoinColumn(name = "VERSION_ID", updatable = false, insertable = false)
  private val version: OrderVersion? = null,

) : MonitoringCondition
