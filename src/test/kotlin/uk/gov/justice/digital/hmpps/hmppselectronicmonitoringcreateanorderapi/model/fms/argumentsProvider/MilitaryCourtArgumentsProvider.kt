package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class MilitaryCourtArgumentsProvider : ArgumentsProvider {
  override fun provideArguments(context: ExtensionContext?): Stream<out Arguments?>? = Stream.of(
    Arguments.of("BULFORD_MILITARY_COURT_CENTRE", "Bulford Military Court Centre"),
    Arguments.of("CATTERICK_MILITARY_COURT_CENTRE", "Catterick Military Court Centre"),
  )
}
