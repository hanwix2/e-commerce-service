package kr.hhplus.be.server.infrastructure

import kr.hhplus.be.server.global.cache.KeyName
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository

@Repository
class RedisCouponLeftRepository(
    private val redisTemplate: StringRedisTemplate,
) : CouponLeftRepository {

    override fun increment(couponId: Long): Long {
        return redisTemplate.opsForValue()
            .increment(KeyName.COUPON_LEFT + couponId) ?: 0L
    }

    override fun decrement(couponId: Long): Long {
        return redisTemplate.opsForValue()
            .decrement(KeyName.COUPON_LEFT + couponId) ?: 0L
    }

}