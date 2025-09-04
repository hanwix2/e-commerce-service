package kr.hhplus.be.server.application.event

data class UserCouponCreateEvent(
    val userId: Long,
    val couponId: Long
)
