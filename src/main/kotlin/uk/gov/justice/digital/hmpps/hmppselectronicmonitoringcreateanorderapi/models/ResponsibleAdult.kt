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
@Table(name = "RESPONSIBLE_ADULT")
data class ResponsibleAdult(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "VERSION_ID", nullable = false, unique = true)
  val versionId: UUID,

  @Column(name = "FULL_NAME", nullable = true)
  var fullName: String? = null,

  @Column(name = "RELATIONSHIP", nullable = true)
  var relationship: String? = null,

  @Column(name = "OTHER_RELATIONSHIP_DETAILS", nullable = true)
  var otherRelationshipDetails: String? = null,

  @Column(name = "CONTACT_NUMBER", nullable = true)
  var contactNumber: String? = null,

  @Schema(hidden = true)
  @OneToOne
  @JoinColumn(name = "VERSION_ID", updatable = false, insertable = false)
  private val version: OrderVersion? = null,
)
