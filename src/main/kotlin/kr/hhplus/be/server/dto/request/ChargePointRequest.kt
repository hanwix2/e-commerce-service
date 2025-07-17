package kr.hhplus.be.server.dto.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "포인트 충전 요청")
data class ChargePointRequest(
    @Schema(description = "충전할 포인트 값", example = "10000")
    val amount: Long
)
