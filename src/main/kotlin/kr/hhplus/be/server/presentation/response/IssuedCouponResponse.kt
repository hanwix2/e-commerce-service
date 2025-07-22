package kr.hhplus.be.server.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "발급 받은 쿠폰 정보")
data class IssuedCouponResponse(
    @Schema(description = "유저 ID", example = "12345")
    val userId: Long,
    @Schema(description = "쿠폰 ID", example = "67890")
    val couponId: Long,
    @Schema(description = "할인 타입, [예: RATE, PRICE]")
    val discountType: String,
    @Schema(description = "할인 금액", example = "5000")
    val discountAmount: Long,
    @Schema(description = "쿠폰 발급 일시", example = "2025-07-18T12:00:00")
    val issuedAt: LocalDateTime
)
