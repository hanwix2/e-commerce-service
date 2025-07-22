package kr.hhplus.be.server.presentation.response

data class OrderResponse(
    val orderId: String,
    val userId: Long,
    val totalPrice: Long,
    val discountAmount: Long,
    val paidAmount: Long,
    val items: List<OrderItemResponse>
)

data class OrderItemResponse(
    val orderItemId: String,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val price: Long
)
