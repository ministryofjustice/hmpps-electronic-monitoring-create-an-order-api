package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceWearerAddressUsage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.InstallationLocationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

@DataJpaTest(
  properties = [
    "spring.datasource.url=jdbc:h2:mem:cemo-db;MODE=PostgreSQL",
    "spring.jpa.hibernate.ddl-auto=create",
    "spring.flyway.enabled=false",
  ],
)
class OrderVersionRepositoryTest {
  @Autowired
  lateinit var orderRepo: OrderRepository

  private val mockUsername: String = "username"
  private val mockOrderId: UUID = UUID.randomUUID()
  private val mockAddressId = UUID.randomUUID()
  private val mockVersionId = UUID.randomUUID()
  private val mockAlcoholMonitoringConditionsId = UUID.randomUUID()

  private val mockStartDate: ZonedDateTime = ZonedDateTime.of(
    LocalDate.of(2025, 1, 1),
    LocalTime.NOON,
    ZoneId.of("UTC"),
  )

  private val mockEndDate: ZonedDateTime = ZonedDateTime.of(
    LocalDate.of(2030, 1, 1),
    LocalTime.NOON,
    ZoneId.of("UTC"),
  )

  private val mockAddress = Address(
    id = mockAddressId,
    versionId = mockVersionId,
    addressType = AddressType.PRIMARY,
    addressUsage = DeviceWearerAddressUsage.NA,
    addressLine1 = "mockAddressLine1",
    addressLine2 = "mockAddressLine2",
    addressLine3 = "",
    addressLine4 = "",
    postcode = "mockPostcode",
  )

  private val updatedMockAddress = Address(
    id = mockAddressId,
    versionId = mockVersionId,
    addressType = AddressType.PRIMARY,
    addressUsage = DeviceWearerAddressUsage.NA,
    addressLine1 = "updatedMockAddressLine1",
    addressLine2 = "updatedMockAddressLine2",
    addressLine3 = "",
    addressLine4 = "",
    postcode = "updatedMockPostcode",
  )

  private val orderVersionWithExistingPrimaryAddressAndRelation = Order(
    id = mockOrderId,
    versions = mutableListOf(
      OrderVersion(
        id = mockVersionId,
        username = mockUsername,
        status = OrderStatus.IN_PROGRESS,
        type = RequestType.REQUEST,
        orderId = mockOrderId,
        monitoringConditionsAlcohol = AlcoholMonitoringConditions(
          id = mockAlcoholMonitoringConditionsId,
          versionId = mockVersionId,
          monitoringType = AlcoholMonitoringType.ALCOHOL_ABSTINENCE,
          startDate = mockStartDate,
          endDate = mockEndDate,
          installationLocation = InstallationLocationType.PRIMARY,
          installationAddressId = mockAddressId,
          prisonName = null,
          probationOfficeName = null,
        ),
        addresses = mutableListOf(mockAddress),
        dataDictionaryVersion = DataDictionaryVersion.DDV4,
      ),
    ),
  )

  private val updatedOrderVersionWithExistingPrimaryAddressAndRelation = Order(
    id = mockOrderId,
    versions = mutableListOf(
      OrderVersion(
        id = mockVersionId,
        username = mockUsername,
        status = OrderStatus.IN_PROGRESS,
        type = RequestType.REQUEST,
        orderId = mockOrderId,
        monitoringConditionsAlcohol = AlcoholMonitoringConditions(
          id = mockAlcoholMonitoringConditionsId,
          versionId = mockVersionId,
          monitoringType = AlcoholMonitoringType.ALCOHOL_ABSTINENCE,
          startDate = mockStartDate,
          endDate = mockEndDate,
          installationLocation = InstallationLocationType.PRIMARY,
          installationAddressId = mockAddressId,
          prisonName = null,
          probationOfficeName = null,
        ),
        addresses = mutableListOf(updatedMockAddress),
        dataDictionaryVersion = DataDictionaryVersion.DDV4,
      ),
    ),
  )

  fun databaseInsert() {
    orderRepo.save(orderVersionWithExistingPrimaryAddressAndRelation)
  }

  @Nested
  inner class WhenSavingAnOrderVersionThanUpdatesAnAddress {
    @Test
    fun `does not throw an error when other models are linked to that address`() {
      databaseInsert()

      assertDoesNotThrow {
        orderRepo.save(updatedOrderVersionWithExistingPrimaryAddressAndRelation)
      }
      assertDoesNotThrow {
        orderRepo.findById(mockOrderId)
      }
    }
  }
}
