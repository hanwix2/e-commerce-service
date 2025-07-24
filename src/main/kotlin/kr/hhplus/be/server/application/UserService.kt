package kr.hhplus.be.server.application

import kr.hhplus.be.server.domain.UserPointHistory
import kr.hhplus.be.server.domain.UserPointHistoryRepository
import kr.hhplus.be.server.domain.UserRepository
import kr.hhplus.be.server.global.exception.BusinessException
import kr.hhplus.be.server.global.exception.ResponseStatus
import kr.hhplus.be.server.presentation.response.PointChargeResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userPointHistoryRepository: UserPointHistoryRepository
) {

    @Transactional
    fun chargePoint(userId: Long, amount: Long): PointChargeResponse {
        val user = userRepository.findById(userId).orElseThrow { BusinessException(ResponseStatus.USER_NOT_FOUND) }

        user.chargePoint(amount)
        userRepository.save(user)

        val pointHistory = UserPointHistory.createChargeHistory(userId, amount)
        userPointHistoryRepository.save(pointHistory)

        return PointChargeResponse(userId, amount, user.point)
    }
}
