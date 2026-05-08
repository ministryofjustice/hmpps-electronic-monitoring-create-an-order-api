package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.config.DeviceWearerChangedMessages
import kotlin.collections.plusAssign

fun DeviceWearer.compareTo(previous: DeviceWearer): List<String> {
  val messages = mutableListOf<String>()

  fun compareField(fieldName: String, newValue: Any?, oldValue: Any?) {
    if (oldValue != newValue) {
      DeviceWearerChangedMessages.messages[fieldName]?.let {
        messages += it
      }
    }
  }

  fun <T> compareList(fieldName: String, new: List<T>?, old: List<T>?) {
    if ((old ?: emptyList()) != (new ?: emptyList<T>())) {
      DeviceWearerChangedMessages.messages[fieldName]?.let {
        messages += it
      }
    }
  }

  fun compareAdultChild(new: String?, old: String?) {
    if (new != old) {
      if (new === "adult") {
        DeviceWearerChangedMessages.messages["childToAdult"]?.let {
          messages += it
        }
      } else if (new === "child") {
        DeviceWearerChangedMessages.messages["adultToChild"]?.let {
          messages += it
        }
      }
    }
  }

  fun compareNoFixedAddress(new: String?, old: String?) {
    if (new != old) {
      if (new === "true") {
        DeviceWearerChangedMessages.messages["hasFixedAddress"]?.let {
          messages += it
        }
      } else if (new === "false") {
        DeviceWearerChangedMessages.messages["noFixedAddress"]?.let {
          messages += it
        }
      }
    }
  }

  compareField("nameChange", this.firstName, previous.firstName)
  compareField("nameChange", this.middleName, previous.middleName)
  compareField("nameChange", this.lastName, previous.lastName)
  compareField("alias", this.alias, previous.alias)

  compareField("dateOfBirth", this.dateOfBirth, previous.dateOfBirth)
  compareAdultChild(this.adultChild, previous.adultChild)
  compareField("sex", this.sex, previous.sex)
  compareField("genderIdentity", this.genderIdentity, previous.genderIdentity)

  if (listOf(
      address1,
      address2,
      address3,
      address4,
      addressPostCode,
    ) != listOf(
      previous.address1,
      previous.address2,
      previous.address3,
      previous.address4,
      previous.addressPostCode,
    )
  ) {
    DeviceWearerChangedMessages.messages["primaryAddressChange"]?.let { messages += it }
  }

  if (listOf(
      secondaryAddress1,
      secondaryAddress2,
      secondaryAddress3,
      secondaryAddress4,
      secondaryAddressPostCode,
    ) != listOf(
      previous.secondaryAddress1,
      previous.secondaryAddress2,
      previous.secondaryAddress3,
      previous.secondaryAddress4,
      previous.secondaryAddressPostCode,
    )
  ) {
    DeviceWearerChangedMessages.messages["secondaryAddressChange"]?.let { messages += it }
  }

  if (listOf(
      tertiaryAddress1,
      tertiaryAddress2,
      tertiaryAddress3,
      tertiaryAddress4,
      tertiaryAddressPostCode,
    ) != listOf(
      previous.tertiaryAddress1,
      previous.tertiaryAddress2,
      previous.tertiaryAddress3,
      previous.tertiaryAddress4,
      previous.tertiaryAddressPostCode,
    )
  ) {
    DeviceWearerChangedMessages.messages["tertiaryAddressChange"]?.let { messages += it }
  }

  compareField("addressPostCode", this.addressPostCode, previous.addressPostCode)
  compareNoFixedAddress(this.noFixedAddress, previous.noFixedAddress)

  compareField("phoneNumber", this.phoneNumber, previous.phoneNumber)

  compareField("mappa", this.mappa, previous.mappa)
  compareField("mappa", this.mappaCategory, previous.mappaCategory)
  compareField("mappa", this.mappaCaseType, previous.mappaCaseType)

  if (listOf(
      pncId,
      nomisId,
      deliusId,
      prisonNumber,
      complianceAndEnforcementPersonReference,
    ) != listOf(
      previous.pncId,
      previous.nomisId,
      previous.deliusId,
      previous.prisonNumber,
      previous.complianceAndEnforcementPersonReference,
    )
  ) {
    DeviceWearerChangedMessages.messages["personalIdChanged"]?.let { messages += it }
  }

  if (listOf(
      interpreterRequired,
      language,
    ) != listOf(
      previous.interpreterRequired,
      previous.language,
    )
  ) {
    DeviceWearerChangedMessages.messages["interpreterRequired"]?.let { messages += it }
  }

  if (listOf(
      responsibleAdultRequired,
      parent,
      guardian,
    ) != listOf(
      previous.responsibleAdultRequired,
      previous.parent,
      previous.guardian,
    )
  ) {
    DeviceWearerChangedMessages.messages["responsibleAdultChanged"]?.let { messages += it }
  }
  compareField("parentPhoneNumber", this.parentPhoneNumber, previous.parentPhoneNumber)

  compareList("disability", this.disability, previous.disability)
  compareList("riskCategory", this.riskCategory, previous.riskCategory)

  return messages
}
