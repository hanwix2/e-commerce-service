package kr.hhplus.be.server

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import kr.hhplus.be.server.dto.response.ProductResponse
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class ProductController {

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
                                "code": 2001,
                                "status": "PRODUCT_NOT_FOUND",
                                "message": "존재하지 않는 상품입니다."
                            }"""
                        )
                    ]
                )]
            )
        ]
    )
    @GetMapping("/products/{productId}")
    fun getProduct(@PathVariable productId: Long): ProductResponse {
        return ProductResponse(productId, "컴퓨터", 1500000, 30)
    }

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
    @GetMapping("/products/popular")
    fun getPopularProducts(): List<ProductResponse> {
        return listOf(
            ProductResponse(1001, "컴퓨터", 1500000, 30),
            ProductResponse(1002, "키보드", 30000, 20),
            ProductResponse(1003, "마우스", 15000, 50),
            ProductResponse(1004, "모니터", 250000, 10),
            ProductResponse(1005, "헤드셋", 50000, 15)
        )
    }
}