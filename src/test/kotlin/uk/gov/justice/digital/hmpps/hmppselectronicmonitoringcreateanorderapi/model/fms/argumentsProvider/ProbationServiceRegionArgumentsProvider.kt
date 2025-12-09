package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class ProbationServiceRegionArgumentsProvider : ArgumentsProvider {
  override fun provideArguments(context: ExtensionContext?): Stream<out Arguments?>? = Stream.of(
    Arguments.of("NORTH_EAST", "Probation Board"),
    Arguments.of("NORTH_WEST", "Probation Board"),
    Arguments.of("YORKSHIRE_AND_THE_HUMBER", "Probation Board"),
    Arguments.of("GREATER_MANCHESTER", "Probation Board"),
    Arguments.of("EAST_MIDLANDS", "Probation Board"),
    Arguments.of("WALES", "Probation Board"),
    Arguments.of("WEST_MIDLANDS", "Probation Board"),
    Arguments.of("EAST_OF_ENGLAND", "East Probation Board"),
    Arguments.of("SOUTH_WEST", "Probation Board"),
    Arguments.of("SOUTH_CENTRAL", "Probation Board"),
    Arguments.of("LONDON", "Probation Board"),
    Arguments.of("KENT_SURREY_SUSSEX", "Probation Board"),
  )
}
