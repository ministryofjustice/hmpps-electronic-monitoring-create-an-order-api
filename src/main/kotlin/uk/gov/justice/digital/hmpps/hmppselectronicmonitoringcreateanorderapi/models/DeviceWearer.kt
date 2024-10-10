package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.Past
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "DEVICE_WEARER")
data class DeviceWearer(

  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "ORDER_ID", nullable = false, unique = true)
  val orderId: UUID,

  @Column(name = "NOMIS_ID", nullable = true)
  var nomisId: String? = null,

  @Column(name = "PNC_ID", nullable = true)
  var pncId: String? = null,

  @Column(name = "DELIUS_ID", nullable = true)
  var deliusId: String? = null,

  @Column(name = "PRISON_NUMBER", nullable = true)
  var prisonNumber: String? = null,

  @Column(name = "FIRST_NAME", nullable = true)
  var firstName: String? = null,

  @Column(name = "LAST_NAME", nullable = true)
  var lastName: String? = null,

  @Column(name = "ALIAS", nullable = true)
  var alias: String? = null,

  @Column(name = "ADULT_AT_TIME_OF_INSTALLATION", nullable = true)
  var adultAtTimeOfInstallation: Boolean? = null,

  @Column(name = "SEX", nullable = true)
  var sex: String? = null,

  @Column(name = "GENDER", nullable = true)
  var gender: String? = null,

  @Column(name = "DATE_OF_BIRTH", nullable = true)
  @field:Past(message = "Date of birth must be in the past")
  var dateOfBirth: ZonedDateTime? = null,

  @Column(name = "DISABILITIES", nullable = true)
  var disabilities: String? = null,

  @OneToOne
  @JoinColumn(name = "ORDER_ID", updatable = false, insertable = false)
  private val order: Order? = null,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "deviceWearer", orphanRemoval = true)
  var alternativeContactDetails: AlternativeContractDetails? = null,
)
