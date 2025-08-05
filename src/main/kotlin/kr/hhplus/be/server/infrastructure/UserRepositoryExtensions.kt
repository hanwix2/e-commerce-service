package kr.hhplus.be.server.infrastructure

import kr.hhplus.be.server.domain.User
import kr.hhplus.be.server.global.exception.BusinessException
import kr.hhplus.be.server.global.exception.ResponseStatus
import org.springframework.data.repository.findByIdOrNull

fun UserRepository.findByIdOrThrow(id: Long): User {
    return findByIdOrNull(id)
        ?: throw BusinessException(ResponseStatus.USER_NOT_FOUND)
}