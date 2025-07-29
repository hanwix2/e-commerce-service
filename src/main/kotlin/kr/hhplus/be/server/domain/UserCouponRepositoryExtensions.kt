package kr.hhplus.be.server.domain

import kr.hhplus.be.server.global.exception.BusinessException
import kr.hhplus.be.server.global.exception.ResponseStatus
import org.springframework.data.repository.findByIdOrNull

fun UserCouponRepository.findByIdOrThrow(id: Long): UserCoupon {
    return findByIdOrNull(id)
        ?: throw BusinessException(ResponseStatus.COUPON_NOT_FOUND)
}
