package kr.hhplus.be.server.application

import kr.hhplus.be.server.domain.UserPointHistory
import kr.hhplus.be.server.infrastructure.UserPointHistoryRepository
import kr.hhplus.be.server.infrastructure.UserRepository
import kr.hhplus.be.server.infrastructure.findByIdOrThrow
import kr.hhplus.be.server.presentation.response.PointChargeResponse
import kr.hhplus.be.server.presentation.response.PointResponse
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userPointHistoryRepository: UserPointHistoryRepository
) {

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Retryable(
        value = [ObjectOptimisticLockingFailureException::class],
        maxAttempts = 5
    )
    fun chargePoint(userId: Long, amount: Long): PointChargeResponse {
        val user = userRepository.findByIdOrThrow(userId)

        user.chargePoint(amount)
        userRepository.save(user)

        val pointHistory = UserPointHistory.createChargeHistory(userId, amount)
        userPointHistoryRepository.save(pointHistory)

        return PointChargeResponse(userId, amount, user.point)
    }

    fun getPointOfUser(userId: Long): PointResponse {
        val user = userRepository.findByIdOrThrow(userId)

        return PointResponse(
            userId = user.id,
            totalPoint = user.point
        )
    }
}
