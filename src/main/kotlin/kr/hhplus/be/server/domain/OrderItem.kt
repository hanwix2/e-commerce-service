package kr.hhplus.be.server.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class OrderItem(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    var order: Order? = null,

    @Column(nullable = false)
    var productId: Long = 0L,

    @Column(nullable = false)
    var productName: String = "",

    var price: Long = 0L,

    var quantity: Int = 0,

    @Enumerated(EnumType.STRING)
    var status: OrderItemStatus = OrderItemStatus.PURCHASE,

    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {

    companion object {
        fun of(product: OrderProduct, order: Order): OrderItem {
            return OrderItem(
                productId = product.product.id,
                productName = product.product.name,
                price = product.product.price,
                quantity = product.quantity,
                order = order
            )
        }
    }

}

enum class OrderItemStatus {
    PURCHASE,
    REFUND
}
