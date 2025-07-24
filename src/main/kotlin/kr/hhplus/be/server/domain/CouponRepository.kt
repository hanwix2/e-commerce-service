package kr.hhplus.be.server.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CouponRepository : JpaRepository<Coupon, Long>