package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class YouthCustodyServiceRegionArgumentsProviderDDv6 : ArgumentsProvider {
  override fun provideArguments(context: ExtensionContext?): Stream<out Arguments?>? = Stream.of(
    Arguments.of("SOUTH_WEST_AND_SOUTH_CENTRAL", "South West and South Central"),
    Arguments.of("EAST_AND_SOUTH_EAST", "East and South East"),
    Arguments.of("LONDON", "London"),
    Arguments.of("MIDLANDS", "Midlands"),
    Arguments.of("NORTH_EAST_AND_CUMBRIA", "North East and Cumbria"),
    Arguments.of("NORTH_WEST", "North West"),
    Arguments.of("WALES", "Wales"),
    Arguments.of("YORKSHIRE_AND_HUMBERSIDE", "Yorkshire and Humberside"),
  )
}
