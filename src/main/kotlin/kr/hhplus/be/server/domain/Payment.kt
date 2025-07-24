package kr.hhplus.be.server.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Payment(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    var order: Order? = null,

    var discountAmount: Long = 0L,

    var paidAmount: Long = 0L,

    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)