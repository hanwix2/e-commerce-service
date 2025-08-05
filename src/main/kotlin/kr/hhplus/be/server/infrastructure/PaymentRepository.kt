package kr.hhplus.be.server.infrastructure

import kr.hhplus.be.server.domain.Payment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentRepository : JpaRepository<Payment, Long>