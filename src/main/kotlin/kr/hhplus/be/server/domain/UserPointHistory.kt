package kr.hhplus.be.server.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class UserPointHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: PointHistoryType,

    @Column(nullable = false)
    val amount: Long,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {

    companion object {
        fun createChargeHistory(userId: Long, amount: Long): UserPointHistory {
            return UserPointHistory(
                userId = userId,
                type = PointHistoryType.CHARGE,
                amount = amount
            )
        }
    }
}

enum class PointHistoryType {
    CHARGE, USE
}