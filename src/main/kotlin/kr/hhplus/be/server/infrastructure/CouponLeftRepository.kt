package kr.hhplus.be.server.infrastructure

interface CouponLeftRepository {

    fun increment(couponId: Long): Long

    fun decrement(couponId: Long): Long

}