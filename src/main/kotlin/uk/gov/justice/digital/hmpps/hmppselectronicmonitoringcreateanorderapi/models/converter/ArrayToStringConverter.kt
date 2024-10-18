package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class ArrayToStringConverter : AttributeConverter<Array<String>, String> {
  override fun convertToDatabaseColumn(attribute: Array<String>?): String? {
    if (attribute.isNullOrEmpty()) {
      return null
    }
    return attribute.joinToString(",")
  }
  override fun convertToEntityAttribute(dbData: String?): Array<String>? {
    if (dbData.isNullOrEmpty()) {
      return null
    }
    return dbData.split(",").toTypedArray() ?: emptyArray()
  }
}
