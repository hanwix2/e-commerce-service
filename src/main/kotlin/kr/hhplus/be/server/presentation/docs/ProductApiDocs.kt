package kr.hhplus.be.server.presentation.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import kr.hhplus.be.server.presentation.response.ProductResponse
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.PathVariable

interface ProductApiDocs {

    @Operation(
        summary = "상품 조회",
        description = "상품 ID로 상품 정보를 조회합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "상품 조회 성공",
                content = [Content(
                    schema = Schema(implementation = ProductResponse::class),
                    examples = [
                        ExampleObject(
                            name = "ProductResponseExample",
                            value = """
                            {"productId": 1001, "productName": "컴퓨터", "price": 1500000, "stock": 30}
                            """
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 상품",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "ProductNotFound",
                            value = """{
                                "status": "PRODUCT_NOT_FOUND",
                                "message": "존재하지 않는 상품입니다."
                            }"""
                        )
                    ]
                )]
            )
        ]
    )
    fun getProduct(
        @PathVariable productId: Long
    ): ProductResponse

    @Operation(
        summary = "인기 판매 상품 조회",
        description = "최근 3일간 판매량이 높은 상위 5개 상품을 조회합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "인기 판매 상품 조회 성공",
                content = [Content(
                    schema = Schema(implementation = ProductResponse::class),
                    examples = [
                        ExampleObject(
                            name = "ProductResponseExample",
                            value = """
                            [
                                {"productId": 1001, "productName": "컴퓨터", "price": 1500000, "stock": 30},
                                {"productId": 1002, "productName": "키보드", "price": 30000, "stock": 20},
                                {"productId": 1003, "productName": "마우스", "price": 15000, "stock": 50},
                                {"productId": 1004, "productName": "모니터", "price": 250000, "stock": 10},
                                {"productId": 1005, "productName": "헤드셋", "price": 50000, "stock": 15}
                            ]
                            """
                        )
                    ]
                )]
            )
        ]
    )
    fun getPopularProducts(): List<ProductResponse>

}