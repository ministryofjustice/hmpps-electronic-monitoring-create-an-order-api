package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.utilities

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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAppointment
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationLocation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InterestedParties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderParameters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleAdult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.VariationDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DocumentType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MonitoringConditionType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderTypeDescription
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.SentenceType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.VariationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YesNoUnknown
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.collections.forEach

class TestUtilities {

  companion object {

    fun createReadyToSubmitOrder(
      id: UUID = UUID.randomUUID(),
      versionId: UUID = UUID.randomUUID(),
      noFixedAddress: Boolean = false,
      requestType: RequestType = RequestType.REQUEST,
      status: OrderStatus = OrderStatus.IN_PROGRESS,
      documents: MutableList<AdditionalDocument> = mutableListOf(),
      startDate: ZonedDateTime,
      endDate: ZonedDateTime,
      installationLocation: InstallationLocation? = null,
      installationAppointment: InstallationAppointment? = null,
      username: String = "AUTH_ADM",
    ): Order {
      val order = Order(
        id = id,

      )
      order.versions = mutableListOf(
        OrderVersion(
          id = versionId,
          username = username,
          status = OrderStatus.IN_PROGRESS,
          type = requestType,
          orderId = id,
          dataDictionaryVersion = DataDictionaryVersion.DDV4,
        ),
      )

      order.deviceWearer = DeviceWearer(
        versionId = versionId,
        firstName = "John",
        lastName = "Smith",
        alias = "Johnny",
        dateOfBirth = ZonedDateTime.of(1990, 1, 1, 1, 1, 1, 1, ZoneId.systemDefault()),
        adultAtTimeOfInstallation = true,
        sex = "MALE",
        gender = "MALE",
        disabilities = "VISION,LEARNING_UNDERSTANDING_CONCENTRATING",
        interpreterRequired = true,
        language = "British Sign",
        pncId = "pncId",
        deliusId = "deliusId",
        nomisId = "nomisId",
        prisonNumber = "prisonNumber",
        homeOfficeReferenceNumber = "homeOfficeReferenceNumber",
        noFixedAbode = noFixedAddress,
      )

      order.deviceWearerResponsibleAdult = ResponsibleAdult(
        versionId = versionId,
        fullName = "Mark Smith",
        contactNumber = "+447401111111",
      )

      val installationAddress = Address(
        versionId = versionId,
        addressLine1 = "24 Somewhere Street",
        addressLine2 = "Nowhere City",
        addressLine3 = "Random County",
        addressLine4 = "United Kingdom",
        postcode = "SW11 1NC",
        addressType = AddressType.INSTALLATION,
      )

      if (!noFixedAddress) {
        order.addresses.add(
          Address(
            versionId = versionId,
            addressLine1 = "20 Somewhere Street",
            addressLine2 = "Nowhere City",
            addressLine3 = "Random County",
            addressLine4 = "United Kingdom",
            postcode = "SW11 1NC",
            addressType = AddressType.PRIMARY,
          ),
        )
        order.addresses.add(
          Address(
            versionId = versionId,
            addressLine1 = "22 Somewhere Street",
            addressLine2 = "Nowhere City",
            addressLine3 = "Random County",
            addressLine4 = "United Kingdom",
            postcode = "SW11 1NC",
            addressType = AddressType.SECONDARY,
          ),
        )
      }

      order.addresses.add(
        installationAddress,
      )

      order.installationAndRisk = InstallationAndRisk(
        versionId = versionId,
        offence = "FRAUD_OFFENCES",
        riskDetails = "Danger",
        riskCategory = arrayOf("SEXUAL_OFFENCES", "RISK_TO_GENDER"),
        mappaLevel = "MAAPA 1",
        mappaCaseType = "CPPC (Critical Public Protection Case)",
      )

      order.contactDetails = ContactDetails(
        versionId = versionId,
        contactNumber = "07401111111",
      )

      order.monitoringConditions = MonitoringConditions(
        versionId = versionId,
        orderType = OrderType.COMMUNITY,
        orderTypeDescription = OrderTypeDescription.DAPOL,
        startDate = startDate,
        endDate = endDate,
        curfew = true,
        trail = true,
        exclusionZone = true,
        alcohol = true,
        caseId = "d8ea62e61bb8d610a10c20e0b24bcb85",
        conditionType = MonitoringConditionType.REQUIREMENT_OF_A_COMMUNITY_ORDER,
        sentenceType = SentenceType.LIFE_SENTENCE,
        issp = YesNoUnknown.YES,
      )

      documents.forEach {
        order.additionalDocuments.add(it)
      }
      if (documents.isEmpty()) {
        order.additionalDocuments.add(
          AdditionalDocument(
            versionId = versionId,
            fileName = "Test file",
            fileType = DocumentType.LICENCE,
          ),
        )
      }

      val curfewConditions = CurfewConditions(
        versionId = versionId,
        startDate = startDate,
        endDate = endDate,
        curfewAddress = "PRIMARY,SECONDARY",
      )

      val curfewTimeTables = DayOfWeek.entries.map {
        CurfewTimeTable(
          versionId = versionId,
          dayOfWeek = it,
          startTime = "17:00",
          endTime = "09:00",
          curfewAddress = "PRIMARY_ADDRESS",
        )
      }
      order.curfewTimeTable.addAll(curfewTimeTables)
      val secondTimeTable = DayOfWeek.entries.map {
        CurfewTimeTable(
          versionId = versionId,
          dayOfWeek = it,
          startTime = "17:00",
          endTime = "09:00",
          curfewAddress = "SECONDARY_ADDRESS",
        )
      }
      order.curfewTimeTable.addAll(secondTimeTable)
      order.curfewConditions = curfewConditions

      order.curfewReleaseDateConditions = CurfewReleaseDateConditions(
        versionId = versionId,
        releaseDate = startDate,
        startTime = "19:00",
        endTime = "23:00",
        curfewAddress = AddressType.PRIMARY,
      )

      order.enforcementZoneConditions.add(
        EnforcementZoneConditions(
          versionId = versionId,
          description = "Mock Exclusion Zone",
          duration = "Mock Exclusion Duration",
          startDate = startDate,
          endDate = endDate,
          zoneType = EnforcementZoneType.EXCLUSION,
          fileId = UUID.randomUUID(),
          fileName = "MockMapFile.jpeg",
        ),
      )

      order.enforcementZoneConditions.add(
        EnforcementZoneConditions(
          versionId = versionId,
          description = "Mock Inclusion Zone",
          duration = "Mock Inclusion Duration",
          startDate = startDate,
          endDate = endDate,
          zoneType = EnforcementZoneType.INCLUSION,
        ),
      )

      order.monitoringConditionsAlcohol = AlcoholMonitoringConditions(
        versionId = versionId,
        startDate = startDate,
        endDate = endDate,
        monitoringType = AlcoholMonitoringType.ALCOHOL_ABSTINENCE,
      )

      order.monitoringConditionsTrail = TrailMonitoringConditions(
        versionId = versionId,
        startDate = startDate,
        endDate = endDate,
      )

      order.interestedParties = InterestedParties(
        versionId = versionId,
        responsibleOfficerName = "John Smith",
        responsibleOfficerPhoneNumber = "07401111111",
        responsibleOrganisation = "PROBATION",
        responsibleOrganisationRegion = "LONDON",
        responsibleOrganisationEmail = "abc@def.com",
        notifyingOrganisation = "PRISON",
        notifyingOrganisationName = "WAYLAND_PRISON",
        notifyingOrganisationEmail = "",
      )
      order.probationDeliveryUnit = ProbationDeliveryUnit(
        versionId = versionId,
        unit = "CAMDEN_AND_ISLINGTON",
      )
      order.orderParameters = OrderParameters(
        versionId = versionId,
        havePhoto = false,
      )
      if (order.getCurrentVersion().type === RequestType.VARIATION) {
        order.variationDetails = VariationDetails(
          versionId = versionId,
          variationType = VariationType.ADDRESS,
          variationDate = startDate,
          variationDetails = "Change to address",
        )
      }
      order.installationLocation = installationLocation
      order.installationAppointment = installationAppointment
      order.versions[0].status = status

      return order
    }
  }
}
