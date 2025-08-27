package kr.hhplus.be.server.presentation.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import kr.hhplus.be.server.global.exception.ErrorResponse
import kr.hhplus.be.server.presentation.request.OrderRequest
import kr.hhplus.be.server.presentation.response.OrderResponse
import org.springframework.web.bind.annotation.RequestBody

interface OrderApiDocs {

    @Operation(
        summary = "주문/결제 처리",
        description = "유저가 상품을 주문/결제합니다.",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "주문/결제 성공",
                content = [
                    Content(
                        schema = Schema(implementation = OrderResponse::class),
                        examples = [
                            ExampleObject(
                                name = "OrderResponseExample",
                                value = """{
                                "orderId": "123e4567-e89b-12d3-a456-426614174000",
                                "userId": 1,
                                "totalPrice": 30000,
                                "discountAmount": 10000,
                                "paidAmount": 20000,
                                "items": [
                                    {
                                        "orderItemId": "123e4567-e89b-12d3-a456-426614174001",
                                        "productId": 1001,
                                        "productName": "컴퓨터",
                                        "quantity": 1,
                                        "price": 150000
                                    },
                                    {
                                        "orderItemId": "123e4567-e89b-12d3-a456-426614174002",
                                        "productId": 1002,
                                        "productName": "키보드",
                                        "quantity": 1,
                                        "price": 30000
                                    }
                                ]
                            }"""
                            )
                        ]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "404",
                description = "유저/상품/쿠폰 정보 없음",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "UserNotFound",
                            summary = "사용자 없음",
                            value = """
                            {
                              "status": "USER_NOT_FOUND",
                              "message": "사용자를 찾을 수 없습니다."
                            }
                            """
                        ),
                        ExampleObject(
                            name = "ProductNotFound",
                            summary = "상품 정보 없음",
                            value = """
                            {
                              "status": "PRODUCT_NOT_FOUND",
                              "message": "존재하지 않는 상품입니다."
                            }
                            """
                        ),
                        ExampleObject(
                            name = "CouponNotFound",
                            summary = "쿠폰 미존재",
                            value = """
                            {
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
                description = "비즈니스 로직 상 주문 불가(재고/쿠폰/포인트/결제 등)",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "ProductOutOfStock",
                            summary = "상품 재고 없음",
                            value = """
                            {
                              "status": "PRODUCT_OUT_OF_STOCK",
                              "message": "상품의 재고가 부족합니다."
                            }
                            """
                        ),
                        ExampleObject(
                            name = "CouponInvalid",
                            summary = "쿠폰 사용 불가",
                            value = """
                            {
                              "status": "INVALID_COUPON",
                              "message": "쿠폰이 유효하지 않습니다."
                            }
                            """
                        ),
                        ExampleObject(
                            name = "PointNotEnough",
                            summary = "포인트 부족",
                            value = """
                            {
                              "status": "POINT_NOT_ENOUGH",
                              "message": "포인트가 부족합니다."
                            }
                            """
                        ),
                        ExampleObject(
                            name = "OrderFailed",
                            summary = "결제 실패",
                            value = """
                            {
                              "status": "ORDER_FAILED",
                              "message": "주문이 실패했습니다."
                            }
                            """
                        )
                    ]
                )]
            )
        ]
    )
    fun order(@RequestBody request: OrderRequest
    ): OrderResponse

}