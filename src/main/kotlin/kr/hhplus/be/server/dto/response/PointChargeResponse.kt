package kr.hhplus.be.server.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "포인트 충전 결과")
data class PointChargeResponse(
    @Schema(description = "유저 ID", example = "12345")
    val userId: Long,
    @Schema(description = "충전된 포인트", example = "10000")
    val chargedAmount: Long,
    @Schema(description = "충전 후 총 포인트", example = "150000")
    val totalPoint: Long
)