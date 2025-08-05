package kr.hhplus.be.server.infrastructure

import kr.hhplus.be.server.domain.Coupon
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CouponRepository : JpaRepository<Coupon, Long>