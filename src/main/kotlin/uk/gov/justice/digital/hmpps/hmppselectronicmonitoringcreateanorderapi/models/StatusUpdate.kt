package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProcessingStatus
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "STATUS_UPDATE")
data class StatusUpdate(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "VERSION_ID", nullable = false, unique = false)
  val versionId: UUID,

  @Column(name = "STATUS", nullable = false, unique = false)
  @Enumerated(EnumType.STRING)
  val status: ProcessingStatus,

  @Column(name = "DATE_TIME_OF_STATUS_CHANGE", nullable = false, unique = false)
  val datetimeOfStatusChange: ZonedDateTime,

  @OneToMany(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "statusUpdate", orphanRemoval = true)
  var statusUpdateReasons: MutableList<StatusUpdateReason> = mutableListOf(),

  @Schema(hidden = true)
  @ManyToOne
  @JoinColumn(name = "VERSION_ID", updatable = false, insertable = false)
  private val version: OrderVersion? = null,
)
