package kr.hhplus.be.server.application.event.listener

import io.github.oshai.kotlinlogging.KotlinLogging
import kr.hhplus.be.server.application.event.OrderedEvent
import kr.hhplus.be.server.application.producer.OrderMessageProducer
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class OrderedEventListener(
    final val orderRecordProducer: OrderMessageProducer
) {

    private val logger = KotlinLogging.logger {}

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleEvent(event: OrderedEvent) {
        try {
            orderRecordProducer.sendOrderRecord(event)
        } catch (e: Exception) {
            logger.error(e) { "OrderedEvent 처리 중 오류 발생" }
        }

    }

}