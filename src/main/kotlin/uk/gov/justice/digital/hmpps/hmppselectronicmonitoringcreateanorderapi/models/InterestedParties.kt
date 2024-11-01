package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidPhoneNumber
import java.util.*

@Entity
@Table(name = "INTERESTED_PARTIES")
data class InterestedParties(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),
  @Column(name = "ORDER_ID", nullable = false, unique = true)
  val orderId: UUID,

  @Column(name = "RESPONSIBLE_OFFICER_NAME", nullable = false)
  var responsibleOfficerName: String,

  @Column(name = "RESPONSIBLE_OFFICER_PHONE_NUMBER", nullable = true)
  @field:ValidPhoneNumber
  var responsibleOfficerPhoneNumber: String?,

  @Column(name = "RESPONSIBLE_ORGANISATION", nullable = false)
  var responsibleOrganisation: String,

  @Column(name = "RESPONSIBLE_ORGANISATION_REGION", nullable = false)
  var responsibleOrganisationRegion: String,

  @Column(name = "RESPONSIBLE_ORGANISATION_PHONE_NUMBER", nullable = true)
  @field:ValidPhoneNumber
  var responsibleOrganisationPhoneNumber: String?,

  @Column(name = "RESPONSIBLE_ORGANISATION_EMAIL", nullable = false)
  var responsibleOrganisationEmail: String,

  @Column(name = "NOTIFYING_ORGANISATION", nullable = false)
  var notifyingOrganisation: String,

  @Column(name = "NOTIFYING_ORGANISATION_EMAIL", nullable = false)
  var notifyingOrganisationEmail: String,

  @OneToOne
  @JoinColumn(name = "ORDER_ID", updatable = false, insertable = false)
  private val order: Order? = null,

  @OneToOne
  @JoinColumn(name = "RESPONSIBLE_ORGANISATION_ADDRESS_ID")
  val responsibleOrganisationAddress: Address,
)
