package kr.hhplus.be.server.application

import kr.hhplus.be.server.domain.OrderItemRepository
import kr.hhplus.be.server.domain.Product
import kr.hhplus.be.server.domain.ProductRepository
import kr.hhplus.be.server.global.exception.BusinessException
import kr.hhplus.be.server.global.exception.ResponseStatus
import kr.hhplus.be.server.presentation.response.ProductResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val orderItemRepository: OrderItemRepository
) {
    companion object {
        const val POPULAR_PRODUCT_LIMIT: Int = 5
        const val POPULAR_PRODUCT_SCAN_DAY_RANGE: Long = 3L
    }

    @Transactional(readOnly = true)
    fun getProductInfo(productId: Long): ProductResponse {
        val product = productRepository.findById(productId)
            .orElseThrow { BusinessException(ResponseStatus.PRODUCT_NOT_FOUND) }

        return ProductResponse.from(product)
    }

    @Transactional(readOnly = true)
    fun getPopularProducts(): List<ProductResponse> {

        val startDate = LocalDateTime.now().minusDays(POPULAR_PRODUCT_SCAN_DAY_RANGE)
        val productIds = orderItemRepository.findTopDistinctPurchasedProductIdsByCreatedAtAfter(POPULAR_PRODUCT_LIMIT, startDate)

        val sortedProducts = getSortedProducts(productIds)

        return sortedProducts.map { ProductResponse.from(it) }
    }

    private fun getSortedProducts(productIds: List<Long>): List<Product> {
        val products = productRepository.findAllByIdIn(productIds)
        val sortedProducts = productIds.mapNotNull { id -> products.find { it.id == id } }
        return sortedProducts
    }

}
