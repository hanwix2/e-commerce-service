package kr.hhplus.be.server.global.exception

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "공통 에러 응답")
data class ErrorResponse(
    @Schema(description = "에러 코드", example = "1001")
    val code: Int,

    @Schema(description = "에러 상태", example = "USER_NOT_FOUND")
    val status: ResponseStatus,

    @Schema(description = "에러 메시지", example = "사용자를 찾을 수 없습니다.")
    val message: String,

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val errors: List<ValidationError>? = null
)

data class ValidationError (
    @Schema(description = "필드명", example = "userId")
    val field: String,

    @Schema(description = "오류 메시지", example = "유효하지 않은 사용자 ID입니다.")
    val message: String
)