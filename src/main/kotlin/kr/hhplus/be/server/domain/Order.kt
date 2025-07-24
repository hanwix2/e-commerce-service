package kr.hhplus.be.server.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class Order (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    var user: User,

    var totalPrice: Long = 0L,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.PURCHASE,

    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun createPurchase(user: User, totalPrice: Long): Order {
            return Order(
                user = user,
                totalPrice = totalPrice
            )
        }
    }
}

enum class OrderStatus {
    PURCHASE,
    REFUND,
    PARTIAL_REFUND,
}