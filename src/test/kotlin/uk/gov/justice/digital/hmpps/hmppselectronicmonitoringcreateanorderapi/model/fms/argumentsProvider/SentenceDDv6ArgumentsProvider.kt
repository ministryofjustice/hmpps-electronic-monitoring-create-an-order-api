package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.support.ParameterDeclarations
import java.util.stream.Stream

class SentenceDDv6ArgumentsProvider : ArgumentsProvider {
  override fun provideArguments(
    parameterDeclarations: ParameterDeclarations,
    context: ExtensionContext,
  ): Stream<out Arguments?> = Stream.of(
    // Changed item for DDv6
    Arguments.of("IPP", "IPP (Imprisonment for Public Protection)"),

    // Check one original sentence type still exists
    Arguments.of("LIFE_SENTENCE", "Life Sentence"),
  )
}
