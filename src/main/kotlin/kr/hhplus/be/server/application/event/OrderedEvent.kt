package kr.hhplus.be.server.application.event

import kr.hhplus.be.server.presentation.response.OrderResponse

class OrderedEvent(
    val userId: Long,
    val totalPrice: Long,
    val discountAmount: Long,
    val paidAmount: Long,
    val items: List<OrderedEventItem>
) {
    companion object {
        fun from(orderResponse: OrderResponse): OrderedEvent {
            return OrderedEvent(
                userId = orderResponse.userId,
                totalPrice = orderResponse.totalPrice,
                discountAmount = orderResponse.discountAmount,
                paidAmount = orderResponse.paidAmount,
                items = orderResponse.items.map {
                    OrderedEventItem(
                        productName = it.productName,
                        quantity = it.quantity,
                        price = it.price
                    )
                }
            )
        }
    }
}

class OrderedEventItem(
    val productName: String,
    val quantity: Int,
    val price: Long
)
