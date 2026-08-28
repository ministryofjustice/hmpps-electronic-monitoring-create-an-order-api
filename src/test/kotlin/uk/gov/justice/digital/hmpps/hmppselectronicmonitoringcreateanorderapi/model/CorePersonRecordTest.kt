package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CorePersonRecord
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.setAddressSource
import java.util.UUID

class CorePersonRecordTest {
  @Test
  fun `sets the source on every address without changing the original record`() {
    val addresses = listOf(
      createAddress(AddressType.PRIMARY, AddressSource.CEMO),
      createAddress(AddressType.SECONDARY, null),
    )
    val record = CorePersonRecord(deviceWearer = null, contactDetails = null, addresses = addresses)

    val updatedRecord = record.setAddressSource(AddressSource.COMMON_PLATFORM)

    assertThat(updatedRecord.addresses).allSatisfy {
      assertThat(it.addressSource).isEqualTo(AddressSource.COMMON_PLATFORM)
    }
    assertThat(updatedRecord.addresses).extracting<UUID> { it.id }.containsExactlyElementsOf(addresses.map { it.id })
    assertThat(record.addresses.map { it.addressSource }).containsExactly(AddressSource.CEMO, null)
  }

  @Test
  fun `keeps addresses empty when the record has no addresses`() {
    val record = CorePersonRecord(deviceWearer = null, contactDetails = null, addresses = emptyList())

    val updatedRecord = record.setAddressSource(AddressSource.COMMON_PLATFORM)

    assertThat(updatedRecord.addresses).isEmpty()
  }

  private fun createAddress(addressType: AddressType, addressSource: AddressSource?) = Address(
    versionId = UUID.randomUUID(),
    addressType = addressType,
    addressLine1 = "1 Test Street",
    addressLine2 = "Test Town",
    postcode = "TE1 1ST",
    addressSource = addressSource,
  )
}
