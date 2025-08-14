package kr.hhplus.be.server.application

import kr.hhplus.be.server.domain.*
import kr.hhplus.be.server.global.exception.BusinessException
import kr.hhplus.be.server.global.exception.ResponseStatus
import kr.hhplus.be.server.infrastructure.CouponRepository
import kr.hhplus.be.server.infrastructure.UserCouponRepository
import kr.hhplus.be.server.infrastructure.UserRepository
import kr.hhplus.be.server.infrastructure.findByIdOrThrow
import kr.hhplus.be.server.presentation.request.CouponIssueRequest
import kr.hhplus.be.server.presentation.response.IssuedCouponResponse
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class CouponService(
    private val couponRepository: CouponRepository,
    private val userCouponRepository: UserCouponRepository,
    private val userRepository: UserRepository
) {

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Retryable(
        value = [ObjectOptimisticLockingFailureException::class],
        maxAttempts = 3,
        backoff = Backoff(maxDelay = 1000L, random = true)
    )
    fun issueCoupon(request: CouponIssueRequest): IssuedCouponResponse {
        val user = userRepository.findByIdOrThrow(request.userId)

        val coupon = getCoupon(request.couponId)
        val userCoupon = userCouponRepository.save(UserCoupon.create(coupon, user.id))

        coupon.decreaseIssuedRemain()
        couponRepository.save(coupon)

        return IssuedCouponResponse.from(
            userId = user.id,
            userCoupon = userCoupon
        )
    }

    private fun getCoupon(couponId: Long): Coupon {
        val coupon = couponRepository.findByIdOrThrow(couponId)

        if (!coupon.issuable)
            throw BusinessException(ResponseStatus.INVALID_COUPON)

        if (coupon.issuedRemain <= 0)
            throw BusinessException(ResponseStatus.COUPON_OUT_OF_STOCK)

        return coupon
    }
}
