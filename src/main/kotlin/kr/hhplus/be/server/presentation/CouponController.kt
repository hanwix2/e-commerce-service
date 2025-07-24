package kr.hhplus.be.server.presentation

import kr.hhplus.be.server.presentation.docs.CouponApiDocs
import kr.hhplus.be.server.presentation.request.CouponIssueRequest
import kr.hhplus.be.server.presentation.response.IssuedCouponResponse
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/coupons")
class CouponController : CouponApiDocs {

    @PostMapping("/issue")
    override fun issueCoupon(
        @RequestBody request: CouponIssueRequest
    ): IssuedCouponResponse {
        return IssuedCouponResponse(request.userId, 123, "RATE", 30, LocalDateTime.now())
    }

}