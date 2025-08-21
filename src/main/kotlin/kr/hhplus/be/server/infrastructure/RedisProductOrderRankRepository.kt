package kr.hhplus.be.server.infrastructure

import kr.hhplus.be.server.global.cache.KeyName
import kr.hhplus.be.server.global.cache.TimeToLive
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Repository
class RedisProductOrderRankRepository(
    private val redisTemplate: RedisTemplate<String, String>,
    private val stringRedisTemplate: StringRedisTemplate
) {

    fun addOrderedProduct(currentDate: String, productId: Long, quantity: Int) {
        val key = KeyName.PURCHASED_PRODUCT_COUNT_OF_DATE + currentDate
        val duration = Duration.ofDays(TimeToLive.PURCHASED_PRODUCT_COUNT_OF_DATE)

        redisTemplate.opsForZSet()
            .incrementScore(key, productId.toString(), quantity.toDouble())
        redisTemplate.expire(key, duration)
    }

    fun saveAggregatedOrderedProductCount(currentDate: LocalDate, numberOfDays: Long) {
        val orderedProductKeys = (0 until numberOfDays).map {
            KeyName.PURCHASED_PRODUCT_COUNT_OF_DATE + currentDate.minusDays(it).format(DateTimeFormatter.BASIC_ISO_DATE)
        }

        val key = KeyName.AGGREGATED_PURCHASED_PRODUCT_COUNT

        stringRedisTemplate.execute { connection ->
            val keysByte = orderedProductKeys.map { it.toByteArray() }.toTypedArray()

            connection.zSetCommands().zUnionStore(key.toByteArray(), *keysByte)
            connection.keyCommands().expire(key.toByteArray(), TimeToLive.AGGREGATED_PURCHASED_PRODUCT_COUNT * 60 * 60)
            null
        }
    }

    fun getTopNProducts(size: Int): List<Long> {
        return redisTemplate.opsForZSet()
            .reverseRange(KeyName.AGGREGATED_PURCHASED_PRODUCT_COUNT, 0, (size - 1).toLong())
            ?.map { it.toLong() }
            ?: emptyList()
    }

}