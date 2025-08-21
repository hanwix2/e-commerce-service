package kr.hhplus.be.server.global.util

import kr.hhplus.be.server.application.ProductOrderRankService
import kr.hhplus.be.server.global.cache.CacheKey
import kr.hhplus.be.server.global.cache.CacheName
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class Scheduler(
    private val redisCacheManager: RedisCacheManager,
    private val productOrderRankService: ProductOrderRankService,
) {

    @Scheduled(cron = "0 0 1 * * *")
    fun refreshPopularProductsCache() {
        productOrderRankService.addUnionOrderCount(Constant.POPULAR_PRODUCT_SCAN_DAY_RANGE)
        val topNProductIds = productOrderRankService.getTopNProductsFromUnion(Constant.POPULAR_PRODUCT_LIMIT)

        redisCacheManager.getCache(CacheName.PRODUCTS)?.put(CacheKey.TOP_N_PRODUCTS, topNProductIds)
    }

}