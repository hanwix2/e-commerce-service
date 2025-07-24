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
)