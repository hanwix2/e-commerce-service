package kr.hhplus.be.server.application

import kr.hhplus.be.server.global.lock.DistributedLockHandler
import kr.hhplus.be.server.global.lock.LockResource
import kr.hhplus.be.server.presentation.request.CouponIssueRequest
import kr.hhplus.be.server.presentation.response.IssuedCouponResponse
import org.springframework.stereotype.Service

@Service
class CouponWithLockService(
    private val couponService: CouponService,
    private val distributedLockHandler: DistributedLockHandler
) {

    fun issueCoupon(request: CouponIssueRequest): IssuedCouponResponse {
        return distributedLockHandler.executeWithLock(
            listOf(LockResource.COUPON + request.couponId)
        ) {
            couponService.issueCoupon(request)
        }
    }

}