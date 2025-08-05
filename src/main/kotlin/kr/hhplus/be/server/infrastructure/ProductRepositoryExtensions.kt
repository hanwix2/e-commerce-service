package kr.hhplus.be.server.infrastructure

import kr.hhplus.be.server.domain.Product
import kr.hhplus.be.server.global.exception.BusinessException
import kr.hhplus.be.server.global.exception.ResponseStatus
import org.springframework.data.repository.findByIdOrNull

fun ProductRepository.findByIdOrThrow(id: Long): Product {
    return findByIdOrNull(id)
        ?: throw BusinessException(ResponseStatus.PRODUCT_NOT_FOUND)
}