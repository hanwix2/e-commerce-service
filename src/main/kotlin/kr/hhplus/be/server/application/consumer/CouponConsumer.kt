package kr.hhplus.be.server.application.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import kr.hhplus.be.server.application.CouponService
import kr.hhplus.be.server.application.event.RestoreCouponIssueEvent
import kr.hhplus.be.server.application.event.UserCouponCreateEvent
import kr.hhplus.be.server.application.producer.CouponMessageProducer
import kr.hhplus.be.server.global.kafka.KafkaGroup
import kr.hhplus.be.server.global.kafka.KafkaTopic
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class CouponConsumer(
    final val objectMapper: ObjectMapper,
    final val couponService: CouponService,
    final val couponMessageProducer: CouponMessageProducer
) {

    private val logger = KotlinLogging.logger {}

    @KafkaListener(topics = [KafkaTopic.COUPON_ISSUE_REQUEST], groupId = KafkaGroup.COUPON_ISSUE)
    fun publishCoupon(message: String) {
        val event = objectMapper.readValue(message, UserCouponCreateEvent::class.java)

        try {
            couponService.createUserCoupon(event.userId, event.couponId)
        } catch (e: Exception) {
            logger.error(e) { "Failed to create user coupon: ${e.message}" }
            couponMessageProducer.sendRestoreCouponPublish(RestoreCouponIssueEvent(event.userId, event.couponId))
        }
    }

    @KafkaListener(topics = [KafkaTopic.RESTORE_COUPON_ISSUE], groupId = KafkaGroup.RESTORE_COUPON_ISSUE)
    fun restoreCouponIssue(message: String) {
        val event = objectMapper.readValue(message, UserCouponCreateEvent::class.java)

        try {
            couponService.restoreCouponIssue(event.userId, event.couponId)
        } catch (e: Exception) {
            logger.error(e) { "Failed to restore coupon issue: ${e.message}" }
        }
    }

}