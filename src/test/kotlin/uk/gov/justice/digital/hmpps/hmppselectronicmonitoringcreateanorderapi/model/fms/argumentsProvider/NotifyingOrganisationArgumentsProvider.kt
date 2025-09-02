package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class NotifyingOrganisationArgumentsProvider : ArgumentsProvider {
  override fun provideArguments(context: ExtensionContext?): Stream<out Arguments?>? = Stream.of(
    Arguments.of("CIVIL_COUNTY_COURT", "Civil County Court"),
    Arguments.of("CROWN_COURT", "Crown Court"),
    Arguments.of("MAGISTRATES_COURT", "Magistrates Court"),
    Arguments.of("MILITARY_COURT", "Military Court"),
    Arguments.of("PRISON", "Prison"),
    Arguments.of("HOME_OFFICE", "Home Office"),
    Arguments.of("SCOTTISH_COURT", "Scottish Court"),
    Arguments.of("FAMILY_COURT", "Family Court"),
    Arguments.of("PROBATION", "Probation"),
    Arguments.of("YOUTH_COURT", "Youth Court"),
    Arguments.of("YOUTH_CUSTODY_SERVICE", "Youth Custody Service"),
  )
}
