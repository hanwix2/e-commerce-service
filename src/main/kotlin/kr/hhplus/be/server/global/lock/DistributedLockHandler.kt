package kr.hhplus.be.server.global.lock

import io.github.oshai.kotlinlogging.KotlinLogging
import kr.hhplus.be.server.global.exception.DistributedLockAcquisitionException
import org.redisson.RedissonMultiLock
import org.redisson.api.RedissonClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class DistributedLockHandler(
    @Value("\${spring.data.redis.lock.wait-time}") val defaultWaitTime: Long,
    @Value("\${spring.data.redis.lock.lease-time}") val defaultLeaseTime: Long,

    private val redissonClient: RedissonClient,
) {

    private val logger = KotlinLogging.logger {}

    fun <T> executeWithLock(
        lockKeys: List<String>,
        waitTime: Long = defaultWaitTime,
        leaseTime: Long = defaultLeaseTime,
        unit: TimeUnit = TimeUnit.SECONDS,
        action: () -> T
    ): T {
        val locks = lockKeys.map { redissonClient.getLock(it) }
        val multiLock = RedissonMultiLock(*locks.toTypedArray())
        return try {
            val locked = multiLock.tryLock(waitTime, leaseTime, unit)
            if (!locked) throw DistributedLockAcquisitionException(lockKeys.toString())

            action()
        } finally {
            try {
                multiLock.unlock()
            } catch (e: IllegalMonitorStateException) {
                logger.warn { "Lock is not locked: $lockKeys" }
            }
        }
    }

}