package kr.hhplus.be.server.infrastructure

import kr.hhplus.be.server.domain.UserCoupon
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserCouponRepository : JpaRepository<UserCoupon, Long> {

    fun findByIdAndUserId(id: Long, userId: Long): UserCoupon?

    fun findByUserId(userId: Long): List<UserCoupon>

}