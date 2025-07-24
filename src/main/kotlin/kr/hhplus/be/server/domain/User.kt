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
        if (amount <= 0) {
            throw IllegalArgumentException("Charge amount must be positive")
        }
        this.point += amount
    }
}