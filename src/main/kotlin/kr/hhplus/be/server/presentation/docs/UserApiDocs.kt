package kr.hhplus.be.server.presentation.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import kr.hhplus.be.server.presentation.request.ChargePointRequest
import kr.hhplus.be.server.presentation.request.CouponIssueRequest
import kr.hhplus.be.server.presentation.response.IssuedCouponResponse
import kr.hhplus.be.server.presentation.response.PointChargeResponse
import kr.hhplus.be.server.presentation.response.PointResponse
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

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
                                    "code": 404,
                                    "status": "NOT_FOUND",
                                    "message": "존재하지 않는 유저입니다."
                                }"""
                                )
                                )
                    ]
                )]
            ),
        ]
    )
    fun chargePoint(
        userId: Long, req: ChargePointRequest
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
                                "code": 404,
                                "status": "NOT_FOUND",
                                "message": "존재하지 않는 유저입니다."
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

    @Operation(
        summary = "선착순 쿠폰 발급",
        description = "선착순 방식으로 사용자의 쿠폰 발급을 시도합니다.",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "쿠폰 발급 성공",
                content = [Content(schema = Schema(implementation = IssuedCouponResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "유저/쿠폰 정보 없음",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "UserNotFound",
                            summary = "사용자 없음",
                            value = """
                            {
                              "code": 1001,
                              "status": "USER_NOT_FOUND",
                              "message": "사용자를 찾을 수 없습니다."
                            }
                            """
                        ),
                        ExampleObject(
                            name = "CouponNotFound",
                            summary = "쿠폰 정보 없음",
                            value = """
                            {
                              "code": 4001,
                              "status": "COUPON_NOT_FOUND",
                              "message": "쿠폰을 찾을 수 없습니다."
                            }
                            """
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "409",
                description = "쿠폰 발급 불가/소진/실패",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "CouponInvalid",
                            summary = "쿠폰 발급 불가 상태",
                            value = """
                            {
                              "code": 4002,
                              "status": "INVALID_COUPON",
                              "message": "쿠폰이 유효하지 않습니다."
                            }
                            """
                        ),
                        ExampleObject(
                            name = "CouponOutOfStock",
                            summary = "쿠폰 소진(마감)",
                            value = """
                            {
                              "code": 4004,
                              "status": "COUPON_OUT_OF_STOCK",
                              "message": "쿠폰이 모두 소진되었습니다."
                            }
                            """
                        ),
                        ExampleObject(
                            name = "CouponIssueFailed",
                            summary = "쿠폰 발급 실패(기타 서버 오류/레이스 등)",
                            value = """
                            {
                              "code": 4005,
                              "status": "COUPON_ISSUE_FAILED",
                              "message": "쿠폰 발급에 실패했습니다."
                            }
                            """
                        )
                    ]
                )]
            )
        ]
    )
    fun issueCoupon(
        @PathVariable userId: Long, @RequestBody req: CouponIssueRequest
    ): IssuedCouponResponse

}