package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.EnforceableCondition

class EnforceableConditionTest {
  private val objectMapper = JsonMapper.builder().build()

  @Test
  fun `it should not serialise isTrail and isAlcohol methods`() {
    val condition = EnforceableCondition(
      condition = "Location Monitoring (Fitted Device)",
      startDate = "2026-01-01 00:00:00",
      endDate = "2026-02-01 00:00:00",
    )

    val json = objectMapper.writeValueAsString(condition)

    assertThat(json).doesNotContain("\"trail\"")
    assertThat(json).doesNotContain("\"alcohol\"")
  }
}
