package kr.hhplus.be.server.domain

import kr.hhplus.be.server.global.exception.BusinessException
import kr.hhplus.be.server.global.exception.ResponseStatus

fun UserCouponRepository.findByIdOrThrow(id: Long, userId: Long): UserCoupon {
    return findByIdAndUserId(id, userId)
        ?: throw BusinessException(ResponseStatus.COUPON_NOT_FOUND)
}
