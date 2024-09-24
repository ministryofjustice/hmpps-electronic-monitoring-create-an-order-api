package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "DEVICE_WEARER")
data class DeviceWearer(

  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "ORDER_ID", nullable = false, unique = true)
  val orderId: UUID,

  @Column(name = "FIRST_NAME", nullable = true)
  var firstName: String? = null,

  @Column(name = "LAST_NAME", nullable = true)
  var lastName: String? = null,

  @Column(name = "GENDER", nullable = true)
  var gender: String? = null,

  @Column(name = "DATE_OF_BIRTH", nullable = true)
  var dateOfBirth: LocalDate? = null,

  @OneToOne
  @JoinColumn(name = "ORDER_ID", updatable = false, insertable = false)
  private val orderForm: OrderForm? = null,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "deviceWearer", orphanRemoval = true)
  var deviceWearerContactDetails: DeviceWearerContactDetails? = null,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "deviceWearer", orphanRemoval = true)
  var responsibleAdult: ResponsibleAdult? = null,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "deviceWearer", orphanRemoval = true)
  var alternativeContactDetails: AlternativeContractDetails? = null,

  @OneToMany(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "deviceWearer", orphanRemoval = true)
  var deviceWearerAddresses: MutableList<DeviceWearerAddress> = mutableListOf(),
)
