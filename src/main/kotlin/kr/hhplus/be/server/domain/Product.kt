package kr.hhplus.be.server.domain

import jakarta.persistence.*

@Entity
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false)
    var stock: Int = 0,

    @Column(nullable = false)
    var price: Long = 0L,

    var deleted: Boolean = false,

    @Version
    @Column(nullable = false)
    var version: Long = 1L
) {
    fun isStockSufficient(quantity: Int): Boolean {
        if (quantity <= 0) {
            throw IllegalArgumentException("Quantity must be positive")
        }
        return stock >= quantity
    }

    fun isStockInsufficient(quantity: Int): Boolean {
        return !isStockSufficient(quantity)
    }

    fun reduceStock(quantity: Int) {
        if (quantity <= 0) {
            throw IllegalArgumentException("Quantity must be positive")
        }
        if (stock < quantity) {
            throw IllegalStateException("Insufficient stock")
        }
        stock -= quantity
    }
}