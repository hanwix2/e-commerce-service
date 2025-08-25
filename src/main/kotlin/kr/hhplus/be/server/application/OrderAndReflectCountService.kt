package kr.hhplus.be.server.application

import kr.hhplus.be.server.presentation.request.OrderRequest
import kr.hhplus.be.server.presentation.response.OrderResponse
import org.springframework.stereotype.Service

@Service
class OrderAndReflectCountService(
    private val orderWithLockService: OrderWithLockService,
    private val productOrderRankService: ProductOrderRankService,
) {

    fun order(request: OrderRequest): OrderResponse {

        val orderResponse = orderWithLockService.order(request)

        orderResponse.items.forEach { item ->
            productOrderRankService.addOrderCount(item.productId, item.quantity)
        }

        return orderResponse
    }

}