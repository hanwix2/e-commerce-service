package kr.hhplus.be.server.application

import kr.hhplus.be.server.domain.Product
import kr.hhplus.be.server.global.util.Constant
import kr.hhplus.be.server.infrastructure.ProductRepository
import kr.hhplus.be.server.infrastructure.findByIdOrThrow
import kr.hhplus.be.server.presentation.response.ProductResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val productOrderRankService: ProductOrderRankService
) {

    @Transactional(readOnly = true)
    fun getProductInfo(productId: Long): ProductResponse {
        val product = productRepository.findByIdOrThrow(productId)

        return ProductResponse.from(product)
    }

    @Transactional(readOnly = true)
    fun getPopularProducts(): List<ProductResponse> {

        val productIds = productOrderRankService.getTopNProductsFromUnion(Constant.POPULAR_PRODUCT_LIMIT)

        val sortedProducts = getSortedProducts(productIds)

        return sortedProducts.map { ProductResponse.from(it) }
    }

    private fun getSortedProducts(productIds: List<Long>): List<Product> {
        val products = productRepository.findAllByIdIn(productIds)
        val sortedProducts = productIds.mapNotNull { id -> products.find { it.id == id } }
        return sortedProducts
    }

}
