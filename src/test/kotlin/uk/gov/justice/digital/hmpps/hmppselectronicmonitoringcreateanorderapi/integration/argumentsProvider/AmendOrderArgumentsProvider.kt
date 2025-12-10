package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.argumentsProvider

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ServiceRequestType
import java.util.stream.Stream

class AmendOrderArgumentsProvider : ArgumentsProvider {
  override fun provideArguments(context: ExtensionContext?): Stream<out Arguments?>? = Stream.of(
    Arguments.of(ServiceRequestType.REINSTALL_AT_DIFFERENT_ADDRESS, RequestType.REINSTALL_AT_DIFFERENT_ADDRESS),
    Arguments.of(ServiceRequestType.REINSTALL_DEVICE, RequestType.REINSTALL_DEVICE),
    Arguments.of(ServiceRequestType.REVOCATION, RequestType.REVOCATION),
    Arguments.of(ServiceRequestType.VARIATION, RequestType.VARIATION),
  )
}
