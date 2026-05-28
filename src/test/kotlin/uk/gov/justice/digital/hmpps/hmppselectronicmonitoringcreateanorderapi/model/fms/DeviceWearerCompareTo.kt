package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.DeviceWearerFieldChangeArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.DeviceWearerNegativeFieldArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.FieldChangeCase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.Disability
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsRiskCategory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.compareTo

class DeviceWearerCompareTo {

  private fun baselineWearer() = DeviceWearer(
    firstName = "John",
    lastName = "Doe",
    alias = "JD",
    dateOfBirth = "1990-01-01",
    sex = "Male",
    genderIdentity = "Male",
    address1 = "123 Main St",
    secondaryAddress1 = "Flat 2",
    phoneNumber = "07000000000",
    noFixedAddress = "false",
    mappa = "Yes",
    interpreterRequired = "No",
    responsibleAdultRequired = "false",
    parentPhoneNumber = "07111111111",
    disability = listOf(Disability("Visual")),
    riskCategory = listOf(FmsRiskCategory("High")),
  )

  @Test
  fun `returns empty list when no fields change`() {
    val old = baselineWearer()
    val updated = baselineWearer()

    val result = updated.compareTo(old)

    Assertions.assertThat(result).isEmpty()
  }

  @Test
  fun `returns message when value is added`() {
    val old = baselineWearer().apply {
      phoneNumber = ""
    }
    val updated = baselineWearer().apply {
      phoneNumber = "07999999999"
    }

    val result = updated.compareTo(old)

    Assertions.assertThat(result)
      .containsExactly("Device wearer's phone number has changed")
  }

  @Test
  fun `returns message when value is deleted`() {
    val old = baselineWearer()
    val updated = baselineWearer().apply {
      phoneNumber = ""
    }

    val result = updated.compareTo(old)

    Assertions.assertThat(result)
      .containsExactly("Device wearer's phone number has changed")
  }

  @Test
  fun `returns message when list content changes`() {
    val old = baselineWearer()
    val updated = baselineWearer().apply {
      disability = listOf(Disability("Hearing"))
    }

    val result = updated.compareTo(old)

    Assertions.assertThat(result)
      .containsExactly(
        "Device wearer's disability or health conditions have changed",
      )
  }

  @Test
  fun `returns message when list is added`() {
    val old = baselineWearer().apply {
      disability = emptyList()
    }
    val updated = baselineWearer()

    val result = updated.compareTo(old)

    Assertions.assertThat(result)
      .contains("Device wearer's disability or health conditions have changed")
  }

  @Test
  fun `returns message when list is deleted`() {
    val old = baselineWearer()
    val updated = baselineWearer().apply {
      riskCategory = emptyList()
    }

    val result = updated.compareTo(old)

    Assertions.assertThat(result)
      .contains("Device wearer's risk categories have changed")
  }

  @Test
  fun `returns all relevant messages for multiple changes`() {
    val old = baselineWearer()
    val updated = baselineWearer().apply {
      firstName = "Jane"
      address1 = "456 New Road"
      mappa = "No"
    }

    val result = updated.compareTo(old)

    Assertions.assertThat(result).containsExactlyInAnyOrder(
      "Device wearer's name has changed",
      "Device wearer's main address has changed",
      "Device wearer's MAPPA level has changed",
    )
  }

  @Test
  fun `same message is returned only once even if multiple fields map to it`() {
    val old = baselineWearer()
    val updated = baselineWearer().apply {
      pncId = "PNC123"
      complianceAndEnforcementPersonReference = "CEPR456"
    }

    val result = updated.compareTo(old)

    Assertions.assertThat(result).containsExactly(
      "Device wearer's personal ID number(s) have changed",
    )
  }

  @Test
  fun `does not return message for unmapped field changes`() {
    val old = baselineWearer()
    val updated = baselineWearer().apply {
      title = "ChangedButUnmapped"
    }

    val result = updated.compareTo(old)

    Assertions.assertThat(result).isEmpty()
  }

  @ParameterizedTest(name = "changing {0} field emits expected message")
  @ArgumentsSource(DeviceWearerFieldChangeArgumentsProvider::class)
  fun `changing field emits expected message`(case: FieldChangeCase) {
    val old = baselineWearer()
    val updated = baselineWearer()
    case.mutate(updated)
    val result = updated.compareTo(old)
    Assertions.assertThat(result)
      .withFailMessage("Field ${case.name} did not emit expected message")
      .contains(case.expectedMessage)
  }

  @Test
  fun `should return message when order changed from adult to child`() {
    val old = baselineWearer()
    old.adultChild = "adult"
    val updated = baselineWearer()
    updated.adultChild = "child"
    val result = updated.compareTo(old)
    Assertions.assertThat(result)
      .contains("Order has changed from an adult to youth")
  }

  @Test
  fun `should return message when order changed from child to adult`() {
    val old = baselineWearer()
    old.adultChild = "child"
    val updated = baselineWearer()
    updated.adultChild = "adult"
    val result = updated.compareTo(old)
    Assertions.assertThat(result)
      .contains("Order has changed from a youth to adult")
  }

  @Test
  fun `should return message when order changed from no fixed address to has fixed address`() {
    val old = baselineWearer()
    old.noFixedAddress = "false"
    val updated = baselineWearer()
    updated.noFixedAddress = "true"
    val result = updated.compareTo(old)
    Assertions.assertThat(result)
      .contains("Device wearer now doesn't have a fixed address")
  }

  @Test
  fun `should return message when order changed from has fixed address to no fixed address`() {
    val old = baselineWearer()
    old.noFixedAddress = "true"
    val updated = baselineWearer()
    updated.noFixedAddress = "false"
    val result = updated.compareTo(old)
    Assertions.assertThat(result)
      .contains("Device wearer now has a fixed address")
  }

  @ParameterizedTest(name = "changing {0} does NOT emit any message")
  @ArgumentsSource(DeviceWearerNegativeFieldArgumentsProvider::class)
  fun `changing non mapped field emits no message`(case: FieldChangeCase) {
    val old = baselineWearer()
    val updated = baselineWearer()

    case.mutate(updated)

    val result = updated.compareTo(old)

    Assertions.assertThat(result)
      .withFailMessage(
        "Field ${case.name} should not emit any change message but got: $result",
      )
      .isEmpty()
  }
}
