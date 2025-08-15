package kr.hhplus.be.server.presentation

import kr.hhplus.be.server.application.CouponWithLockService
import kr.hhplus.be.server.presentation.docs.CouponApiDocs
import kr.hhplus.be.server.presentation.request.CouponIssueRequest
import kr.hhplus.be.server.presentation.response.IssuedCouponResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/coupons")
class CouponController(
    private val couponWithLockService: CouponWithLockService
) : CouponApiDocs {

    @PostMapping("/issue")
    override fun issueCoupon(
        @RequestBody request: CouponIssueRequest
    ): IssuedCouponResponse {
        return couponWithLockService.issueCoupon(request)
    }

}