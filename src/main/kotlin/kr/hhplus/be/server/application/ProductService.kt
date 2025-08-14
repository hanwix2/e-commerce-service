package kr.hhplus.be.server.application

import kr.hhplus.be.server.domain.OrderItemStatus
import kr.hhplus.be.server.infrastructure.OrderItemRepository
import kr.hhplus.be.server.domain.Product
import kr.hhplus.be.server.global.cache.CacheName
import kr.hhplus.be.server.global.util.TimeProvider
import kr.hhplus.be.server.infrastructure.ProductRepository
import kr.hhplus.be.server.infrastructure.findByIdOrThrow
import kr.hhplus.be.server.presentation.response.ProductResponse
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val orderItemRepository: OrderItemRepository,
    private val timeProvider: TimeProvider
) {
    companion object {
        const val POPULAR_PRODUCT_LIMIT: Int = 5
        const val POPULAR_PRODUCT_SCAN_DAY_RANGE: Long = 3L
    }

    @Transactional(readOnly = true)
    fun getProductInfo(productId: Long): ProductResponse {
        val product = productRepository.findByIdOrThrow(productId)

        return ProductResponse.from(product)
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = [CacheName.POPULAR_PRODUCTS])
    fun getPopularProducts(): List<ProductResponse> {

        val productIds = getMostPurchasedProductIds()

        val sortedProducts = getSortedProducts(productIds)

        return sortedProducts.map { ProductResponse.from(it) }
    }

    private fun getMostPurchasedProductIds(): List<Long> {
        val currentDate = timeProvider.getCurrentDate()
        val rangeStartAt = currentDate.minusDays(POPULAR_PRODUCT_SCAN_DAY_RANGE).atStartOfDay()
        val rangeEndAt = currentDate.atStartOfDay()

        val productIds = orderItemRepository.findTopDistinctProductIdsByStatusAndCreatedAtRange(
            POPULAR_PRODUCT_LIMIT,
            OrderItemStatus.PURCHASE,
            rangeStartAt,
            rangeEndAt
        )

        return productIds
    }

    private fun getSortedProducts(productIds: List<Long>): List<Product> {
        val products = productRepository.findAllByIdIn(productIds)
        val sortedProducts = productIds.mapNotNull { id -> products.find { it.id == id } }
        return sortedProducts
    }

}
