package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "DAPO")
data class Dapo(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "VERSION_ID", nullable = false, unique = false)
  val versionId: UUID,

  @Column(name = "CLAUSE", nullable = true, length = 20)
  val clause: String?,

  @Column(name = "DATE", nullable = true)
  val date: ZonedDateTime?,

  @Schema(hidden = true)
  @ManyToOne
  @JoinColumn(name = "VERSION_ID", updatable = false, insertable = false)
  private val version: OrderVersion? = null,
)
