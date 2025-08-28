package kr.hhplus.be.server.infrastructure

import kr.hhplus.be.server.global.cache.KeyName
import kr.hhplus.be.server.global.cache.TimeToLive
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Repository
class RedisProductOrderRankRepository(
    private val redisTemplate: StringRedisTemplate
) {

    fun addOrderedProduct(currentDate: String, productId: Long, quantity: Int) {
        val key = KeyName.PURCHASED_PRODUCT_COUNT_OF_DATE + currentDate
        val duration = Duration.ofDays(TimeToLive.PURCHASED_PRODUCT_COUNT_OF_DATE)

        redisTemplate.opsForZSet()
            .incrementScore(key, productId.toString(), quantity.toDouble())
        redisTemplate.expire(key, duration)
    }

    fun saveAggregatedOrderedProductCount(baseDate: LocalDate, numberOfDays: Long) {
        val baseOrderedProductKey =
            KeyName.PURCHASED_PRODUCT_COUNT_OF_DATE + baseDate.format(DateTimeFormatter.BASIC_ISO_DATE)
        val orderedProductKeys = (1 until numberOfDays).map {
            KeyName.PURCHASED_PRODUCT_COUNT_OF_DATE + baseDate.minusDays(it).format(DateTimeFormatter.BASIC_ISO_DATE)
        }

        val unionKey = KeyName.AGGREGATED_PURCHASED_PRODUCT_COUNT

        redisTemplate.opsForZSet()
            .unionAndStore(baseOrderedProductKey, orderedProductKeys, unionKey)
        redisTemplate.expire(unionKey, Duration.ofDays(TimeToLive.AGGREGATED_PURCHASED_PRODUCT_COUNT))
    }

    fun getTopNProducts(size: Int): List<Long> {
        return redisTemplate.opsForZSet()
            .reverseRange(KeyName.AGGREGATED_PURCHASED_PRODUCT_COUNT, 0, (size - 1).toLong())
            ?.map { it.toLong() }
            ?: emptyList()
    }

}