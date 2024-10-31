package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "RESPONSIBLE_OFFICER")
data class InterestedParties(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),
  @Column(name = "ORDER_ID", nullable = false, unique = true)
  val orderId: UUID,

  @Column(name = "RESPONSIBLE_OFFICER_NAME", nullable = false)
  var responsibleOfficerName: String,

  @Column(name = "RESPONSIBLE_OFFICER_PHONE_NUMBER", nullable = false)
  var responsibleOfficerPhoneNumber: String,

  @Column(name = "RESPONSIBLE_ORGANISATION", nullable = false)
  var responsibleOrganisation: String,

  @Column(name = "RESPONSIBLE_ORGANISATION_REGION", nullable = false)
  var responsibleOrganisationRegion: String,

  @Column(name = "RESPONSIBLE_ORGANISATION_ADDRESS_ID", nullable = false)
  var responsibleOrganisationAddressId: UUID,

  @Column(name = "RESPONSIBLE_ORGANISATION_PHONE_NUMBER", nullable = false)
  var responsibleOrganisationPhoneNumber: String,

  @Column(name = "RESPONSIBLE_ORGANISATION_EMAIL", nullable = false)
  var responsibleOrganisationEmail: String,

  @Column(name = "NOTIFYING_ORGANISATION", nullable = false)
  var notifyingOrganisation: String,

  @Column(name = "NOTIFYING_ORGANISATION_EMAIL", nullable = false)
  var notifyingOrganisationEmail: String,

  @OneToOne
  @JoinColumn(name = "ORDER_ID", updatable = false, insertable = false)
  private val order: Order? = null,
)
