package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class PilotArgumentsProvider : ArgumentsProvider {
  override fun provideArguments(context: ExtensionContext?): Stream<out Arguments?>? = Stream.of(
    Arguments.of("ACQUISITIVE_CRIME_PROJECT", "Acquisitive Crime Project"),
    Arguments.of("DOMESTIC_ABUSE_PERPETRATOR_ON_LICENCE_PROJECT", "Domestic Abuse perpetrators on Licence Project"),
    Arguments.of("LICENCE_VARIATION_PROJECT", "Licence Variation Project"),
    Arguments.of("DOMESTIC_ABUSE_PROTECTION_ORDER", "Domestic Abuse Protection Order (DAPO)"),
    Arguments.of("DOMESTIC_ABUSE_PERPETRATOR_ON_LICENCE_DAPOL", "Domestic Abuse Perpetrator on Licence (DAPOL)"),
    Arguments.of(
      "DOMESTIC_ABUSE_PERPETRATOR_ON_LICENCE_HOME_DETENTION_CURFEW_DAPOL_HDC",
      "Domestic Abuse Perpetrator on Licence Home Detention Curfew (DAPOL HDC)",
    ),
    Arguments.of("GPS_ACQUISITIVE_CRIME_HOME_DETENTION_CURFEW", "GPS Acquisitive Crime Home Detention Curfew"),
    Arguments.of("GPS_ACQUISITIVE_CRIME_PAROLE", "GPS Acquisitive Crime Parole"),
    Arguments.of("UNKNOWN", ""),
  )
}
