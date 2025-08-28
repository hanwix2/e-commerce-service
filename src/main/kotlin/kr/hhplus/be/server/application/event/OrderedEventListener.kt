package kr.hhplus.be.server.application.event

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class OrderedEventListener {

    private val logger = KotlinLogging.logger {}

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleEvent(event: OrderedEvent) {
        logger.info {
            """[데이터 플랫폼 주문 정보 전송]
            | - userId: ${event.userId}
            | - 총 금액: ${event.totalPrice}
            | - 할인 금액: ${event.discountAmount}
            | - 결제 금액: ${event.paidAmount}
            | - 상품:
            |${
                event.items.joinToString("\n") { item ->
                    """    - 상품명: ${item.productName}, 수량: ${item.quantity}, 가격: ${item.price}"""
                }
            }
        """.trimMargin()
        }

    }

}