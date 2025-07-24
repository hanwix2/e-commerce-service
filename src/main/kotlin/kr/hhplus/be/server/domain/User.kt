package kr.hhplus.be.server.domain

import jakarta.persistence.*

@Entity
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false)
    var point: Long = 0,

    @Version
    @Column(nullable = false)
    var version: Long = 1
) {
    fun chargePoint(amount: Long) {
        if (amount <= 0)
            throw IllegalArgumentException("Charge amount must be positive")

        this.point += amount
    }

    fun reducePoint(point: Long) {
        if (point <= 0)
            throw IllegalArgumentException("Reduction amount must be positive")

        if (this.point < point)
            throw IllegalStateException("Insufficient points")

        this.point -= point
    }

    fun isPointInsufficient(paidAmount: Long): Boolean {
        if (paidAmount <= 0)
            throw IllegalArgumentException("Paid amount must be positive")

        return this.point < paidAmount
    }
}