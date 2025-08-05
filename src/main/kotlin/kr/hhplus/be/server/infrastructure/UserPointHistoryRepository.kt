package kr.hhplus.be.server.infrastructure

import kr.hhplus.be.server.domain.UserPointHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserPointHistoryRepository : JpaRepository<UserPointHistory, Long> {
    fun findByUserId(userId: Long): List<UserPointHistory>
}