package kr.hhplus.be.server.application

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.*
import kr.hhplus.be.server.global.exception.BusinessException
import kr.hhplus.be.server.global.exception.ResponseStatus
import kr.hhplus.be.server.presentation.request.CouponIssueRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.util.*

class CouponServiceTest {

    private val couponRepository = mockk<CouponRepository>()
    private val userCouponRepository = mockk<UserCouponRepository>()
    private val userRepository = mockk<UserRepository>()
    private val couponService = CouponService(couponRepository, userCouponRepository, userRepository)

    @Test
    fun issueCoupon() {
        val userId = 1L
        val couponId = 101L
        val discountAmount = 5000L
        val discountType = DiscountType.PRICE
        val issuedRemain = 10L

        val request = CouponIssueRequest(userId = userId, couponId = couponId)

        val user = User(id = userId, name = "Test User")
        val coupon = Coupon(
            id = couponId,
            name = "Test Coupon",
            discountAmount = discountAmount,
            discountType = discountType,
            issueLimit = 100L,
            issuedRemain = issuedRemain,
            issuable = true
        )

        val userCoupon = UserCoupon.create(coupon, userId)
        userCoupon.id = 201L

        every { userRepository.findByIdOrNull(userId) } returns user
        every { couponRepository.findByIdOrNull(couponId) } returns coupon
        every { userCouponRepository.save(any()) } returns userCoupon
        every { couponRepository.save(any()) } returns coupon

        val result = couponService.issueCoupon(request)

        verify(exactly = 1) { userRepository.findByIdOrNull(userId) }
        verify(exactly = 1) { couponRepository.findByIdOrNull(couponId) }
        verify(exactly = 1) { userCouponRepository.save(any()) }
        verify(exactly = 1) { couponRepository.save(any()) }

        assertEquals(userId, result.userId)
        assertEquals(userCoupon.id, result.userCouponId)
        assertEquals(discountType.name, result.discountType)
        assertEquals(discountAmount, result.discountAmount)
        assertEquals(userCoupon.createdAt, result.issuedAt)

        assertEquals(issuedRemain - 1, coupon.issuedRemain)
    }

    @Test
    fun `issueCoupon 은 유저가 존재하지 않을 때 쿠폰 발급에 실패한다`() {
        val userId = 2L
        val couponId = 11L

        val request = CouponIssueRequest(userId = userId, couponId = couponId)

        every { userRepository.findByIdOrNull(userId) } returns  null

        val exception = assertThrows(BusinessException::class.java) {
            couponService.issueCoupon(request)
        }

        assertEquals(ResponseStatus.USER_NOT_FOUND, exception.status)
        verify(exactly = 1) { userRepository.findByIdOrNull(userId) }
        verify(exactly = 0) { couponRepository.findByIdOrNull(any()) }
        verify(exactly = 0) { userCouponRepository.save(any()) }
        verify(exactly = 0) { couponRepository.save(any()) }
    }

    @Test
    fun `issueCoupon 는 쿠폰이 존재하지 않을 때 실패한다`() {
        val userId = 3L
        val couponId = 9L

        val request = CouponIssueRequest(userId = userId, couponId = couponId)

        val user = User(id = userId, name = "Test User")

        every { userRepository.findByIdOrNull(userId) } returns user
        every { couponRepository.findByIdOrNull(couponId) } returns null

        val exception = assertThrows(BusinessException::class.java) {
            couponService.issueCoupon(request)
        }

        assertEquals(ResponseStatus.COUPON_NOT_FOUND, exception.status)
        verify(exactly = 1) { userRepository.findByIdOrNull(userId) }
        verify(exactly = 1) { couponRepository.findByIdOrNull(couponId) }
        verify(exactly = 0) { userCouponRepository.save(any()) }
        verify(exactly = 0) { couponRepository.save(any()) }
    }

    @Test
    fun `issueCoupon 는 쿠폰이 발급 가능 상태가 아닐 때 실패한다`() {
        // Given
        val userId = 1L
        val couponId = 101L

        val request = CouponIssueRequest(userId = userId, couponId = couponId)

        val user = User(id = userId, name = "Test User")
        val coupon = Coupon(
            id = couponId,
            name = "Test Coupon",
            discountAmount = 5000L,
            discountType = DiscountType.PRICE,
            issueLimit = 100L,
            issuedRemain = 10L,
            issuable = false
        )

        every { userRepository.findByIdOrNull(userId) } returns user
        every { couponRepository.findByIdOrNull(couponId) } returns coupon

        val exception = assertThrows(BusinessException::class.java) {
            couponService.issueCoupon(request)
        }

        assertEquals(ResponseStatus.INVALID_COUPON, exception.status)
        verify(exactly = 1) { userRepository.findByIdOrNull(userId) }
        verify(exactly = 1) { couponRepository.findByIdOrNull(couponId) }
        verify(exactly = 0) { userCouponRepository.save(any()) }
        verify(exactly = 0) { couponRepository.save(any()) }
    }

    @Test
    fun `issueCoupon 는 잔여 쿠폰이 없을 때 실패한다`() {
        // Given
        val userId = 1L
        val couponId = 101L

        val request = CouponIssueRequest(userId = userId, couponId = couponId)

        val user = User(id = userId, name = "Test User")
        val coupon = Coupon(
            id = couponId,
            name = "Test Coupon",
            discountAmount = 5000L,
            discountType = DiscountType.PRICE,
            issueLimit = 100L,
            issuedRemain = 0L,
            issuable = true
        )

        every { userRepository.findByIdOrNull(userId) } returns user
        every { couponRepository.findByIdOrNull(couponId) } returns coupon

        val exception = assertThrows(BusinessException::class.java) {
            couponService.issueCoupon(request)
        }

        assertEquals(ResponseStatus.COUPON_OUT_OF_STOCK, exception.status)
        verify(exactly = 1) { userRepository.findByIdOrNull(userId) }
        verify(exactly = 1) { couponRepository.findByIdOrNull(couponId) }
        verify(exactly = 0) { userCouponRepository.save(any()) }
        verify(exactly = 0) { couponRepository.save(any()) }
    }
}
