package kr.hhplus.be.server.infrastructure

import kr.hhplus.be.server.domain.Coupon
import kr.hhplus.be.server.global.exception.BusinessException
import kr.hhplus.be.server.global.exception.ResponseStatus
import org.springframework.data.repository.findByIdOrNull

fun CouponRepository.findByIdOrThrow(id: Long): Coupon {
    return findByIdOrNull(id)
        ?: throw BusinessException(ResponseStatus.COUPON_NOT_FOUND)
}
