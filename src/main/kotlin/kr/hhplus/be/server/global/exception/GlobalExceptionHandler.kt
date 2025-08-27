package kr.hhplus.be.server.global.exception

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<Any> {
        return handleExceptionInternal(e)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<Any> {
        return handleExceptionInternal(ResponseStatus.INVALID_PARAMETER, e.message ?: "잘못된 파라미터입니다.")
    }

    @ExceptionHandler(DistributedLockAcquisitionException::class)
    fun handleDistributedLockAcquisitionException(e: DistributedLockAcquisitionException): ResponseEntity<Any> {
        return handleExceptionInternal(
            ResponseStatus.DISTRIBUTED_LOCK_ACQUISITION_FAILED,
            e.message ?: ResponseStatus.DISTRIBUTED_LOCK_ACQUISITION_FAILED.message
        )
    }

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        return handleExceptionInternal(ex, ResponseStatus.INVALID_PARAMETER)
    }

    private fun handleExceptionInternal(e: BusinessException): ResponseEntity<Any> {
        return handleExceptionInternal(e.status)
    }

    private fun handleExceptionInternal(status: ResponseStatus): ResponseEntity<Any> {
        val errorResponse = makeErrorResponseBody(status)
        return ResponseEntity(errorResponse, status.httpStatus)
    }

    private fun handleExceptionInternal(status: ResponseStatus, message: String): ResponseEntity<Any> {
        val errorResponse = makeErrorResponseBody(status, message)
        return ResponseEntity(errorResponse, status.httpStatus)
    }

    private fun handleExceptionInternal(e: BindException, status: ResponseStatus): ResponseEntity<Any> {
        val errorResponse = makeErrorResponseBody(e, status)
        return ResponseEntity(errorResponse, status.httpStatus)
    }

    private fun makeErrorResponseBody(e: BindException, status: ResponseStatus): ErrorResponse {
        val errorResponse = ErrorResponse(
            status = status,
            message = status.message,
            errors = e.bindingResult.fieldErrors.map { fieldError ->
                ValidationError(
                    field = fieldError.field,
                    message = fieldError.defaultMessage ?: "유효하지 않은 값입니다."
                )
            }
        )
        return errorResponse
    }

    private fun makeErrorResponseBody(status: ResponseStatus): ErrorResponse {
        val errorResponse = ErrorResponse(
            status = status,
            message = status.message
        )
        return errorResponse
    }

    private fun makeErrorResponseBody(status: ResponseStatus, message: String): ErrorResponse {
        val errorResponse = ErrorResponse(
            status = status,
            message = message
        )
        return errorResponse
    }

}