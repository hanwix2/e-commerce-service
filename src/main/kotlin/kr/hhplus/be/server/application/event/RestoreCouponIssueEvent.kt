package kr.hhplus.be.server.application.event

data class RestoreCouponIssueEvent(
    val userId: Long,
    val couponId: Long
)