package kr.hhplus.be.server.presentation.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Min

@Schema(description = "주문 요청")
data class OrderRequest(
    @Schema(description = "유저 ID", example = "1")
    val userId: Long,

    @Schema(description = "적용할 쿠폰 ID", example = "123")
    val userCouponId: Long? = null,

    @Schema(description = "주문할 상품 목록")
    @field:Valid
    val orderItems: List<OrderItemRequest>
)

@Schema(description = "주문 항목")
data class OrderItemRequest(
    @Schema(description = "상품 ID", example = "123456")
    val productId: Long,

    @Schema(description = "구매 수량", example = "1")
    @field:Min(1)
    val quantity: Int
)
