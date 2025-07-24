package kr.hhplus.be.server.presentation

import kr.hhplus.be.server.presentation.docs.UserApiDocs
import kr.hhplus.be.server.presentation.request.ChargePointRequest
import kr.hhplus.be.server.presentation.response.PointChargeResponse
import kr.hhplus.be.server.presentation.response.PointResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users")
class UserController : UserApiDocs {

    @PostMapping("/{userId}/points")
    override fun chargePoint(
        @PathVariable userId: Long,
        @RequestBody request: ChargePointRequest
    ): PointChargeResponse {
        return PointChargeResponse(userId, request.amount, 50000)
    }

    @GetMapping("/{userId}/points")
    override fun getPoint(
        @PathVariable userId: Long
    ): PointResponse {
        return PointResponse(userId, 50000)
    }

}