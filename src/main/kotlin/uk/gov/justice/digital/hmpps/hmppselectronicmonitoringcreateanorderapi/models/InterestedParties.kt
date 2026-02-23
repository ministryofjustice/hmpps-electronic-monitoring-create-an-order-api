package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "INTERESTED_PARTIES")
data class InterestedParties(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "VERSION_ID", nullable = false, unique = true)
  val versionId: UUID,

  @Column(name = "RESPONSIBLE_OFFICER_NAME", nullable = true)
  var responsibleOfficerName: String?,

  @Column(name = "RESPONSIBLE_OFFICER_PHONE_NUMBER", nullable = true)
  var responsibleOfficerPhoneNumber: String?,

  @Column(name = "RESPONSIBLE_ORGANISATION", nullable = true)
  var responsibleOrganisation: String?,

  @Column(name = "RESPONSIBLE_ORGANISATION_REGION", nullable = true)
  var responsibleOrganisationRegion: String?,

  @Column(name = "RESPONSIBLE_ORGANISATION_EMAIL", nullable = true)
  var responsibleOrganisationEmail: String?,

  @Column(name = "NOTIFYING_ORGANISATION", nullable = true)
  var notifyingOrganisation: String? = null,

  @Column(name = "NOTIFYING_ORGANISATION_NAME", nullable = true)
  var notifyingOrganisationName: String? = null,

  @Column(name = "NOTIFYING_ORGANISATION_EMAIL", nullable = true)
  var notifyingOrganisationEmail: String? = null,

  @Schema(hidden = true)
  @OneToOne
  @JoinColumn(name = "VERSION_ID", updatable = false, insertable = false)
  private val version: OrderVersion? = null,
) {
  val isValid: Boolean
    get() = (
      !notifyingOrganisation.isNullOrBlank()
      )
}
