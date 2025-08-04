package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model

import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAndRisk
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationLocation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InterestedParties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MandatoryAttendanceConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleAdult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MonitoringConditionType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderTypeDescription
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.SentenceType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YesNoUnknown
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

@ActiveProfiles("test")
abstract class OrderTestBase {
  protected fun createOrder(
    deviceWearer: DeviceWearer = createDeviceWearer(),
    addresses: List<Address> = listOf(createAddress()),
    responsibleAdult: ResponsibleAdult? = null,
    installationAndRisk: InstallationAndRisk = createInstallationAndRisk(),
    monitoringConditions: MonitoringConditions = createMonitoringConditions(),
    mandatoryAttendanceConditions: List<MandatoryAttendanceConditions> = listOf(),
    interestedParties: InterestedParties? = null,
    probationDeliveryUnits: ProbationDeliveryUnit? = null,
    trailMonitoringConditions: TrailMonitoringConditions? = null,
    installationLocation: InstallationLocation? = null,
  ): Order {
    val orderId = UUID.randomUUID()
    val versionId = UUID.randomUUID()
    val order = Order(
      id = UUID.randomUUID(),
      versions = mutableListOf(
        OrderVersion(
          id = versionId,
          username = "",
          status = OrderStatus.IN_PROGRESS,
          type = RequestType.REQUEST,
          orderId = orderId,
          dataDictionaryVersion = DataDictionaryVersion.DDV4,
        ),
      ),
    )

    order.deviceWearer = deviceWearer
    order.addresses.addAll(addresses)
    order.installationAndRisk = installationAndRisk
    order.monitoringConditions = monitoringConditions
    order.mandatoryAttendanceConditions.addAll(mandatoryAttendanceConditions)
    order.interestedParties = interestedParties
    order.probationDeliveryUnit = probationDeliveryUnits
    if (responsibleAdult != null) {
      order.deviceWearerResponsibleAdult = responsibleAdult
    }

    if (mandatoryAttendanceConditions.isNotEmpty()) {
      order.monitoringConditions?.mandatoryAttendance = true
    }

    order.monitoringConditionsTrail = trailMonitoringConditions

    order.installationLocation = installationLocation

    return order
  }

  protected fun createDeviceWearer(
    firstName: String = "John",
    lastName: String = "Smith",
    alias: String = "Johnny",
    dateOfBirth: ZonedDateTime = ZonedDateTime.of(1990, 1, 1, 1, 1, 1, 1, ZoneId.systemDefault()),
    adultAtTimeOfInstallation: Boolean = true,
    sex: String = "MALE",
    gender: String = "Male",
    disabilities: String = "VISION,LEARNING_UNDERSTANDING_CONCENTRATING",
    interpreterRequired: Boolean = true,
    language: String = "British Sign",
    pncId: String = "pncId",
    deliusId: String = "deliusId",
    nomisId: String = "nomisId",
    prisonNumber: String = "prisonNumber",
    homeOfficeReferenceNumber: String = "homeOfficeReferenceNumber",
    noFixedAbode: Boolean = false,
    riskCategory: String = "",
  ): DeviceWearer = DeviceWearer(
    versionId = UUID.randomUUID(),
    firstName = firstName,
    lastName = lastName,
    alias = alias,
    dateOfBirth = dateOfBirth,
    adultAtTimeOfInstallation = adultAtTimeOfInstallation,
    sex = sex,
    gender = gender,
    disabilities = disabilities,
    interpreterRequired = interpreterRequired,
    language = language,
    pncId = pncId,
    deliusId = deliusId,
    nomisId = nomisId,
    prisonNumber = prisonNumber,
    homeOfficeReferenceNumber = homeOfficeReferenceNumber,
    noFixedAbode = noFixedAbode,
  )

  protected fun createAddress(
    addressLine1: String = "Line 1",
    addressLine2: String = "Line 2",
    addressLine3: String = "",
    addressLine4: String = "",
    postcode: String = "AB11 1CD",
    addressType: AddressType = AddressType.PRIMARY,
  ) = Address(
    versionId = UUID.randomUUID(),
    addressLine1 = addressLine1,
    addressLine2 = addressLine2,
    addressLine3 = addressLine3,
    addressLine4 = addressLine4,
    postcode = postcode,
    addressType = addressType,
  )

  protected fun createInterestedParty(
    notifyingOrganisation: String = "",
    notifyingOrganisationName: String = "",
    notifyingOrganisationEmail: String = "",
    responsibleOfficerPhoneNumber: String = "",
    responsibleOrganisation: String = "PROBATION",
    responsibleOrganisationRegion: String = "",
    responsibleOfficerName: String = "",
    responsibleOrganisationEmail: String = "",
  ) = InterestedParties(
    versionId = UUID.randomUUID(),
    notifyingOrganisation = notifyingOrganisation,
    notifyingOrganisationName = notifyingOrganisationName,
    notifyingOrganisationEmail = notifyingOrganisationEmail,
    responsibleOfficerPhoneNumber = responsibleOfficerPhoneNumber,
    responsibleOrganisationRegion = responsibleOrganisationRegion,
    responsibleOrganisation = responsibleOrganisation,
    responsibleOfficerName = responsibleOfficerName,
    responsibleOrganisationEmail = responsibleOrganisationEmail,
  )

  protected fun createProbationDeliveryUnit(unit: String = "") = ProbationDeliveryUnit(
    versionId = UUID.randomUUID(),
    unit = unit,
  )

  protected fun createResponsibleAdult(fullName: String = "Mark Smith", contactNumber: String = "+447401111111") =
    ResponsibleAdult(
      versionId = UUID.randomUUID(),
      fullName = fullName,
      contactNumber = contactNumber,
    )

  protected fun createInstallationAndRisk(
    offence: String = "FRAUD_OFFENCES",
    riskDetails: String = "Danger",
    mappaLevel: String = "MAAPA 1",
    mappaCaseType: String = "CPPC (Critical Public Protection Case)",
    riskCategory: String = "THREATS_OF_VIOLENCE",
  ) = InstallationAndRisk(
    versionId = UUID.randomUUID(),
    offence = offence,
    riskDetails = riskDetails,
    mappaLevel = mappaLevel,
    mappaCaseType = mappaCaseType,
    riskCategory = arrayOf(riskCategory),
  )

  protected fun createMonitoringConditions(
    orderType: OrderType = OrderType.POST_RELEASE,
    issp: YesNoUnknown = YesNoUnknown.YES,
    hdc: YesNoUnknown = YesNoUnknown.YES,
    prarr: YesNoUnknown = YesNoUnknown.YES,
    startDate: ZonedDateTime = ZonedDateTime.of(2025, 1, 1, 1, 1, 1, 1, ZoneId.of("UTC")),
    endDate: ZonedDateTime = ZonedDateTime.of(2025, 2, 1, 1, 1, 1, 1, ZoneId.of("UTC")),
    sentenceType: SentenceType = SentenceType.LIFE_SENTENCE,
    conditionType: MonitoringConditionType = MonitoringConditionType.BAIL_ORDER,
    orderTypeDescription: OrderTypeDescription = OrderTypeDescription.DAPO,
    trail: Boolean = false,
    curfew: Boolean = false,
    alcohol: Boolean = false,
    mandatoryAttendance: Boolean = false,
    exclusionZone: Boolean = false,
  ): MonitoringConditions = MonitoringConditions(
    versionId = UUID.randomUUID(),
    orderType = orderType,
    issp = issp,
    hdc = hdc,
    prarr = prarr,
    startDate = startDate,
    endDate = endDate,
    sentenceType = sentenceType,
    conditionType = conditionType,
    orderTypeDescription = orderTypeDescription,
    trail = trail,
    curfew = curfew,
    alcohol = alcohol,
    mandatoryAttendance = mandatoryAttendance,
    exclusionZone = exclusionZone,
  )

  protected fun createMandatoryAttendanceCondition(
    endDate: ZonedDateTime = ZonedDateTime.of(2025, 2, 1, 0, 0, 0, 0, ZoneId.of("UTC")),
    startDate: ZonedDateTime = ZonedDateTime.of(2025, 1, 1, 23, 59, 0, 0, ZoneId.of("UTC")),
    postcode: String = "AB11 1CD",
    addressLine1: String = "Line 1",
    addressLine2: String = "Line 2",
    addressLine3: String = "",
    addressLine4: String = "",
    endTime: String = "13:00",
    purpose: String = "The appointment purpose",
    appointmentDay: String = "Monday",
    startTime: String = "12:00",
  ): MandatoryAttendanceConditions = MandatoryAttendanceConditions(
    versionId = UUID.randomUUID(),
    endDate = endDate,
    startDate = startDate,
    postcode = postcode,
    addressLine1 = addressLine1,
    addressLine2 = addressLine2,
    addressLine3 = addressLine3,
    addressLine4 = addressLine4,
    endTime = endTime,
    purpose = purpose,
    appointmentDay = appointmentDay,
    startTime = startTime,
  )
}
