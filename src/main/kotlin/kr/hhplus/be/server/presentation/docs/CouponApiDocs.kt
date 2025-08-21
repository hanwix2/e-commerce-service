package kr.hhplus.be.server.presentation.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import kr.hhplus.be.server.presentation.request.CouponIssueRequest
import kr.hhplus.be.server.presentation.response.IssuedCouponResponse
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.RequestBody

interface CouponApiDocs {

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
                        ),
                        ExampleObject(
                            name = "CouponAlreadyIssued",
                            summary = "이미 발급된 쿠폰",
                            value = """
                            {
                              "code": 4006,
                              "status": "COUPON_ALREADY_ISSUED",
                              "message": "이미 발급된 쿠폰입니다."
                            }
                            """
                        )
                    ]
                )]
            )
        ]
    )
    fun issueCoupon(
        @RequestBody request: CouponIssueRequest
    ): IssuedCouponResponse

}