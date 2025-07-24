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
        return productService.getPopularProducts()
    }
}