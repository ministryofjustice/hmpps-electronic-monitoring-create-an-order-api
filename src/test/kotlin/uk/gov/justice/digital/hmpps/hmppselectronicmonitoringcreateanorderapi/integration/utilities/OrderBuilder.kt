package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.utilities

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.*
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import java.util.*

class OrderBuilder(
) {
  var orderId: UUID = UUID.randomUUID()
  var noFixedAddress: Boolean = false
  var orderStatus: OrderStatus = OrderStatus.IN_PROGRESS
  var documents: MutableList<AdditionalDocument> = mutableListOf()

  var initialVersionId: UUID = UUID.randomUUID()
  var initialRequestType: RequestType = RequestType.REQUEST

  var deviceWearer: DeviceWearer? = null
  var responsibleAdult: ResponsibleAdult? = null
  var installationAndRisk: InstallationAndRisk? = null
  var contactDetails: ContactDetails? = null
  var monitoringConditions: MonitoringConditions? = null
  var addresses: MutableList<Address> = mutableListOf()

  fun deviceWearer(block: DeviceWearerBuilder.() -> Unit) = apply {
    val builder = DeviceWearerBuilder(this.initialVersionId, false)
    builder.block()
    this.deviceWearer = builder.build()
  }

  fun responsibleAdult(block: ResponsibleAdultBuilder.() -> Unit) = apply {
    val builder = ResponsibleAdultBuilder(this.initialVersionId)
    builder.block()
    this.responsibleAdult = builder.build()
  }

  fun addAddress(block: AddressBuilder.() -> Unit) = apply {
    val builder = AddressBuilder(this.initialVersionId)
    builder.block()
    this.addresses.add(builder.build())
  }

  fun installationAndRisk(block: InstallationAndRiskBuilder.() -> Unit) = apply {
    val builder = InstallationAndRiskBuilder(this.initialVersionId)
    builder.block()
    this.installationAndRisk = builder.build()
  }

  fun contactDetails(block: ContactDetailsBuilder.() -> Unit) = apply {
    val builder = ContactDetailsBuilder(this.initialVersionId)
    builder.block()
    this.contactDetails = builder.build()
  }

  fun monitoringConditions(block: MonitoringConditionsBuilder.() -> Unit) = apply {
    val builder = MonitoringConditionsBuilder(this.initialVersionId)
    builder.block()
    this.monitoringConditions = builder.build()
  }

  fun build(): Order {
    val order = Order(
      id = this.orderId,
      versions = mutableListOf(
        OrderVersion(
          id = this.initialVersionId,
          username = "AUTH_ADM",
          status = OrderStatus.IN_PROGRESS,
          type = this.initialRequestType,
          orderId = this.orderId,
          dataDictionaryVersion = DataDictionaryVersion.DDV4,
        ),
      ),
    )
    order.deviceWearer = deviceWearer
    order.deviceWearerResponsibleAdult = responsibleAdult
    order.installationAndRisk = installationAndRisk
    order.contactDetails = contactDetails
    order.monitoringConditions = monitoringConditions
    order.addresses.addAll(addresses)

    if (!noFixedAddress && addresses.none { it.addressType == AddressType.PRIMARY || it.addressType == AddressType.SECONDARY }) {
      addresses.add(
        Address(
          versionId = initialVersionId,
          addressLine1 = "20 Somewhere Street",
          addressLine2 = "Nowhere City",
          addressLine3 = "Random County",
          addressLine4 = "United Kingdom",
          postcode = "SW11 1NC",
          addressType = AddressType.PRIMARY,
        ),
      )
      addresses.add(
        Address(
          versionId = initialVersionId,
          addressLine1 = "22 Somewhere Street",
          addressLine2 = "Nowhere City",
          addressLine3 = "Random County",
          addressLine4 = "United Kingdom",
          postcode = "SW11 1NC",
          addressType = AddressType.SECONDARY,
        ),
      )
    }

    if (addresses.none { it.addressType == AddressType.INSTALLATION }) {
      addresses.add(
        Address(
          versionId = initialVersionId,
          addressLine1 = "24 Somewhere Street",
          addressLine2 = "Nowhere City",
          addressLine3 = "Random County",
          addressLine4 = "United Kingdom",
          postcode = "SW11 1NC",
          addressType = AddressType.INSTALLATION,
        ),
      )
    }

    return order
  }
}

