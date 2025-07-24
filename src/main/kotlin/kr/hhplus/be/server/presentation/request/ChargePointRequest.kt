package kr.hhplus.be.server.presentation.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min

@Schema(description = "포인트 충전 요청")
data class ChargePointRequest(

    @Schema(description = "충전할 포인트 값", example = "10000", minimum = "1")
    @Min(1)
    val amount: Long

)
