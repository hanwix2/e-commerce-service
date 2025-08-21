package kr.hhplus.be.server.application

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.*
import kr.hhplus.be.server.global.exception.BusinessException
import kr.hhplus.be.server.global.exception.ResponseStatus
import kr.hhplus.be.server.infrastructure.CouponLeftRepository
import kr.hhplus.be.server.infrastructure.CouponRepository
import kr.hhplus.be.server.infrastructure.RedisCouponIssueUserRepository
import kr.hhplus.be.server.infrastructure.UserCouponRepository
import kr.hhplus.be.server.infrastructure.UserRepository
import kr.hhplus.be.server.presentation.request.CouponIssueRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull

class CouponServiceTest {

    private val couponRepository = mockk<CouponRepository>()
    private val userCouponRepository = mockk<UserCouponRepository>()
    private val userRepository = mockk<UserRepository>()
    private val couponIssueCountRepository = mockk<CouponLeftRepository>()
    private val couponIssueUserRepository = mockk<RedisCouponIssueUserRepository>()
    private val couponService = CouponService(
        couponRepository, userCouponRepository, userRepository,
        couponIssueCountRepository, couponIssueUserRepository
    )

    @Test
    fun issueCoupon() {
        val userId = 1L
        val couponId = 101L
        val discountAmount = 5000L
        val discountType = DiscountType.PRICE

        val request = CouponIssueRequest(userId = userId, couponId = couponId)

        val user = User(id = userId, name = "Test User")
        val coupon = Coupon(
            id = couponId,
            name = "Test Coupon",
            discountAmount = discountAmount,
            discountType = discountType,
            issueLimit = 100L,
            issuable = true
        )

        val userCoupon = UserCoupon.create(coupon, userId)
        userCoupon.id = 201L

        every { couponIssueUserRepository.add(any(), any()) } returns 1L
        every { couponIssueCountRepository.decrement(couponId) } returns 99L
        every { userRepository.findByIdOrNull(userId) } returns user
        every { couponRepository.findByIdOrNull(couponId) } returns coupon
        every { userCouponRepository.save(any()) } returns userCoupon
        every { couponRepository.save(any()) } returns coupon

        val result = couponService.issueCoupon(request)

        verify(exactly = 1) { userRepository.findByIdOrNull(userId) }
        verify(exactly = 1) { couponRepository.findByIdOrNull(couponId) }
        verify(exactly = 1) { userCouponRepository.save(any()) }

        assertEquals(userId, result.userId)
        assertEquals(userCoupon.id, result.userCouponId)
        assertEquals(discountType.name, result.discountType)
        assertEquals(discountAmount, result.discountAmount)
        assertEquals(userCoupon.createdAt, result.issuedAt)
    }

    @Test
    fun `issueCoupon 은 유저가 존재하지 않을 때 쿠폰 발급에 실패한다`() {
        val userId = 2L
        val couponId = 11L
        val discountAmount = 5000L
        val discountType = DiscountType.PRICE
        val coupon = Coupon(
            id = couponId,
            name = "Test Coupon",
            discountAmount = discountAmount,
            discountType = discountType,
            issueLimit = 100L,
            issuable = true
        )
        val request = CouponIssueRequest(userId = userId, couponId = couponId)

        every { couponIssueUserRepository.add(any(), any()) } returns 1L
        every { couponIssueCountRepository.decrement(couponId) } returns 99L
        every { couponIssueUserRepository.delete(any(), any()) } returns 1L
        every { couponIssueCountRepository.increment(couponId) } returns 100L
        every { couponRepository.findByIdOrNull(couponId) } returns coupon
        every { userRepository.findByIdOrNull(userId) } returns  null

        val exception = assertThrows(BusinessException::class.java) {
            couponService.issueCoupon(request)
        }

        assertEquals(ResponseStatus.USER_NOT_FOUND, exception.status)
    }

    @Test
    fun `issueCoupon 는 쿠폰이 존재하지 않을 때 실패한다`() {
        val userId = 3L
        val couponId = 9L

        val request = CouponIssueRequest(userId = userId, couponId = couponId)

        val user = User(id = userId, name = "Test User")

        every { couponIssueUserRepository.add(any(), any()) } returns 1L
        every { couponIssueCountRepository.decrement(couponId) } returns 99L
        every { couponIssueUserRepository.delete(any(), any()) } returns 1L
        every { couponIssueCountRepository.increment(couponId) } returns 100L
        every { userRepository.findByIdOrNull(userId) } returns user
        every { couponRepository.findByIdOrNull(couponId) } returns null

        val exception = assertThrows(BusinessException::class.java) {
            couponService.issueCoupon(request)
        }

        assertEquals(ResponseStatus.COUPON_NOT_FOUND, exception.status)
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
            issuable = false
        )

        every { couponIssueUserRepository.add(any(), any()) } returns 1L
        every { couponIssueCountRepository.decrement(couponId) } returns 99L
        every { couponIssueUserRepository.delete(any(), any()) } returns 1L
        every { couponIssueCountRepository.increment(couponId) } returns 100L
        every { couponRepository.findByIdOrNull(couponId) } returns coupon

        val exception = assertThrows(BusinessException::class.java) {
            couponService.issueCoupon(request)
        }

        assertEquals(ResponseStatus.INVALID_COUPON, exception.status)
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
            issuable = true
        )

        every { couponIssueUserRepository.add(any(), any()) } returns 1L
        every { couponIssueCountRepository.decrement(couponId) } returns -1L
        every { couponIssueUserRepository.delete(any(), any()) } returns 1L
        every { couponIssueCountRepository.increment(couponId) } returns 0L
        every { couponRepository.findByIdOrNull(couponId) } returns coupon

        val exception = assertThrows(BusinessException::class.java) {
            couponService.issueCoupon(request)
        }

        assertEquals(ResponseStatus.COUPON_OUT_OF_STOCK, exception.status)
    }

    @Test
    fun `issueCoupon 는 이미 발급 받은 유저가 쿠폰을 다시 발급받으려고 할 때 실패한다`() {
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
            issuable = true
        )

        every { couponIssueUserRepository.add(any(), any()) } returns 0L
        every { couponIssueCountRepository.decrement(couponId) } returns 99L
        every { couponRepository.findByIdOrNull(couponId) } returns coupon
        every { userRepository.findByIdOrNull(userId) } returns user

        val exception = assertThrows(BusinessException::class.java) {
            couponService.issueCoupon(request)
        }

        assertEquals(ResponseStatus.COUPON_ALREADY_ISSUED, exception.status)
    }

}
