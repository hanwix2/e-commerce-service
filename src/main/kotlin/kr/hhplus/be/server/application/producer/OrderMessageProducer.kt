package kr.hhplus.be.server.application.producer

import com.fasterxml.jackson.databind.ObjectMapper
import kr.hhplus.be.server.application.event.OrderedEvent
import kr.hhplus.be.server.global.kafka.KafkaTopic
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class OrderMessageProducer(
    final val kafkaTemplate: KafkaTemplate<String, String>,
    final val objectMapper: ObjectMapper
) {

    fun sendOrderRecord(orderedEvent: OrderedEvent) {
        objectMapper.writeValueAsString(orderedEvent).also {
            kafkaTemplate.send(KafkaTopic.ORDER_RECORD, it)
        }
    }

}