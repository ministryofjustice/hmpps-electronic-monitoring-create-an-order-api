package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAndRisk
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleAdult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer as FmsDeviceWearer

@ActiveProfiles("test")
class DeviceWearerTest {

  @ParameterizedTest(name = "it should map saved sex values to Serco - {0} -> {1}")
  @MethodSource("sexValues")
  fun `It should map correctly map saved sex values to Serco`(savedValue: String, mappedValue: String) {
    val order = createOrder(
      deviceWearer = createDeviceWearer(sex = savedValue),
    )
    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order)

    assertThat(fmsDeviceWearer.sex).isEqualTo(mappedValue)
  }

  @ParameterizedTest(name = "it should map saved gender values to Serco - {0} -> {1}")
  @MethodSource("genderValues")
  fun `It should map correctly map saved gender values to Serco`(savedValue: String, mappedValue: String) {
    val order = createOrder(
      deviceWearer = createDeviceWearer(gender = savedValue),
    )
    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order)

    assertThat(fmsDeviceWearer.genderIdentity).isEqualTo(mappedValue)
  }

  companion object {
    @JvmStatic
    fun sexValues() = listOf(
      Arguments.of("MALE", "Male"),
      Arguments.of("FEMALE", "Female"),
      Arguments.of("PREFER_NOT_TO_SAY", "Prefer Not to Say"),
      Arguments.of("UNKNOWN", "Prefer Not to Say"),
    )

    @JvmStatic
    fun genderValues() = listOf(
      Arguments.of("MALE", "Male"),
      Arguments.of("FEMALE", "Female"),
      Arguments.of("NON_BINARY", "Non-Binary"),
      Arguments.of("PREFER_TO_SELF_DESCRIBE", "Prefer to self-describe"),
      Arguments.of("NOT_ABLE_TO_PROVIDE_THIS_INFORMATION", ""),
    )
  }

  private fun createOrder(
    deviceWearer: DeviceWearer = createDeviceWearer(),
    addresses: List<Address> = listOf(createAddress()),
    responsibleAdult: ResponsibleAdult? = null,
    installationAndRisk: InstallationAndRisk = createInstallationAndRisk(),
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
        ),
      ),
    )

    order.deviceWearer = deviceWearer
    order.addresses.addAll(addresses)
    order.installationAndRisk = installationAndRisk

    if (responsibleAdult != null) {
      order.deviceWearerResponsibleAdult = responsibleAdult
    }

    return order
  }

  private fun createDeviceWearer(
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

  private fun createAddress(
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

  private fun createResponsibleAdult(fullName: String = "Mark Smith", contactNumber: String = "+447401111111") =
    ResponsibleAdult(
      versionId = UUID.randomUUID(),
      fullName = fullName,
      contactNumber = contactNumber,
    )

  private fun createInstallationAndRisk(
    offence: String = "FRAUD_OFFENCES",
    riskDetails: String = "Danger",
    mappaLevel: String = "MAAPA 1",
    mappaCaseType: String = "CPPC (Critical Public Protection Case)",
  ) = InstallationAndRisk(
    versionId = UUID.randomUUID(),
    offence = offence,
    riskDetails = riskDetails,
    mappaLevel = mappaLevel,
    mappaCaseType = mappaCaseType,
  )
}
