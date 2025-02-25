package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class HmppsSqsEventMessage(
  @JsonProperty("Type") val type: String,
  @JsonProperty("Message") val message: String,
  @JsonProperty("MessageId") val messageId: String,
  @JsonProperty("MessageAttributes") val messageAttributes: HmppsSqsMessageAttributes,

)

@JsonIgnoreProperties(ignoreUnknown = true)
class HmppsSqsMessageAttributes(@JsonProperty("eventType") val eventType: HmppsSqsMessageAttribute)

@JsonIgnoreProperties(ignoreUnknown = true)
class HmppsSqsMessageAttribute(@JsonProperty("Type") val type: String, @JsonProperty("Value") val value: String)
