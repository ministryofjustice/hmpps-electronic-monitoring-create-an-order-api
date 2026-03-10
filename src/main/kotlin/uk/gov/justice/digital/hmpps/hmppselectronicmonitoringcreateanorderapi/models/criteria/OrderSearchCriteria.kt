package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria

data class OrderSearchCriteria(val searchTerm: String = "", val tagFilter: TagFilter = TagFilter())

data class TagFilter(val tagGroups: List<List<String>> = emptyList(), val noneOf: List<String> = emptyList())
