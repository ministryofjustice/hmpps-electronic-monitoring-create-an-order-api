package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "STATUS_UPDATE_REASON")
data class StatusUpdateReason(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "STATUS_UPDATE_ID", nullable = false, unique = false)
  val statusUpdateId: UUID,

  @Column(name = "SECTION", nullable = false, unique = false)
  val section: String,

  @Column(name = "DETAILS", nullable = true, unique = false)
  val details: String?,

  @Schema(hidden = true)
  @ManyToOne
  @JoinColumn(name = "STATUS_UPDATE_ID", updatable = false, insertable = false)
  private val statusUpdate: StatusUpdate? = null,
)
