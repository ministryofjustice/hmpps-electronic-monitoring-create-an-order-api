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

  @Column(name = "NAME", nullable = true)
  var name: String? = null,

  @Column(name = "PHONE_NUMBER", nullable = true)
  var phoneNumber: String? = null,

  @Column(name = "ORGANISATION", nullable = true)
  var organisation: String? = null,

  @Column(name = "ORGANISATION_REGION", nullable = true)
  var organisationRegion: String? = null,

  @Column(name = "ORGANISATION_POST_CODE", nullable = true)
  var organisationPostCode: String? = null,

  @Column(name = "ORGANISATION_PHONE_NUMBER", nullable = true)
  var organisationPhoneNumber: String? = null,

  @Column(name = "ORGANISATION_EMAIL", nullable = true)
  var organisationEmail: String? = null,

  @OneToOne
  @JoinColumn(name = "ORDER_ID", updatable = false, insertable = false)
  private val order: Order? = null,
)
