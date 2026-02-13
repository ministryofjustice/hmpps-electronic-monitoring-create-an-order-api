package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import java.util.stream.Stream

class OrderTypeArgumentsProvider : ArgumentsProvider {
  override fun provideArguments(context: ExtensionContext?): Stream<out Arguments?>? = Stream.of(
    Arguments.of(RequestType.REQUEST, ""),
    Arguments.of(RequestType.REQUEST, ""),
    Arguments.of(RequestType.VARIATION, "SR08-Amend monitoring requirements"),
    Arguments.of(RequestType.REJECTED, ""),
    Arguments.of(RequestType.AMEND_ORIGINAL_REQUEST, ""),
    Arguments.of(
      RequestType.REINSTALL_AT_DIFFERENT_ADDRESS,
      "SR05-Install monitoring equipment at an additional address",
    ),
    Arguments.of(RequestType.REINSTALL_DEVICE, "SR04-Re-install monitoring equipment"),
    Arguments.of(RequestType.REVOCATION, "SR21-Revocation monitoring requirements"),
    Arguments.of(RequestType.END_MONITORING, "SR21-Revocation monitoring requirements"),
  )
}
