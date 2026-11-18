package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import org.slf4j.LoggerFactory
import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer

// We send checkin_schedule as a list, but Serco returns "" when it is empty.
class CheckinScheduleDeserializer : ValueDeserializer<MutableList<Schedule>?>() {
  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): MutableList<Schedule>? =
    when (p.currentToken()) {
      JsonToken.START_ARRAY -> {
        val listType = ctxt.typeFactory.constructCollectionType(MutableList::class.java, Schedule::class.java)
        ctxt.readValue<MutableList<Schedule>>(p, listType)
      }
      JsonToken.VALUE_STRING -> {
        val text = p.string
        if (text.isNotBlank()) {
          log.warn("Ignoring unexpected checkin_schedule text: {}", text)
        }
        mutableListOf()
      }
      else -> ctxt.handleUnexpectedToken(MutableList::class.java, p) as MutableList<Schedule>?
    }

  override fun getNullValue(ctxt: DeserializationContext): MutableList<Schedule>? = null

  companion object {
    private val log = LoggerFactory.getLogger(CheckinScheduleDeserializer::class.java)
  }
}
