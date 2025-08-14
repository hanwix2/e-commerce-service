package kr.hhplus.be.server.infrastructure

import kr.hhplus.be.server.domain.Order
import kr.hhplus.be.server.domain.OrderItem
import kr.hhplus.be.server.domain.OrderItemStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface OrderItemRepository : JpaRepository<OrderItem, Long> {

    @Query(
        """
        SELECT oi.productId
        FROM OrderItem oi 
        WHERE oi.createdAt >= :startAt
        AND oi.createdAt < :endAt
        AND oi.status = :status 
        GROUP BY oi.productId
        ORDER BY SUM(oi.quantity) DESC
        LIMIT :limitNumber
    """
    )
    fun findTopDistinctProductIdsByStatusAndCreatedAtRange(
        limitNumber: Int,
        status: OrderItemStatus = OrderItemStatus.PURCHASE,
        startAt: LocalDateTime,
        endAt: LocalDateTime
    ): List<Long>

    fun findByOrder(order: Order): List<OrderItem>

}
