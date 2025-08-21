package kr.hhplus.be.server.application

import kr.hhplus.be.server.domain.Coupon
import kr.hhplus.be.server.domain.UserCoupon
import kr.hhplus.be.server.global.exception.BusinessException
import kr.hhplus.be.server.global.exception.ResponseStatus
import kr.hhplus.be.server.infrastructure.*
import kr.hhplus.be.server.presentation.request.CouponIssueRequest
import kr.hhplus.be.server.presentation.response.IssuedCouponResponse
import org.springframework.stereotype.Service

@Service
class CouponService(
    private val couponRepository: CouponRepository,
    private val userCouponRepository: UserCouponRepository,
    private val userRepository: UserRepository,
    private val couponIssueCountRepository: CouponLeftRepository,
    private val couponIssueUserRepository: RedisCouponIssueUserRepository
) {

    fun issueCoupon(request: CouponIssueRequest): IssuedCouponResponse {

        if (couponIssueUserRepository.add(request.couponId, request.userId) == 0L) {
            throw BusinessException(ResponseStatus.COUPON_ALREADY_ISSUED)
        }

        val couponLeft = couponIssueCountRepository.decrement(request.couponId)

        try {
            val userCoupon = createUserCoupon(request.couponId, request.userId, couponLeft)

            return IssuedCouponResponse.from(userCoupon)
        } catch (e: BusinessException) {
            couponIssueUserRepository.delete(request.couponId, request.userId)
            couponIssueCountRepository.increment(request.couponId)
            throw BusinessException(e.status)
        }
    }

    private fun createUserCoupon(
        couponId: Long,
        userId: Long,
        couponLeft: Long
    ): UserCoupon {
        val coupon = getCoupon(couponId)

        if (couponLeft < 0) {
            throw BusinessException(ResponseStatus.COUPON_OUT_OF_STOCK)
        }

        val user = userRepository.findByIdOrThrow(userId)
        return userCouponRepository.save(UserCoupon.create(coupon, user.id))
    }

    private fun getCoupon(couponId: Long): Coupon {
        val coupon = couponRepository.findByIdOrThrow(couponId)

        if (!coupon.issuable)
            throw BusinessException(ResponseStatus.INVALID_COUPON)

        return coupon
    }
}
