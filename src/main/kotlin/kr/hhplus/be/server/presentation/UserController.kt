package kr.hhplus.be.server.presentation

import jakarta.validation.Valid
import kr.hhplus.be.server.application.UserService
import kr.hhplus.be.server.presentation.docs.UserApiDocs
import kr.hhplus.be.server.presentation.request.ChargePointRequest
import kr.hhplus.be.server.presentation.response.PointChargeResponse
import kr.hhplus.be.server.presentation.response.PointResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) : UserApiDocs {

    @PostMapping("/{userId}/points")
    override fun chargePoint(
        @PathVariable userId: Long,
        @Valid @RequestBody request: ChargePointRequest
    ): PointChargeResponse {
        return userService.chargePoint(userId, request.amount)
    }

    @GetMapping("/{userId}/points")
    override fun getPoint(
        @PathVariable userId: Long
    ): PointResponse {
        return PointResponse(userId, 50000)
    }

}