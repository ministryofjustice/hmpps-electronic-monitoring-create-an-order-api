package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

internal fun normalizedComparisonValue(value: Any?): Any? = when (value) {
  is String -> value.takeUnless { it.isBlank() || it.equals("null", ignoreCase = true) }
  is List<*> -> value.map(::normalizedComparisonValue)
  is OffenceData -> listOf(normalizedComparisonValue(value.offence), normalizedComparisonValue(value.offenceDate))
  is FmsRiskCategory -> normalizedComparisonValue(value.category)
  is Disability -> normalizedComparisonValue(value.disability)
  is DapoClause -> listOf(
    normalizedComparisonValue(value.dapoOrderClauseNumber),
    normalizedComparisonValue(value.date),
  )
  is Zone -> listOf(
    normalizedComparisonValue(value.description),
    normalizedComparisonValue(value.duration),
    normalizedComparisonValue(value.start),
    normalizedComparisonValue(value.end),
  )
  is Schedule -> listOf(
    normalizedComparisonValue(value.day),
    normalizedComparisonValue(value.start),
    normalizedComparisonValue(value.end),
  )
  else -> value
}

internal fun sameComparisonValue(first: Any?, second: Any?): Boolean =
  normalizedComparisonValue(first) == normalizedComparisonValue(second)

internal fun sameCaseInsensitiveText(first: String?, second: String?): Boolean =
  normalizedComparisonValue(first)?.toString()?.equals(
    normalizedComparisonValue(second)?.toString(),
    ignoreCase = true,
  ) ?: (normalizedComparisonValue(second) == null)
