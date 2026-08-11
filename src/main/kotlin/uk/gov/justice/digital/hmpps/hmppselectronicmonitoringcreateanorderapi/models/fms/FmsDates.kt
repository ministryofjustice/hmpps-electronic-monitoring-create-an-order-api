package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal object FmsDates {
  val londonTimeZone: ZoneId = ZoneId.of("Europe/London")
  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  fun getBritishDate(dateTime: ZonedDateTime?): String? =
    dateTime?.toInstant()?.atZone(londonTimeZone)?.format(dateFormatter)

  fun getBritishDateAndTime(dateTime: ZonedDateTime?): String? =
    dateTime?.toInstant()?.atZone(londonTimeZone)?.format(dateTimeFormatter)

  fun parseDate(date: String): ZonedDateTime =
    LocalDate.parse(date, dateFormatter).atStartOfDay().atZone(londonTimeZone)

  fun parseDateOrNull(date: String): ZonedDateTime? = if (date.isNotBlank()) parseDate(date) else null

  fun parseDateTime(date: String): ZonedDateTime = LocalDateTime.parse(date, dateTimeFormatter).atZone(londonTimeZone)

  fun parseDateTimeOrNull(date: String): ZonedDateTime? = if (date.isNotBlank()) parseDateTime(date) else null
}
