package kr.hhplus.be.server.global.util

import kr.hhplus.be.server.global.cache.CacheName
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class Scheduler(
    private val redisCacheManager: RedisCacheManager
) {

    @Scheduled(cron = "0 0 0 * * *")
    fun clearPopularProductsCache() {
        redisCacheManager.getCache(CacheName.POPULAR_PRODUCTS)?.clear()
    }

}