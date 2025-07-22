package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.utilities

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AdditionalDocument
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewReleaseDateConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewTimeTable
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.EnforcementZoneConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAndRisk
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InterestedParties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleAdult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import java.time.DayOfWeek
import java.util.*

class OrderBuilder {
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
  var curfewConditions: CurfewConditions? = null
  val curfewTimeTables: List<CurfewTimeTable> = listOf()
  var curfewReleaseDateConditions: CurfewReleaseDateConditions? = null
  var enforcementZoneConditions: MutableList<EnforcementZoneConditions> = mutableListOf()
  var monitoringConditionsAlcohol: AlcoholMonitoringConditions? = null
  var monitoringConditionsTrail: TrailMonitoringConditions? = null
  var interestedParties: InterestedParties? = null
  var probationDeliveryUnit: ProbationDeliveryUnit? = null

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

  fun curfewConditions(block: CurfewConditionsBuilder.() -> Unit) = apply {
    val builder = CurfewConditionsBuilder(this.initialVersionId)
    builder.block()
    this.curfewConditions = builder.build()
  }

  fun curfewReleaseDateConditions(block: CurfewReleaseDateConditionsBuilder.() -> Unit) = apply {
    val builder = CurfewReleaseDateConditionsBuilder(this.initialVersionId)
    builder.block()
    this.curfewReleaseDateConditions = builder.build()
  }

  fun addEnforcementZoneConditions(block: EnforcementZoneConditionsBuilder.() -> Unit) = apply {
    val builder = EnforcementZoneConditionsBuilder(this.initialVersionId)
    builder.block()
    this.enforcementZoneConditions.add(builder.build())
  }

  fun monitoringConditionsAlcohol(block: MonitoringConditionsAlcoholBuilder.() -> Unit) = apply {
    val builder = MonitoringConditionsAlcoholBuilder(this.initialVersionId)
    builder.block()
    this.monitoringConditionsAlcohol = builder.build()
  }

  fun monitoringConditionsTrail(block: MonitoringConditionsTrailBuilder.() -> Unit) = apply {
    val builder = MonitoringConditionsTrailBuilder(this.initialVersionId)
    builder.block()
    this.monitoringConditionsTrail = builder.build()
  }

  fun interestedParties(block: InterestedPartiesBuilder.() -> Unit) = apply {
    val builder = InterestedPartiesBuilder(this.initialVersionId)
    builder.block()
    this.interestedParties = builder.build()
  }

  fun probationDeliveryUnit(block: ProbationDeliveryUnitBuilder.() -> Unit) = apply {
    val builder = ProbationDeliveryUnitBuilder(this.initialVersionId)
    builder.block()
    this.probationDeliveryUnit = builder.build()
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
    order.additionalDocuments.addAll(documents)
    order.curfewConditions = curfewConditions
    order.curfewReleaseDateConditions = curfewReleaseDateConditions
    order.enforcementZoneConditions.addAll(enforcementZoneConditions)
    order.monitoringConditionsAlcohol = monitoringConditionsAlcohol
    order.monitoringConditionsTrail = monitoringConditionsTrail
    order.interestedParties = interestedParties
    order.probationDeliveryUnit = probationDeliveryUnit

    if (curfewTimeTables.isEmpty()) {
      order.curfewTimeTable.addAll(
        DayOfWeek.entries.map {
          CurfewTimeTable(
            versionId = initialVersionId,
            dayOfWeek = it,
            startTime = "17:00",
            endTime = "09:00",
            curfewAddress = "PRIMARY_ADDRESS",
          )
        },
      )
      order.curfewTimeTable.addAll(
        DayOfWeek.entries.map {
          CurfewTimeTable(
            versionId = initialVersionId,
            dayOfWeek = it,
            startTime = "17:00",
            endTime = "09:00",
            curfewAddress = "SECONDARY_ADDRESS",
          )
        },
      )
    }
    order.curfewTimeTable.addAll(curfewTimeTables)

    if (!noFixedAddress &&
      addresses.none { it.addressType == AddressType.PRIMARY || it.addressType == AddressType.SECONDARY }
    ) {
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

    order.enforcementZoneConditions.add(
      EnforcementZoneConditionsBuilder(initialVersionId).build(),
    )

    order.enforcementZoneConditions.add(
      EnforcementZoneConditionsBuilder(initialVersionId).apply {
        fileId = null
        fileName = ""
      }.build(),
    )

    return order
  }
}
