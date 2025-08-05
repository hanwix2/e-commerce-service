package kr.hhplus.be.server.application

import kr.hhplus.be.server.domain.PointHistoryType
import kr.hhplus.be.server.domain.User
import kr.hhplus.be.server.infrastructure.UserPointHistoryRepository
import kr.hhplus.be.server.infrastructure.UserRepository
import kr.hhplus.be.server.global.exception.BusinessException
import kr.hhplus.be.server.global.exception.ResponseStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest @Autowired constructor(
    private val userService: UserService,
    private val userRepository: UserRepository,
    private var userPointHistoryRepository: UserPointHistoryRepository
) {

    lateinit var user: User

    @BeforeEach
    fun setUp() {
        user = userRepository.save(User(name = "Test User", point = 1000L))
    }

    @Test
    fun `getPointOfUser - 사용자의 보유 포인트 조회`() {
        val pointOfUser = userService.getPointOfUser(user.id)

        assertThat(pointOfUser.userId).isEqualTo(user.id)
        assertThat(pointOfUser.totalPoint).isEqualTo(user.point)
    }

    @Test
    fun `chargePoint - 사용자의 포인트를 충전하면 보유 포인트가 증가하고 충전 이력이 저장된다`() {
        val previousPoint = user.point
        val chargeAmount = 500L

        val response = userService.chargePoint(user.id, chargeAmount)

        assertThat(response.userId).isEqualTo(user.id)
        assertThat(response.chargedAmount).isEqualTo(chargeAmount)
        assertThat(response.totalPoint).isEqualTo(previousPoint + chargeAmount)

        userPointHistoryRepository.findByUserId(user.id).also { histories ->
            assertThat(histories).hasSize(1)
            assertThat(histories[0].userId).isEqualTo(user.id)
            assertThat(histories[0].type).isEqualTo(PointHistoryType.CHARGE)
            assertThat(histories[0].amount).isEqualTo(chargeAmount)
        }
    }

    @Test
    fun `chargePoint - 충전 포인트가 음수일 경우 포인트 충전은 실패한다`() {
        val chargeAmount = -100L

        assertThrows<IllegalArgumentException> {
            userService.chargePoint(user.id, chargeAmount)
        }

        userRepository.findById(user.id).ifPresent {
            assertThat(it.point).isEqualTo(user.point)
        }
    }

    @Test
    fun `chargePoint - 존재하지 않는 사용자에게 포인트를 충전하려고 하면 실패한다`() {
        val nonExistentUserId = 0L
        val chargeAmount = 100L

        assertThrows<BusinessException> {
            userService.chargePoint(nonExistentUserId, chargeAmount)
        }.also { exception ->
            assertThat(exception.status).isEqualTo(ResponseStatus.USER_NOT_FOUND)
        }
    }
}