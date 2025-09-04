package kr.hhplus.be.server.application

import jakarta.transaction.Transactional
import kr.hhplus.be.server.application.event.UserCouponCreateEvent
import kr.hhplus.be.server.application.producer.CouponMessageProducer
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
    private val couponIssueUserRepository: CouponIssueUserRepository,
    private val userCouponCreateProducer: CouponMessageProducer
) {

    fun issueCoupon(request: CouponIssueRequest): IssuedCouponResponse {

        if (couponIssueUserRepository.add(request.couponId, request.userId) == 0L) {
            throw BusinessException(ResponseStatus.COUPON_ALREADY_ISSUED)
        }

        val couponLeft = couponIssueCountRepository.decrement(request.couponId)

        try {
            validateCouponLeft(couponLeft)
            val user = userRepository.findByIdOrThrow(request.userId)
            val coupon = getCoupon(request.couponId)

            userCouponCreateProducer.sendUserCouponIssueRequest(UserCouponCreateEvent(user.id, coupon.id))

            return IssuedCouponResponse(user.id, coupon.id)
        } catch (e: BusinessException) {
            couponIssueUserRepository.delete(request.couponId, request.userId)
            couponIssueCountRepository.increment(request.couponId)
            throw BusinessException(e.status)
        }
    }

    private fun validateCouponLeft(couponLeft: Long) {
        if (couponLeft < 0) {
            throw BusinessException(ResponseStatus.COUPON_OUT_OF_STOCK)
        }
    }

    @Transactional
    fun createUserCoupon(
        userId: Long,
        couponId: Long
    ) {
        val user = userRepository.findByIdOrThrow(userId)
        val coupon = getCoupon(couponId)

        userCouponRepository.save(UserCoupon.create(coupon, user.id))
    }

    private fun getCoupon(couponId: Long): Coupon {
        val coupon = couponRepository.findByIdOrThrow(couponId)

        if (!coupon.issuable)
            throw BusinessException(ResponseStatus.INVALID_COUPON)

        return coupon
    }

    fun restoreCouponIssue(userId: Long, couponId: Long) {
        couponIssueUserRepository.delete(couponId, userId)
        couponIssueCountRepository.increment(couponId)
    }

}
