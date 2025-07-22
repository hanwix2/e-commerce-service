package kr.hhplus.be.server.presentation

import kr.hhplus.be.server.presentation.request.ChargePointRequest
import kr.hhplus.be.server.presentation.request.CouponIssueRequest
import kr.hhplus.be.server.presentation.response.PointChargeResponse
import kr.hhplus.be.server.presentation.response.IssuedCouponResponse
import kr.hhplus.be.server.presentation.response.PointResponse
import kr.hhplus.be.server.presentation.docs.UserApiDocs
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1")
class UserController : UserApiDocs {

    @PostMapping("/users/{userId}/points")
    override fun chargePoint(
        @PathVariable userId: Long,
        @RequestBody req: ChargePointRequest
    ): PointChargeResponse {
        return PointChargeResponse(userId, req.amount, 50000)
    }

    @GetMapping("/users/{userId}/points")
    override fun getPoint(
        @PathVariable userId: Long
    ): PointResponse {
        return PointResponse(userId, 50000)
    }

    @PostMapping("/users/{userId}/coupons")
    override fun issueCoupon(
        @PathVariable userId: Long,
        @RequestBody req: CouponIssueRequest
    ): IssuedCouponResponse {
        return IssuedCouponResponse(userId, 123, "RATE", 30, LocalDateTime.now())
    }
}