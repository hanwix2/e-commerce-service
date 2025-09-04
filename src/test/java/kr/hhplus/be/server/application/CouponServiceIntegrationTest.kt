package kr.hhplus.be.server.application

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kr.hhplus.be.server.domain.Coupon
import kr.hhplus.be.server.domain.DiscountType
import kr.hhplus.be.server.domain.User
import kr.hhplus.be.server.global.cache.KeyName
import kr.hhplus.be.server.global.exception.BusinessException
import kr.hhplus.be.server.global.exception.ResponseStatus
import kr.hhplus.be.server.infrastructure.CouponRepository
import kr.hhplus.be.server.infrastructure.UserCouponRepository
import kr.hhplus.be.server.infrastructure.UserRepository
import kr.hhplus.be.server.presentation.request.CouponIssueRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.test.context.ActiveProfiles
import java.util.*
import java.util.concurrent.CountDownLatch

@SpringBootTest
@ActiveProfiles("test")
class CouponServiceIntegrationTest @Autowired constructor(
    private val couponService: CouponService,
    private val couponRepository: CouponRepository,
    private val userRepository: UserRepository,
    private val redisTemplate: StringRedisTemplate
) {

    lateinit var user: User

    @BeforeEach
    fun setUp() {
        user = userRepository.save(User(name = "Test User", point = 1000L))
    }

    @Test
    fun `issueCoupon - 사용자에게 쿠폰을 발급`() {
        // Given
        val coupon = Coupon(
            name = "Test Coupon",
            discountAmount = 100L,
            discountType = DiscountType.PRICE,
            issueLimit = 100L,
            issuable = true
        )
        val savedCoupon = couponRepository.save(coupon)

        redisTemplate.opsForValue().set(KeyName.COUPON_LEFT + savedCoupon.id, "100")

        val request = CouponIssueRequest(userId = user.id, couponId = savedCoupon.id)

        // When
        val response = couponService.issueCoupon(request)

        // Then
        assertThat(response.userId).isEqualTo(user.id)
        assertThat(response.couponId).isEqualTo(savedCoupon.id)
    }

    @Test
    fun `issueCoupon - 유저가 존재하지 않을 때 쿠폰 발급에 실패한다`() {
        // Given
        val userId = 0L
        val coupon = Coupon(
            name = "Test Coupon",
            discountAmount = 100L,
            discountType = DiscountType.PRICE,
            issueLimit = 100L,
            issuable = true
        )
        val savedCoupon = couponRepository.save(coupon)

        redisTemplate.opsForValue().set(KeyName.COUPON_LEFT + savedCoupon.id, "100")

        val request = CouponIssueRequest(userId = userId, couponId = coupon.id) // 존재하지 않는 유저 ID


        // When & Then
        assertThrows<BusinessException> {
            couponService.issueCoupon(request)
        }.also { exception ->
            assertEquals(ResponseStatus.USER_NOT_FOUND, exception.status)
        }
    }

    @Test
    fun `issueCoupon - 쿠폰이 존재하지 않을 때 쿠폰 발급에 실패한다`() {
        // Given
        val couponId = 0L
        redisTemplate.opsForValue().set(KeyName.COUPON_LEFT + couponId, "100")

        val request = CouponIssueRequest(userId = user.id, couponId = couponId)

        // When & Then
        val exception = assertThrows<BusinessException> {
            couponService.issueCoupon(request)
        }.also { exception ->
            assertEquals(ResponseStatus.COUPON_NOT_FOUND, exception.status)
        }
    }

    @Test
    fun `issueCoupon - 쿠폰이 발급 불가능 상태일 때 쿠폰 발급에 실패한다`() {
        // Given
        val coupon = Coupon(
            name = "Test Coupon",
            discountAmount = 100L,
            discountType = DiscountType.PRICE,
            issueLimit = 100L,
            issuable = false
        )
        val savedCoupon = couponRepository.save(coupon)

        redisTemplate.opsForValue().set(KeyName.COUPON_LEFT + savedCoupon.id, "100")

        val request = CouponIssueRequest(userId = user.id, couponId = coupon.id)

        // When & Then
        val exception = assertThrows<BusinessException> {
            couponService.issueCoupon(request)
        }.also { exception ->
            assertEquals(ResponseStatus.INVALID_COUPON, exception.status)
        }
    }

    @Test
    fun `issueCoupon - 쿠폰이 발급 가능 수량을 초과했을 때 쿠폰 발급에 실패한다`() {
        // Given
        val coupon = Coupon(
            name = "Test Coupon",
            discountAmount = 100L,
            discountType = DiscountType.PRICE,
            issueLimit = 100L,
            issuable = true
        )
        val savedCoupon = couponRepository.save(coupon)

        redisTemplate.opsForValue().set(KeyName.COUPON_LEFT + savedCoupon.id, "0")

        val request = CouponIssueRequest(userId = user.id, couponId = coupon.id)

        // When & Then
        val exception = assertThrows<BusinessException> {
            couponService.issueCoupon(request)
        }.also { exception ->
            assertEquals(ResponseStatus.COUPON_OUT_OF_STOCK, exception.status)
        }
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
            issuable = true
        )
        val savedCoupon = couponRepository.save(coupon)
        redisTemplate.opsForValue().set(KeyName.COUPON_LEFT + savedCoupon.id, couponIssueLimit.toString())

        val latch = CountDownLatch(users.size)

        // When
        val exceptions = Collections.synchronizedCollection(mutableListOf<Exception>())

        runBlocking {
            users.forEach { user ->
                launch(Dispatchers.IO) {
                    try {
                        couponService.issueCoupon(CouponIssueRequest(userId = user.id, couponId = savedCoupon.id))
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
        assertThat(exceptions.size).isEqualTo(users.size - couponIssueLimit.toInt())
    }

}