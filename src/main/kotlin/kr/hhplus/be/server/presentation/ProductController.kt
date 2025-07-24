package kr.hhplus.be.server.presentation

import kr.hhplus.be.server.application.ProductService
import kr.hhplus.be.server.presentation.response.ProductResponse
import kr.hhplus.be.server.presentation.docs.ProductApiDocs
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/products")
class ProductController(
    private val productService: ProductService
) : ProductApiDocs {

    @GetMapping("/{productId}")
    override fun getProduct(
        @PathVariable productId: Long
    ): ProductResponse {
        return productService.getProductInfo(productId)
    }

    @GetMapping("/popular")
    override fun getPopularProducts(): List<ProductResponse> {
        return listOf(
            ProductResponse(1001, "컴퓨터", 1500000, 30),
            ProductResponse(1002, "키보드", 30000, 20),
            ProductResponse(1003, "마우스", 15000, 50),
            ProductResponse(1004, "모니터", 250000, 10),
            ProductResponse(1005, "헤드셋", 50000, 15)
        )
    }
}