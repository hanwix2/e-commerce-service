package kr.hhplus.be.server.dto.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "쿠폰 발급 요청")
data class CouponIssueRequest(
    @Schema(description = "발급받을 쿠폰 ID", example = "12345")
    val couponId: Long
)
