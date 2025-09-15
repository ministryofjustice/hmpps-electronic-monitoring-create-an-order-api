package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class SentenceArgumentsProvider : ArgumentsProvider {
  override fun provideArguments(context: ExtensionContext?): Stream<out Arguments?>? = Stream.of(
    Arguments.of("EXTENDED_DETERMINATE_SENTENCE", "Extended Determinate Sentence"),
    Arguments.of("IPP", "Imprisonment for Public Protection (IPP)"),
    Arguments.of("LIFE_SENTENCE", "Life Sentence"),
    Arguments.of("SOPC", "Section 236A Special Custodial Sentences for Offenders of Particular Concern (SOPC)"),
    Arguments.of("EPP", "Section 227/228 Extended Sentence for Public Protection (EPP)"),
    Arguments.of("SECTION_85_EXTENDED_SENTENCES", "Section 85 Extended Sentences"),
    Arguments.of("STANDARD_DETERMINATE_SENTENCE", "Standard Determinate Sentence"),
    Arguments.of("DTO", "Detention & Training Order"),
    Arguments.of("SECTION_91", "HDC (Section 91)"),
  )
}
