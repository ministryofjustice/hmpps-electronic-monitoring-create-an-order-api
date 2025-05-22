package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.stereotype.Service
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@ActiveProfiles("test")
@SpringBootTest
class OrderSectionServiceBaseTests {
  @MockitoBean
  lateinit var orderRepo: OrderRepository

  @Autowired
  lateinit var testObject: OrderServiceBaseTestObject

  private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  private val londonTimeZone = ZoneId.of("Europe/London")

  private fun getBritishDateAndTime(dateTime: ZonedDateTime?): String? =
    dateTime?.toInstant()?.atZone(londonTimeZone)?.format(dateTimeFormatter)

  @Test
  fun `default BST date should have time converted to UTC`() {
    val userSuppliedDate = ZonedDateTime.of(2025, 5, 22, 0, 0, 0, 0, ZoneId.of("UTC"))

    // Expect stored time is 22:59 in UTC
    val expectedDate = ZonedDateTime.of(2025, 5, 22, 22, 59, 0, 0, ZoneId.of("UTC"))

    val actualDate = testObject.getDefaultZonedDateTimeWrapper(userSuppliedDate, 23, 59)
    assertThat(actualDate).isEqualTo(expectedDate)

    // Assert Serco would receive time value with 23:59:00
    assertThat(getBritishDateAndTime(actualDate)).isEqualTo("2025-05-22 23:59:00")
  }

  @Test
  fun `default GMT date should have time converted to UTC`() {
    val userSuppliedDate = ZonedDateTime.of(2025, 12, 22, 0, 0, 0, 0, ZoneId.of("UTC"))

    // Expect stored time is 23:59 in UTC
    val expectedDate = ZonedDateTime.of(2025, 12, 22, 23, 59, 0, 0, ZoneId.of("UTC"))

    val actualDate = testObject.getDefaultZonedDateTimeWrapper(userSuppliedDate, 23, 59)
    assertThat(actualDate).isEqualTo(expectedDate)

    // Assert Serco would receive time value with 23:59:00
    assertThat(getBritishDateAndTime(actualDate)).isEqualTo("2025-12-22 23:59:00")
  }
}

@Service
class OrderServiceBaseTestObject : OrderSectionServiceBase() {

  public fun getDefaultZonedDateTimeWrapper(date: ZonedDateTime?, hours: Int, minutes: Int): ZonedDateTime? =
    getDefaultZonedDateTime(date, hours, minutes)
}
