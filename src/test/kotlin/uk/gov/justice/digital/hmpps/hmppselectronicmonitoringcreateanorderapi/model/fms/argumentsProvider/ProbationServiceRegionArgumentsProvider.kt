package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class ProbationServiceRegionArgumentsProvider : ArgumentsProvider {
  override fun provideArguments(context: ExtensionContext?): Stream<out Arguments?>? = Stream.of(
    Arguments.of("NORTH_EAST", "North East"),
    Arguments.of("NORTH_WEST", "North West"),
    Arguments.of("YORKSHIRE_AND_THE_HUMBER", "Yorkshire and the Humber"),
    Arguments.of("GREATER_MANCHESTER", "Greater Manchester"),
    Arguments.of("EAST_MIDLANDS", "East Midlands"),
    Arguments.of("WALES", "Wales"),
    Arguments.of("WEST_MIDLANDS", "West Midlands"),
    Arguments.of("EAST_OF_ENGLAND", "East of England"),
    Arguments.of("SOUTH_WEST", "South West"),
    Arguments.of("SOUTH_CENTRAL", "South Central"),
    Arguments.of("LONDON", "London"),
    Arguments.of("KENT_SURREY_SUSSEX", "Kent, Surrey & Sussex"),
  )
}
