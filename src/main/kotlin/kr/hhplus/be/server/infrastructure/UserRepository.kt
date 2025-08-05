package kr.hhplus.be.server.infrastructure

import kr.hhplus.be.server.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long>