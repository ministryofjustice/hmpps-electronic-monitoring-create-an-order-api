package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceWearerAddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceWearerAddressUsage
import java.util.*

@Entity
@Table(name = "DEVICE_WEARER_ADDRESS")
data class DeviceWearerAddress(

  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "ORDER_ID", nullable = false)
  val orderId: UUID,

  @Column(name = "ADDRESS_LINE_1", nullable = true)
  var addressLine1: String? = null,

  @Column(name = "ADDRESS_LINE_2", nullable = true)
  var addressLine2: String? = null,

  @Column(name = "ADDRESS_LINE_3", nullable = true)
  var addressLine3: String? = null,

  @Column(name = "ADDRESS_LINE_4", nullable = true)
  var addressLine4: String? = null,

  @Column(name = "POSTCODE", nullable = true)
  var postcode: String? = null,

  @Enumerated(EnumType.STRING)
  @Column(name = "ADDRESSTYPE", nullable = true)
  var addressType: DeviceWearerAddressType,

  @Enumerated(EnumType.STRING)
  @Column(name = "ADDRESSUSAGE", nullable = true)
  var addressUsage: DeviceWearerAddressUsage? = DeviceWearerAddressUsage.NA,

  @ManyToOne
  @JoinColumn(name = "ORDER_ID", updatable = false, insertable = false)
  private val order: OrderForm? = null,
)
