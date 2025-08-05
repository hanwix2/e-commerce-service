package kr.hhplus.be.server.application

import kr.hhplus.be.server.domain.*
import kr.hhplus.be.server.global.exception.BusinessException
import kr.hhplus.be.server.global.exception.ResponseStatus
import kr.hhplus.be.server.infrastructure.CouponRepository
import kr.hhplus.be.server.infrastructure.UserCouponRepository
import kr.hhplus.be.server.infrastructure.UserRepository
import kr.hhplus.be.server.presentation.request.CouponIssueRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
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
class CouponServiceIntegrationTest @Autowired constructor(
    private val couponService: CouponService,
    private val couponRepository: CouponRepository,
    private val userCouponRepository: UserCouponRepository,
    private val userRepository: UserRepository
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
            issuedRemain = 10L,
            issuable = true
        )
        val savedCoupon = couponRepository.save(coupon)
        val request = CouponIssueRequest(userId = user.id, couponId = savedCoupon.id)

        // When
        val response = couponService.issueCoupon(request)

        // Then
        assertThat(response.userId).isEqualTo(user.id)
        assertThat(response.userCouponId).isNotNull
        assertThat(response.discountType).isEqualTo(savedCoupon.discountType.name)
        assertThat(response.discountAmount).isEqualTo(savedCoupon.discountAmount)

        userCouponRepository.findByUserId(user.id).also { userCoupons ->
            assertThat(userCoupons).hasSize(1)
            assertThat(userCoupons[0].userId).isEqualTo(user.id)
            assertThat(userCoupons[0].coupon).isEqualTo(savedCoupon)
        }
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
            issuedRemain = 10L,
            issuable = true
        )
        couponRepository.save(coupon)

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
            issuedRemain = 10L,
            issuable = false
        )
        couponRepository.save(coupon)

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
            issuedRemain = 0L,
            issuable = true
        )
        couponRepository.save(coupon)
        val request = CouponIssueRequest(userId = user.id, couponId = coupon.id)

        // When & Then
        val exception = assertThrows<BusinessException> {
            couponService.issueCoupon(request)
        }.also { exception ->
            assertEquals(ResponseStatus.COUPON_OUT_OF_STOCK, exception.status)
        }
    }
}