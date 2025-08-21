package kr.hhplus.be.server.infrastructure

interface CouponIssueUserRepository {

    fun add(couponId: Long, userId: Long): Long

    fun delete(couponId: Long, userId: Long): Long

}