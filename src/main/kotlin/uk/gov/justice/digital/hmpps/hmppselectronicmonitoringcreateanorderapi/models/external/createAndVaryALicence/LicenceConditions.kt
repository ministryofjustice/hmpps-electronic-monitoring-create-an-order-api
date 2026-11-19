package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.createAndVaryALicence

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Date

@JsonIgnoreProperties(ignoreUnknown = true)
data class Licence(
  val conditions: LicenceConditions,
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  val licenceStartDate: Date,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LicenceConditions(
  @JsonProperty("AP") val ap: ConditionSet? = null,
  @JsonProperty("PSS") val pss: ConditionSet? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ConditionSet(
  val additional: List<LicenceCondition> = emptyList(),
  // TODO: There are also standard and bespoke conditions in the json response...For now just using additional
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LicenceCondition(
  val id: Long? = null,
  val code: String? = null,
  val category: String? = null,
  val text: String? = null,
  val type: String? = null,
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  val endDate: Date? = null,
  val restrictions: List<String>? = null,
)
