package kr.hhplus.be.server.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class UserCoupon(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    var coupon: Coupon? = null,

    var userId: Long = 0L,

    @Enumerated(EnumType.STRING)
    var discountType: DiscountType = DiscountType.PRICE,

    var discountAmount: Long = 0L,

    @Enumerated(EnumType.STRING)
    var status: UserCouponStatus = UserCouponStatus.ACTIVE,

    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(length = 36, nullable = true)
    var paymentId: Long? = null,

    var usedAt: LocalDateTime? = null
) {
    companion object {
        fun create(
            coupon: Coupon,
            userId: Long,
        ): UserCoupon {
            return UserCoupon(
                coupon = coupon,
                userId = userId,
                discountType = coupon.discountType,
                discountAmount = coupon.discountAmount,
                status = UserCouponStatus.ACTIVE
            )
        }
    }

    fun use(paymentId: Long, usedAt: LocalDateTime) {
        this.paymentId = paymentId
        this.usedAt = usedAt
        this.status = UserCouponStatus.USED
    }

    fun isAvailable(): Boolean {
        return this.status == UserCouponStatus.ACTIVE
    }

    fun getDiscountPriceAmount(originPrice: Long): Long {
        return if (this.discountType == DiscountType.RATE) {
            originPrice * this.discountAmount / 100
        } else {
            this.discountAmount
        }
    }

}

enum class UserCouponStatus {
    ACTIVE,
    USED,
    EXPIRED
}