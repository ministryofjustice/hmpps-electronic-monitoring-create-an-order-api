package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.support.ParameterDeclarations
import java.util.stream.Stream

class MilitaryCourtArgumentsProvider : ArgumentsProvider {
  override fun provideArguments(
    parameterDeclarations: ParameterDeclarations,
    context: ExtensionContext,
  ): Stream<out Arguments?> = Stream.of(
    Arguments.of("BULFORD_MILITARY_COURT_CENTRE", "Bulford Military Court Centre"),
    Arguments.of("CATTERICK_MILITARY_COURT_CENTRE", "Catterick Military Court Centre"),
  )
}
