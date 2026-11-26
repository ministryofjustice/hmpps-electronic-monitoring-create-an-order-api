package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsDates
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

class FmsDatesTest {
  @ParameterizedTest
  @CsvSource(
    "2026-01-15, 2026-01-15T00:00:00Z",
    "2026-07-15, 2026-07-14T23:00:00Z",
    "2026-03-29, 2026-03-29T00:00:00Z",
    "2026-03-30, 2026-03-29T23:00:00Z",
    "2026-10-25, 2026-10-24T23:00:00Z",
    "2026-10-26, 2026-10-26T00:00:00Z",
    "1970-01-01, 1969-12-31T23:00:00Z",
  )
  fun `it should parse a British calendar date as London midnight expressed in UTC`(
    date: String,
    expectedInstant: String,
  ) {
    val parsed = FmsDates.parseDate(date)

    assertThat(parsed.toInstant()).isEqualTo(Instant.parse(expectedInstant))
    assertThat(parsed.offset).isEqualTo(ZoneOffset.UTC)
    assertThat(FmsDates.getBritishDate(parsed)).isEqualTo(date)
  }

  @ParameterizedTest
  @CsvSource(
    "2026-01-15 12:00:00, 2026-01-15T12:00:00Z",
    "2026-07-15 12:00:00, 2026-07-15T11:00:00Z",
    "2026-07-15 00:00:00, 2026-07-14T23:00:00Z",
    "2026-03-29 00:30:00, 2026-03-29T00:30:00Z",
    "2026-03-29 02:30:00, 2026-03-29T01:30:00Z",
    "2026-10-25 00:30:00, 2026-10-24T23:30:00Z",
    "2026-10-25 02:30:00, 2026-10-25T02:30:00Z",
  )
  fun `it should round trip unambiguous British timestamps at second precision`(
    dateTime: String,
    expectedInstant: String,
  ) {
    val parsed = FmsDates.parseDateTime(dateTime)

    assertThat(parsed.toInstant()).isEqualTo(Instant.parse(expectedInstant))
    assertThat(parsed.offset).isEqualTo(ZoneOffset.UTC)
    assertThat(FmsDates.getBritishDateAndTime(parsed)).isEqualTo(dateTime)
  }

  @ParameterizedTest
  @ValueSource(strings = ["UTC", "Europe/London", "America/New_York", "Asia/Tokyo"])
  fun `it should format the same instant consistently regardless of its stored zone`(zone: String) {
    val stored = Instant.parse("2026-07-14T23:30:00Z").atZone(ZoneId.of(zone))

    assertThat(FmsDates.getBritishDate(stored)).isEqualTo("2026-07-15")
    assertThat(FmsDates.getBritishDateAndTime(stored)).isEqualTo("2026-07-15 00:30:00")
  }

  @Test
  fun `it should return null when formatting absent dates`() {
    assertThat(FmsDates.getBritishDate(null)).isNull()
    assertThat(FmsDates.getBritishDateAndTime(null)).isNull()
  }

  @ParameterizedTest
  @ValueSource(strings = ["", " ", "\t"])
  fun `it should return null when parsing optional blank dates`(date: String) {
    assertThat(FmsDates.parseDateOrNull(date)).isNull()
    assertThat(FmsDates.parseDateTimeOrNull(date)).isNull()
  }

  @Test
  fun `it should reject timestamps in date-only fields and dates in timestamp fields`() {
    assertThatThrownBy { FmsDates.parseDate("2026-07-15 23:59:00") }
      .isInstanceOf(DateTimeParseException::class.java)
    assertThatThrownBy { FmsDates.parseDateTime("2026-07-15") }
      .isInstanceOf(DateTimeParseException::class.java)
    assertThatThrownBy { FmsDates.parseDateOrNull("not a date") }
      .isInstanceOf(DateTimeParseException::class.java)
    assertThatThrownBy { FmsDates.parseDateTimeOrNull("not a timestamp") }
      .isInstanceOf(DateTimeParseException::class.java)
  }

  @Test
  fun `Java resolves a nonexistent London clock time by shifting it through the spring gap`() {
    val parsed = FmsDates.parseDateTime("2026-03-29 01:30:00")

    assertThat(parsed.toInstant()).isEqualTo(Instant.parse("2026-03-29T01:30:00Z"))
    assertThat(FmsDates.getBritishDateAndTime(parsed)).isEqualTo("2026-03-29 02:30:00")
  }

  @Test
  fun `an offset-free FMS timestamp cannot distinguish the two autumn clock occurrences`() {
    val firstOccurrence = ZonedDateTime.parse("2026-10-25T00:30:00Z")
    val secondOccurrence = ZonedDateTime.parse("2026-10-25T01:30:00Z")

    val firstTimestamp = FmsDates.getBritishDateAndTime(firstOccurrence)
    val secondTimestamp = FmsDates.getBritishDateAndTime(secondOccurrence)

    assertThat(firstTimestamp).isEqualTo("2026-10-25 01:30:00")
    assertThat(secondTimestamp).isEqualTo(firstTimestamp)
    assertThat(FmsDates.parseDateTime(requireNotNull(secondTimestamp)).toInstant())
      .isEqualTo(firstOccurrence.toInstant())
      .isNotEqualTo(secondOccurrence.toInstant())
  }
}
