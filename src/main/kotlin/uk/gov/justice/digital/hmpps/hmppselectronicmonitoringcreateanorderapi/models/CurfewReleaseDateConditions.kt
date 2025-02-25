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
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(name = "CURFEW_RELEASE_DATE")
data class CurfewReleaseDateConditions(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "VERSION_ID", nullable = false, unique = true)
  val versionId: UUID,

  @field:NotNull(message = ValidationErrors.CurfewReleaseDateConditions.START_DATE_REQUIRED)
  @Column(name = "RELEASE_DATE", nullable = true)
  var releaseDate: ZonedDateTime? = null,

  @field:NotNull(message = ValidationErrors.CurfewReleaseDateConditions.START_TIME_REQUIRED)
  @field:Size(min = 1, message = ValidationErrors.CurfewReleaseDateConditions.START_TIME_REQUIRED)
  @Column(name = "START_TIME", nullable = true)
  var startTime: String? = null,

  @field:NotNull(message = ValidationErrors.CurfewReleaseDateConditions.END_TIME_REQUIRED)
  @field:Size(min = 1, message = ValidationErrors.CurfewReleaseDateConditions.END_TIME_REQUIRED)
  @Column(name = "END_TIME", nullable = true)
  var endTime: String? = null,

  @Enumerated(EnumType.STRING)
  @field:NotNull(message = ValidationErrors.CurfewReleaseDateConditions.ADDRESS_REQUIRED)
  @Column(name = "CURFEW_ADDRESS", nullable = true)
  var curfewAddress: AddressType? = null,

  @Schema(hidden = true)
  @OneToOne
  @JoinColumn(name = "VERSION_ID", updatable = false, insertable = false)
  private val version: OrderVersion? = null,
)
