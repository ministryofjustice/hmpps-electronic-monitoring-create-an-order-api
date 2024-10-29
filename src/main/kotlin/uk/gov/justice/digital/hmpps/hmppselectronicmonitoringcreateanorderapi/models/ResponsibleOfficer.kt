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
data class ResponsibleOfficer(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),
  @Column(name = "ORDER_ID", nullable = false, unique = true)
  val orderId: UUID,

  @Column(name = "RESPONSIBLE_OFFICER_NAME", nullable = true)
  var responsibleOfficerName: String? = null,

  @Column(name = "RESPONSIBLE_OFFICER_PHONE_NUMBER", nullable = true)
  var responsibleOfficerPhoneNumber: String? = null,

  @Column(name = "RESPONSIBLE_ORGANISATION", nullable = true)
  var responsibleOrganisation: String? = null,

  @Column(name = "RESPONSIBLE_ORGANISATION_REGION", nullable = true)
  var responsibleOrganisationRegion: String? = null,

  @Column(name = "RESPONSIBLE_ORGANISATION_ADDRESS_ID", nullable = true)
  var responsibleOrganisationAddressId: UUID? = null,

  @Column(name = "RESPONSIBLE_ORGANISATION_PHONE_NUMBER", nullable = true)
  var responsibleOrganisationPhoneNumber: String? = null,

  @Column(name = "RESPONSIBLE_ORGANISATION_EMAIL", nullable = true)
  var responsibleOrganisationEmail: String? = null,

  @Column(name = "NOTIFYING_ORGANISATION_EMAIL", nullable = true)
  var notifyingOrganisationEmail: String? = null,

  @OneToOne
  @JoinColumn(name = "ORDER_ID", updatable = false, insertable = false)
  private val order: Order? = null,

  @OneToOne(optional = true)
  @JoinColumn(
    name = "RESPONSIBLE_ORGANISATION_ADDRESS_ID",
    updatable = false,
    insertable = false,
    nullable = true,
  )
  private val responsibleOrganisationAddress: Address,
)
