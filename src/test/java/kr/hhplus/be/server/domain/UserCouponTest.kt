package kr.hhplus.be.server.domain

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

class UserCouponTest {

    @Test
    fun use() {
        val userCoupon = UserCoupon(
            id = 1L,
            userId = 1L,
            status = UserCouponStatus.ACTIVE
        )
        val paymentId = 1L
        val usedAt = LocalDateTime.now()

        userCoupon.use(paymentId, usedAt)

        assertTrue(userCoupon.status == UserCouponStatus.USED)
        assertEquals(paymentId, userCoupon.paymentId)
        assertEquals(usedAt, userCoupon.usedAt)
    }

    @Test
    fun isAvailable() {
        val activeCoupon = UserCoupon(status = UserCouponStatus.ACTIVE)
        assertTrue(activeCoupon.isAvailable())

        val usedCoupon = UserCoupon(status = UserCouponStatus.USED)
        assertFalse(usedCoupon.isAvailable())

        val expiredCoupon = UserCoupon(status = UserCouponStatus.EXPIRED)
        assertFalse(expiredCoupon.isAvailable())
    }

    @Test
    fun getDiscountPriceAmount() {
        val originPrice = 5000L

        val userCouponPrice = UserCoupon(
            discountType = DiscountType.PRICE,
            discountAmount = 1000L
        )
        assertEquals(1000L, userCouponPrice.getDiscountPriceAmount(originPrice))


        val userCouponRate = UserCoupon(
            discountType = DiscountType.RATE,
            discountAmount = 10L
        )
        assertEquals(500L, userCouponRate.getDiscountPriceAmount(originPrice))
    }
}