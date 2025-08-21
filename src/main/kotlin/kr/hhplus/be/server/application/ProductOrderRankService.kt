package kr.hhplus.be.server.application

import kr.hhplus.be.server.global.cache.CacheKey
import kr.hhplus.be.server.global.cache.CacheName
import kr.hhplus.be.server.global.util.TimeProvider
import kr.hhplus.be.server.infrastructure.RedisProductOrderRankRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class ProductOrderRankService(
    private val productOrderRankRepository: RedisProductOrderRankRepository,
    private val timeProvider: TimeProvider,
) {

    fun addOrderCount(productId: Long, quantity: Int) {
        val currentStringDate = timeProvider.getCurrentIsoStringDate()
        productOrderRankRepository.addOrderedProduct(currentStringDate, productId, quantity)
    }

    fun addUnionOrderCount(numberOfDays: Long) {
        val currentDate = timeProvider.getCurrentDate()
        productOrderRankRepository.saveAggregatedOrderedProductCount(currentDate, numberOfDays)
    }

    @Cacheable(cacheNames = [CacheName.PRODUCTS], key = CacheKey.TOP_N_PRODUCTS)
    fun getTopNProductsFromUnion(size: Int): List<Long> {
        return productOrderRankRepository.getTopNProducts(size)
    }

}