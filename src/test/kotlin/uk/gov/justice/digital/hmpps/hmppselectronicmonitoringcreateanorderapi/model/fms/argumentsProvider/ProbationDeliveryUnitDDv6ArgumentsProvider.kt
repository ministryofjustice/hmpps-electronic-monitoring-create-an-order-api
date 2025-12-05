package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class ProbationDeliveryUnitDDv6ArgumentsProvider : ArgumentsProvider {
  override fun provideArguments(context: ExtensionContext?): Stream<out Arguments?>? = Stream.of(
    // New or changed items for DDv6
    Arguments.of("STAFFORDSHIRE_NORTH", "Staffordshire North"),
    Arguments.of("STAFFORDSHIRE_SOUTH", "Staffordshire South"),
    Arguments.of("PERSONALITY_DISORDER_PROSPER", "Personality Disorder Prosper (West Mids)"),
    Arguments.of("STOCKPORT_AND_TAMESIDE", "Stockport and Tameside"),
    Arguments.of("SALFORD_AND_TRAFFORD", "Salford and Trafford"),

    // Check one original pdu still exists
    Arguments.of("BARKING_AND_DAGENHAM_AND_HAVERING", "Barking and Dagenham and Havering"),
  )
}
