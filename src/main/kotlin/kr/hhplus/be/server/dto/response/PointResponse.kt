package kr.hhplus.be.server.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "포인트 정보")
data class PointResponse(
    @Schema(description = "유저 ID", example = "12345")
    val userId: Long,
    @Schema(description = "포인트 보유", example = "150000")
    val totalPoint: Long
)
