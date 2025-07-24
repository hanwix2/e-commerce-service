package kr.hhplus.be.server.global.exception

class BusinessException(responseStatus: ResponseStatus) : RuntimeException() {
    val status: ResponseStatus = responseStatus
}