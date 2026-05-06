package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.config.DeviceWearerChangedMessages
import kotlin.collections.plusAssign

fun DeviceWearer.compareTo(updated: DeviceWearer): List<String> {
  val messages = mutableSetOf<String>() // Avoid duplicates

  fun compareField(fieldName: String, oldValue: Any?, newValue: Any?) {
    if (oldValue != newValue) {
      DeviceWearerChangedMessages.messages[fieldName]?.let {
        messages += it
      }
    }
  }

  fun <T> compareList(fieldName: String, old: List<T>?, new: List<T>?) {
    if ((old ?: emptyList()) != (new ?: emptyList<T>())) {
      DeviceWearerChangedMessages.messages[fieldName]?.let {
        messages += it
      }
    }
  }



  compareField("nameChange", this.firstName, updated.firstName)
  compareField("nameChange", this.middleName, updated.middleName)
  compareField("nameChange", this.lastName, updated.lastName)
  compareField("alias", this.alias, updated.alias)

  compareField("dateOfBirth", this.dateOfBirth, updated.dateOfBirth)
  compareField("adultChild", this.adultChild, updated.adultChild)
  compareField("sex", this.sex, updated.sex)
  compareField("genderIdentity", this.genderIdentity, updated.genderIdentity)

  if (listOf(
      address1,
      address2,
      address3,
      address4,
      addressPostCode
    ) != listOf(
      updated.address1,
      updated.address2,
      updated.address3,
      updated.address4,
      updated.addressPostCode
    )
  ) {
    DeviceWearerChangedMessages.messages["primaryAddressChange"]?.let { messages += it }
  }


  if (listOf(
      secondaryAddress1,
      secondaryAddress2,
      secondaryAddress3,
      secondaryAddress4,
      secondaryAddressPostCode
    ) != listOf(
      updated.secondaryAddress1,
      updated.secondaryAddress2,
      updated.secondaryAddress3,
      updated.secondaryAddress4,
      updated.secondaryAddressPostCode
    )) {
    DeviceWearerChangedMessages.messages["secondaryAddressChange"]?.let { messages += it }
  }

  if (listOf(
      tertiaryAddress1,
      tertiaryAddress2,
      tertiaryAddress3,
      tertiaryAddress4,
      tertiaryAddressPostCode
    ) != listOf(
      updated.tertiaryAddress1,
      updated.tertiaryAddress2,
      updated.tertiaryAddress3,
      updated.tertiaryAddress4,
      updated.tertiaryAddressPostCode
    )) {
    DeviceWearerChangedMessages.messages["tertiaryAddressChange"]?.let { messages += it }
  }

  compareField("addressPostCode", this.addressPostCode, updated.addressPostCode)
  compareField("noFixedAddress", this.noFixedAddress, updated.noFixedAddress)

  compareField("phoneNumber", this.phoneNumber, updated.phoneNumber)

  compareField("mappa", this.mappa, updated.mappa)
  compareField("mappa", this.mappaCategory, updated.mappaCategory)
  compareField("mappa", this.mappaCaseType, updated.mappaCaseType)

  if (listOf(
      pncId,
      nomisId,
      deliusId,
      prisonNumber,
      complianceAndEnforcementPersonReference,
    ) != listOf(
      updated.pncId,
      updated.nomisId,
      updated.deliusId,
      updated.prisonNumber,
      updated.complianceAndEnforcementPersonReference,
    )) {
    DeviceWearerChangedMessages.messages["personalIdChanged"]?.let { messages += it }
  }

  if (listOf(
      interpreterRequired,
      language,
    ) != listOf(
      updated.interpreterRequired,
      updated.language,
    )) {
    DeviceWearerChangedMessages.messages["interpreterRequired"]?.let { messages += it }
  }

  if (listOf(
      responsibleAdultRequired,
      parent,
      guardian,
    ) != listOf(
      updated.responsibleAdultRequired,
      updated.parent,
      updated.guardian,
    )) {
    DeviceWearerChangedMessages.messages["responsibleAdultChanged"]?.let { messages += it }
  }
  compareField("parentPhoneNumber", this.parentPhoneNumber, updated.parentPhoneNumber)

  compareList("disability", this.disability, updated.disability)
  compareList("riskCategory", this.riskCategory, updated.riskCategory)

  return messages.toList()
}