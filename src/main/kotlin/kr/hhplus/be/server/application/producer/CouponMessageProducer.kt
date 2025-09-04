package kr.hhplus.be.server.application.producer

import com.fasterxml.jackson.databind.ObjectMapper
import kr.hhplus.be.server.application.event.RestoreCouponIssueEvent
import kr.hhplus.be.server.application.event.UserCouponCreateEvent
import kr.hhplus.be.server.global.kafka.KafkaTopic
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class CouponMessageProducer(
    final val kafkaTemplate: KafkaTemplate<String, String>,
    final val objectMapper: ObjectMapper
) {

    fun sendUserCouponIssueRequest(event: UserCouponCreateEvent) {
        objectMapper.writeValueAsString(event).also {
            kafkaTemplate.send(KafkaTopic.COUPON_ISSUE_REQUEST, it)
        }
    }

    fun sendRestoreCouponPublish(event: RestoreCouponIssueEvent) {
        objectMapper.writeValueAsString(event).also {
            kafkaTemplate.send(KafkaTopic.RESTORE_COUPON_ISSUE, it)
        }
    }

}