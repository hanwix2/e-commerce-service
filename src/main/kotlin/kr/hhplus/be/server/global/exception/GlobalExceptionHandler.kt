package kr.hhplus.be.server.global.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = e.status.code,
            status = e.status,
            message = e.status.message
        )
        return ResponseEntity(errorResponse, e.status.httpStatus)
    }

}