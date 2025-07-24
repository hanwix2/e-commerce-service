package kr.hhplus.be.server.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.server.domain.UserCoupon
import java.time.LocalDateTime

@Schema(description = "발급 받은 쿠폰 정보")
data class IssuedCouponResponse(
    @Schema(description = "유저 ID", example = "12345")
    val userId: Long,

    @Schema(description = "쿠폰 ID", example = "67890")
    val userCouponId: Long,

    @Schema(description = "할인 타입, [예: RATE, PRICE]")
    val discountType: String,

    @Schema(description = "할인 금액", example = "5000")
    val discountAmount: Long,

    @Schema(description = "쿠폰 발급 일시", example = "2025-07-18T12:00:00")
    val issuedAt: LocalDateTime
) {
    companion object {
        fun from(
            userId: Long,
            userCoupon: UserCoupon
        ): IssuedCouponResponse {
            return IssuedCouponResponse(
                userId = userId,
                userCouponId = userCoupon.id,
                discountType = userCoupon.discountType.name,
                discountAmount = userCoupon.discountAmount,
                issuedAt = userCoupon.createdAt
            )
        }
    }
}
