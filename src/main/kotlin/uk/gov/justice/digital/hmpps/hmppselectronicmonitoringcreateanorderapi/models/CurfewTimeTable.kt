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
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import java.time.DayOfWeek
import java.util.*

@Entity
@Table(name = "CURFEW_TIMETABLE")
data class CurfewTimeTable(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "VERSION_ID", nullable = false)
  val versionId: UUID,

  @Enumerated(EnumType.STRING)
  @Column(name = "DAY_OF_WEEK", nullable = false)
  var dayOfWeek: DayOfWeek,

  @Column(name = "START_TIME", nullable = true)
  @field:NotNull(message = ValidationErrors.CurfewTimetable.START_TIME_REQUIRED)
  @field:Size(min = 1, message = ValidationErrors.CurfewTimetable.START_TIME_REQUIRED)
  var startTime: String? = null,

  @Column(name = "END_TIME", nullable = true)
  @field:NotNull(message = ValidationErrors.CurfewTimetable.END_TIME_REQUIRED)
  @field:Size(min = 1, message = ValidationErrors.CurfewTimetable.END_TIME_REQUIRED)
  var endTime: String? = null,

  @Column(name = "CURFEW_ADDRESS", nullable = true)
  @field:NotNull(message = ValidationErrors.CurfewTimetable.ADDRESS_REQUIRED)
  @field:Size(min = 1, message = ValidationErrors.CurfewTimetable.ADDRESS_REQUIRED)
  var curfewAddress: String? = null,

  @Schema(hidden = true)
  @ManyToOne(optional = true)
  @JoinColumn(name = "VERSION_ID", updatable = false, insertable = false)
  private val version: OrderVersion? = null,
)
