package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.formatters

import com.google.i18n.phonenumbers.PhoneNumberUtil

class PhoneNumberFormatter {
  companion object {
    fun formatAsInternationalDirectDialingNumber(phoneNumber: String): String {
      if (phoneNumber == "") {
        return ""
      }

      val phoneUtil = PhoneNumberUtil.getInstance()
      val region = if (phoneNumber.startsWith("+")) "" else "GB"
      val number = phoneUtil.parse(phoneNumber, region)
      val countryCode = number.countryCode
      val nationalSignificantNumber = phoneUtil.getNationalSignificantNumber(number)

      // phoneUtil.getMetadataForRegion is private which makes it hard to "correctly" get the
      // Internation Direct Dialling(IDD) prefix for a given region. Given the Electronic Monitoring
      // programme only monitors UK device wearers from UK contact centres,
      // it should be safe to always use the UK IDD prefix.
      val iddPrefix = "00"

      return "$iddPrefix$countryCode$nationalSignificantNumber"
    }
  }
}
