package kr.hhplus.be.server.domain

import kr.hhplus.be.server.global.exception.BusinessException
import kr.hhplus.be.server.global.exception.ResponseStatus
import org.springframework.data.repository.findByIdOrNull

fun UserRepository.findByIdOrThrow(id: Long): User {
    return findByIdOrNull(id)
        ?: throw BusinessException(ResponseStatus.USER_NOT_FOUND)
}