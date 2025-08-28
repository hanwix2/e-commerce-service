package kr.hhplus.be.server.infrastructure

import kr.hhplus.be.server.global.cache.KeyName
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository

@Repository
class RedisCouponIssueUserRepository(
    private val redisTemplate: StringRedisTemplate,
): CouponIssueUserRepository {

    override fun add(couponId: Long, userId: Long): Long {
        return redisTemplate.opsForSet()
            .add(KeyName.COUPON_ISSUED_USER + couponId, userId.toString()) ?: 0L
    }

    override fun delete(couponId: Long, userId: Long): Long {
        return redisTemplate.opsForSet()
            .remove(KeyName.COUPON_ISSUED_USER + couponId, userId.toString()) ?: 0L
    }

}