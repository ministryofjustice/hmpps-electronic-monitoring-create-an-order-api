package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import java.util.*

@Repository
interface AddressRepository : JpaRepository<Address, UUID> {
  fun findByOrderIdAndOrderUsernameAndOrderStatusAndAddressType(
    id: UUID,
    username: String,
    status: OrderStatus,
    addressType: AddressType,
  ): Optional<Address>
}
