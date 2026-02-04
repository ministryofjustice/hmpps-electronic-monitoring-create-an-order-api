package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "ORDER_PARAMETERS")
data class OrderParameters(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "VERSION_ID", nullable = false, unique = true)
  val versionId: UUID,

  @Column(name = "HAVE_PHOTO", nullable = true)
  var havePhoto: Boolean? = null,

  @Column(name = "HAVE_COURT_ORDER", nullable = true)
  var haveCourtOrder: Boolean? = null,

  @Column(name = "HAVE_GRANT_OF_BAIL", nullable = true)
  var haveGrantOfBail: Boolean? = null,

  @Column(name = "IS_MAPPA", nullable = true)
  var isMappa: Boolean? = null,

  @Schema(hidden = true)
  @OneToOne
  @JoinColumn(name = "VERSION_ID", updatable = false, insertable = false)
  private val version: OrderVersion? = null,
)
