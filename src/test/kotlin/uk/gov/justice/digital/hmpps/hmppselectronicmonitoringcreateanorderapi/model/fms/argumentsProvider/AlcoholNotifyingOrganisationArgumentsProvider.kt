package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import java.util.stream.Stream

class AlcoholNotifyingOrganisationArgumentsProvider : ArgumentsProvider {
  override fun provideArguments(context: ExtensionContext?): Stream<out Arguments?>? = Stream.of(
    Arguments.of(NotifyingOrganisationDDv5.CIVIL_COUNTY_COURT, "AAMR"),
    Arguments.of(NotifyingOrganisationDDv5.CROWN_COURT, "AAMR"),
    Arguments.of(NotifyingOrganisationDDv5.MAGISTRATES_COURT, "AAMR"),
    Arguments.of(NotifyingOrganisationDDv5.MILITARY_COURT, "AAMR"),
    Arguments.of(NotifyingOrganisationDDv5.PRISON, "AML"),
    Arguments.of(NotifyingOrganisationDDv5.HOME_OFFICE, "AAMR"),
    Arguments.of(NotifyingOrganisationDDv5.SCOTTISH_COURT, "AAMR"),
    Arguments.of(NotifyingOrganisationDDv5.FAMILY_COURT, "AAMR"),
    Arguments.of(NotifyingOrganisationDDv5.PROBATION, "AML"),
    Arguments.of(NotifyingOrganisationDDv5.YOUTH_COURT, "AAMR"),
    Arguments.of(NotifyingOrganisationDDv5.YOUTH_CUSTODY_SERVICE, "AAMR"),
  )
}
