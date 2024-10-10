package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "DEVICE_WEARER_ADDRESS_INFORMATION")
data class DeviceWearerAddressInformation(

  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "ORDER_ID", nullable = false)
  val orderId: UUID,

  @Column(name = "NO_FIXED_ABODE", nullable = true)
  var noFixedAbode: Boolean? = null,

  @Column(name = "PRIMARY_IS_INSTALLATION_ADDRESS", nullable = true)
  var primaryIsInstallationAddress: Boolean? = null,

  @OneToOne(cascade = [CascadeType.ALL])
  var primaryAddress: Address? = null,

  @OneToOne(cascade = [CascadeType.ALL])
  var secondaryAddress: Address? = null,

  @OneToOne(cascade = [CascadeType.ALL])
  var tertiaryAddress: Address? = null,

  @OneToOne(cascade = [CascadeType.ALL])
  var installationAddress: Address? = null,

  @OneToOne
  @JoinColumn(name = "ORDER_ID", updatable = false, insertable = false)
  private val order: Order? = null,
)
