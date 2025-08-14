package kr.hhplus.be.server.application

import kr.hhplus.be.server.global.lock.DistributedLockHandler
import kr.hhplus.be.server.global.lock.LockResource
import kr.hhplus.be.server.presentation.request.OrderRequest
import kr.hhplus.be.server.presentation.response.OrderResponse
import org.springframework.stereotype.Service

@Service
class OrderWithLockService(
    private val orderService: OrderService,
    private val distributedLockHandler: DistributedLockHandler
) {

    fun order(request: OrderRequest): OrderResponse {
        return distributedLockHandler.executeWithLock(
            getLockKeysFromOrderRequest(request)
        ) {
            orderService.order(request)
        }
    }

    private fun getLockKeysFromOrderRequest(request: OrderRequest): MutableList<String> {
        val lockKeys = mutableListOf<String>()

        lockKeys.add(LockResource.USER + request.userId)

        request.userCouponId?.let {
            lockKeys.add(LockResource.USER_COUPON + it)
        }

        request.orderItems
            .sortedBy { it.productId }
            .forEach { product ->
                lockKeys.add(LockResource.PRODUCT + product.productId)
            }
        return lockKeys
    }

}