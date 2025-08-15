package kr.hhplus.be.server.application

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kr.hhplus.be.server.domain.Coupon
import kr.hhplus.be.server.domain.DiscountType
import kr.hhplus.be.server.domain.User
import kr.hhplus.be.server.global.exception.BusinessException
import kr.hhplus.be.server.global.exception.ResponseStatus
import kr.hhplus.be.server.infrastructure.CouponRepository
import kr.hhplus.be.server.infrastructure.UserCouponRepository
import kr.hhplus.be.server.infrastructure.UserRepository
import kr.hhplus.be.server.presentation.request.CouponIssueRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.*
import java.util.concurrent.CountDownLatch

@SpringBootTest
@ActiveProfiles("test")
class CouponWithLockServiceIntegrationTest @Autowired constructor(
    private val couponWithLockService: CouponWithLockService,
    private val couponRepository: CouponRepository,
    private val userCouponRepository: UserCouponRepository,
    private val userRepository: UserRepository
) {

    @BeforeEach
    fun setUp() {
        userCouponRepository.deleteAll()
        couponRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `issueCoupon - (동시성 테스트) 여러 사용자가 동시에 쿠폰을 발급받을 때 발급 가능 수량 만큼만 발급되어야 한다`() {
        // Given
        val users = listOf(
            userRepository.save(User(name = "User1", point = 1000L)),
            userRepository.save(User(name = "User2", point = 1000L)),
            userRepository.save(User(name = "User3", point = 1000L)),
            userRepository.save(User(name = "User4", point = 1000L))
        )

        val couponIssueLimit = 2L

        val coupon = Coupon(
            name = "Test Coupon",
            discountAmount = 100L,
            discountType = DiscountType.PRICE,
            issueLimit = couponIssueLimit,
            issuedRemain = couponIssueLimit,
            issuable = true
        )
        val savedCoupon = couponRepository.save(coupon)
        val latch = CountDownLatch(users.size)

        // When
        val exceptions = Collections.synchronizedCollection(mutableListOf<Exception>())

        runBlocking {
            users.forEach { user ->
                launch(Dispatchers.IO) {
                    try {
                        couponWithLockService.issueCoupon(CouponIssueRequest(userId = user.id, couponId = savedCoupon.id))
                    } catch (e: BusinessException) {
                        if (e.status == ResponseStatus.COUPON_OUT_OF_STOCK) {
                            exceptions.add(e)
                        }
                    } finally {
                        latch.countDown()
                    }
                }
            }
        }
        latch.await()

        // Then
        val userCoupons = users.flatMap { user ->
            userCouponRepository.findByUserId(user.id)
        }
        assertThat(userCoupons).hasSize(couponIssueLimit.toInt())

        assertThat(exceptions.size).isEqualTo(users.size - couponIssueLimit.toInt())

        couponRepository.findById(savedCoupon.id).ifPresent {
            assertThat(it.issuedRemain).isEqualTo(0L)
        }
    }
}