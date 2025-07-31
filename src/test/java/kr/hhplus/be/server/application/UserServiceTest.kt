package kr.hhplus.be.server.application

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.User
import kr.hhplus.be.server.domain.UserPointHistoryRepository
import kr.hhplus.be.server.domain.UserRepository
import kr.hhplus.be.server.global.exception.BusinessException
import kr.hhplus.be.server.global.exception.ResponseStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.util.*

class UserServiceTest {

    private val userRepository = mockk<UserRepository>()
    private val userPointHistoryRepository = mockk<UserPointHistoryRepository>()
    private val userService: UserService = UserService(userRepository, userPointHistoryRepository)

    @Test
    fun chargePoint_success() {
        val userId = 1L
        val initialPoint = 1000L
        val chargeAmount = 500L
        val expectedTotalPoint = initialPoint + chargeAmount
        val user = User(id = userId, name = "Test User", point = initialPoint)

        every { userRepository.findByIdOrNull(userId) } returns user
        every { userRepository.save(any()) } returns user
        every { userPointHistoryRepository.save(any()) } returns mockk()

        val result = userService.chargePoint(userId, chargeAmount)

        verify(exactly = 1) { userRepository.findByIdOrNull(userId) }
        verify(exactly = 1) { userRepository.save(user) }

        assertEquals(userId, result.userId)
        assertEquals(chargeAmount, result.chargedAmount)
        assertEquals(expectedTotalPoint, result.totalPoint)
        assertEquals(expectedTotalPoint, user.point)
    }

    @Test
    fun chargePoint_failed_userNotFound() {
        val userId = 2L
        val chargeAmount = 500L

        every { userRepository.findByIdOrNull(userId) } returns null

        val exception = assertThrows(BusinessException::class.java) {
            userService.chargePoint(userId, chargeAmount)
        }

        assertEquals(ResponseStatus.USER_NOT_FOUND, exception.status)
        verify(exactly = 1) { userRepository.findByIdOrNull(userId) }
        verify(exactly = 0) { userRepository.save(any()) }
        verify(exactly = 0) { userPointHistoryRepository.save(any()) }
    }

}
