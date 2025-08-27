package kr.hhplus.be.server.presentation.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import kr.hhplus.be.server.presentation.request.ChargePointRequest
import kr.hhplus.be.server.presentation.response.PointChargeResponse
import kr.hhplus.be.server.presentation.response.PointResponse
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.PathVariable

interface UserApiDocs {

    @Operation(
        summary = "유저의 포인트 충전",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "포인트 충전 성공",
                content = [Content(
                    schema = Schema(implementation = PointChargeResponse::class),
                    examples = [
                        ExampleObject(
                            name = "PointChargeResponseExample",
                            value = """{
                                "userId": 1,
                                "chargedAmount": 50000,
                                "totalPoint": 150000
                            }"""
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 유저",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        (
                                ExampleObject(
                                    name = "UserNotFound",
                                    value = """{
                                    "status": "USER_NOT_FOUND",
                                    "message": "사용자를 찾을 수 없습니다."
                                }"""
                                )
                                )
                    ]
                )]
            ),
        ]
    )
    fun chargePoint(
        userId: Long, request: ChargePointRequest
    ): PointChargeResponse

    @Operation(
        summary = "유저의 포인트 조회",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "포인트 조회 성공",
                content = [Content(
                    schema = Schema(implementation = PointResponse::class),
                    examples = [
                        ExampleObject(
                            name = "PointResponseExample",
                            value = """{
                                "userId": 1,
                                "totalPoint": 50000
                            }"""
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 유저",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "UserNotFound",
                            value = """{
                                "status": "USER_NOT_FOUND",
                                "message": "사용자를 찾을 수 없습니다."
                            }"""
                        )
                    ]
                )]
            )
        ]
    )
    fun getPoint(
        @PathVariable userId: Long
    ): PointResponse

}