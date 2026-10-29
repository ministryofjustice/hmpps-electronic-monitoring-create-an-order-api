package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.support.ParameterDeclarations
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderType
import java.util.stream.Stream

class AlcoholNotifyingOrganisationArgumentsProvider : ArgumentsProvider {
  override fun provideArguments(
    parameterDeclarations: ParameterDeclarations,
    context: ExtensionContext,
  ): Stream<out Arguments?> = Stream.of(
    Arguments.of(NotifyingOrganisationDDv5.CIVIL_COUNTY_COURT, OrderType.CIVIL, "AAMR"),
    Arguments.of(NotifyingOrganisationDDv5.CROWN_COURT, OrderType.BAIL, "AAMR"),
    Arguments.of(NotifyingOrganisationDDv5.MAGISTRATES_COURT, OrderType.BAIL, "AAMR"),
    Arguments.of(NotifyingOrganisationDDv5.MILITARY_COURT, OrderType.BAIL, "AAMR"),
    Arguments.of(NotifyingOrganisationDDv5.PRISON, OrderType.POST_RELEASE, "AML"),
    Arguments.of(NotifyingOrganisationDDv5.HOME_OFFICE, OrderType.IMMIGRATION, "AAMR"),
    Arguments.of(NotifyingOrganisationDDv5.SCOTTISH_COURT, OrderType.BAIL, "AAMR"),
    Arguments.of(NotifyingOrganisationDDv5.FAMILY_COURT, OrderType.BAIL, "AAMR"),
    Arguments.of(NotifyingOrganisationDDv5.PROBATION, OrderType.POST_RELEASE, "AML"),
    Arguments.of(NotifyingOrganisationDDv5.PROBATION, OrderType.COMMUNITY, "AAMR"),
    Arguments.of(NotifyingOrganisationDDv5.YOUTH_COURT, OrderType.PRE_TRIAL, "AAMR"),
    Arguments.of(NotifyingOrganisationDDv5.YOUTH_CUSTODY_SERVICE, OrderType.BAIL, "AAMR"),
  )
}
