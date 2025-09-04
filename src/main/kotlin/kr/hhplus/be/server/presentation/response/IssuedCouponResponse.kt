package kr.hhplus.be.server.presentation.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "발급 받은 쿠폰 정보")
data class IssuedCouponResponse(
    @Schema(description = "유저 ID", example = "12345")
    val userId: Long,

    @Schema(description = "쿠폰 ID", example = "67890")
    val couponId: Long
)
