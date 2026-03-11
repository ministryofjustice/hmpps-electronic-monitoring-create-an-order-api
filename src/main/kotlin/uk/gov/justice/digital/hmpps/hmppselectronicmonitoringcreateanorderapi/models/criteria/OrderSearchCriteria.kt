package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria

data class OrderSearchCriteria(val searchTerm: String = "", val tagFilter: TagFilter = TagFilter())

data class TagFilter(val tagGroups: List<List<String>> = emptyList(), val exclude: List<String> = emptyList()) {
  fun allOf(vararg tags: String): TagFilter = copy(tagGroups = tagGroups + listOf(tags.toList()))

  fun anyOf(vararg tags: String): TagFilter = copy(tagGroups = tagGroups + tags.map { listOf(it) })

  fun exclude(vararg tags: String): TagFilter = copy(exclude = exclude + tags.toList())
}
