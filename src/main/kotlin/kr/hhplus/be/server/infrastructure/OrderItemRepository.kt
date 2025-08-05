package kr.hhplus.be.server.infrastructure

import kr.hhplus.be.server.domain.Order
import kr.hhplus.be.server.domain.OrderItem
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
        AND oi.status = kr.hhplus.be.server.domain.OrderItemStatus.PURCHASE 
        GROUP BY oi.productId
        ORDER BY SUM(oi.quantity) DESC
        LIMIT :limitNumber
    """
    )
    fun findTopDistinctPurchasedProductIdsByCreatedAtAfter(limitNumber: Int, startAt: LocalDateTime): List<Long>

    fun findByOrder(order: Order): List<OrderItem>

}
