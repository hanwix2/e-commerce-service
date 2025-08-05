package kr.hhplus.be.server.application

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.*
import kr.hhplus.be.server.global.exception.BusinessException
import kr.hhplus.be.server.global.exception.ResponseStatus
import kr.hhplus.be.server.infrastructure.*
import kr.hhplus.be.server.presentation.request.OrderItemRequest
import kr.hhplus.be.server.presentation.request.OrderRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull

class OrderServiceTest {

    private val userRepository = mockk<UserRepository>()
    private val productRepository = mockk<ProductRepository>()
    private val orderRepository = mockk<OrderRepository>()
    private val orderItemRepository = mockk<OrderItemRepository>()
    private val paymentRepository = mockk<PaymentRepository>()
    private val userCouponRepository = mockk<UserCouponRepository>()
    private val userPointHistoryRepository = mockk<UserPointHistoryRepository>()

    private lateinit var orderService: OrderService

    @BeforeEach
    fun setUp() {
        orderService = OrderService(
            userRepository,
            productRepository,
            orderRepository,
            orderItemRepository,
            paymentRepository,
            userCouponRepository,
            userPointHistoryRepository
        )
    }

    @Test
    fun `order 결제 성공 - 결제 정보 저장 & 포인트, 상품 재고 차감`() {
        val userId = 1L
        val productId1 = 101L
        val productId2 = 102L
        val quantity1 = 2
        val quantity2 = 1
        val productPrice1 = 10000L
        val productPrice2 = 5000L
        val userPoint = 50000L

        val orderRequest = OrderRequest(
            userId = userId,
            orderItems = listOf(
                OrderItemRequest(productId = productId1, quantity = quantity1),
                OrderItemRequest(productId = productId2, quantity = quantity2)
            )
        )

        val user = User(id = userId, name = "Test User", point = userPoint)
        val product1 = Product(id = productId1, name = "Product 1", price = productPrice1, stock = 10)
        val product2 = Product(id = productId2, name = "Product 2", price = productPrice2, stock = 5)

        val totalPrice = (productPrice1 * quantity1) + (productPrice2 * quantity2)
        val order = Order(id = 1L, user = user, totalPrice = totalPrice)

        val orderItem1 = OrderItem(
            id = 1L,
            order = order,
            productId = productId1,
            productName = "Product 1",
            price = productPrice1,
            quantity = quantity1
        )

        val orderItem2 = OrderItem(
            id = 2L,
            order = order,
            productId = productId2,
            productName = "Product 2",
            price = productPrice2,
            quantity = quantity2
        )

        val payment = Payment(
            id = 1L,
            order = order,
            discountAmount = 0L,
            paidAmount = totalPrice
        )

        every { userRepository.findByIdOrNull(userId) } returns user
        every { productRepository.findByIdOrNull(productId1) } returns product1
        every { productRepository.findByIdOrNull(productId2) } returns product2
        every { orderRepository.save(any()) } returns order
        every { orderItemRepository.saveAll(any<List<OrderItem>>()) } returns listOf(orderItem1, orderItem2)
        every { paymentRepository.save(any()) } returns payment
        every { userRepository.save(any()) } returns user
        every { userPointHistoryRepository.save(any()) } returns mockk()

        val result = orderService.order(orderRequest)

        assertEquals(order.id.toString(), result.orderId)
        assertEquals(userId, result.userId)
        assertEquals(totalPrice, result.totalPrice)
        assertEquals(0L, result.discountAmount)
        assertEquals(totalPrice, result.paidAmount)
        assertEquals(2, result.items.size)

        verify(exactly = 1) { userRepository.findByIdOrNull(userId) }
        verify(exactly = 1) { productRepository.findByIdOrNull(productId1) }
        verify(exactly = 1) { productRepository.findByIdOrNull(productId2) }
        verify(exactly = 1) { orderRepository.save(any()) }
        verify(exactly = 1) { orderItemRepository.saveAll(any<List<OrderItem>>()) }
        verify(exactly = 1) { paymentRepository.save(any()) }
        verify(exactly = 1) { userRepository.save(any()) }
        verify(exactly = 1) { userPointHistoryRepository.save(any()) }
        verify(exactly = 0) { userCouponRepository.findByIdOrNull(any()) }

        assertEquals(10 - quantity1, product1.stock)
        assertEquals(5 - quantity2, product2.stock)

        assertEquals(userPoint - totalPrice, user.point)
    }

    @Test
    fun `order 는 쿠폰 할인을 받아 주문을 완료할 수 있다`() {
        val userId = 1L
        val productId = 101L
        val quantity = 2
        val productPrice = 10000L
        val userPoint = 50000L
        val couponId = 201L
        val discountAmount = 5000L

        val orderRequest = OrderRequest(
            userId = userId,
            userCouponId = couponId,
            orderItems = listOf(
                OrderItemRequest(productId = productId, quantity = quantity)
            )
        )

        val user = User(id = userId, name = "Test User", point = userPoint)
        val product = Product(id = productId, name = "Product 1", price = productPrice, stock = 10)
        val totalPrice = productPrice * quantity
        val order = Order(id = 1L, user = user, totalPrice = totalPrice)

        val userCoupon = UserCoupon(
            id = couponId,
            userId = userId,
            discountType = DiscountType.PRICE,
            discountAmount = discountAmount,
            status = UserCouponStatus.ACTIVE
        )

        val orderProduct = OrderProduct(product = product, quantity = quantity)

        val orderItem = OrderItem(
            id = 1L,
            order = order,
            productId = productId,
            productName = "Product 1",
            price = productPrice,
            quantity = quantity
        )

        val paidAmount = totalPrice - discountAmount
        val payment = Payment(
            id = 1L,
            order = order,
            discountAmount = discountAmount,
            paidAmount = paidAmount
        )

        every { userRepository.findByIdOrNull(userId) } returns user
        every { productRepository.findByIdOrNull(productId) } returns product
        every { orderRepository.save(any()) } returns order
        every { orderItemRepository.saveAll(any<List<OrderItem>>()) } returns listOf(orderItem)
        every { userCouponRepository.findByIdAndUserId(couponId, userId) } returns userCoupon
        every { paymentRepository.save(any()) } returns payment
        every { userCouponRepository.save(any()) } returns userCoupon
        every { userRepository.save(any()) } returns user
        every { userPointHistoryRepository.save(any()) } returns mockk()

        val result = orderService.order(orderRequest)

        assertEquals(order.id.toString(), result.orderId)
        assertEquals(userId, result.userId)
        assertEquals(totalPrice, result.totalPrice)
        assertEquals(discountAmount, result.discountAmount)
        assertEquals(paidAmount, result.paidAmount)

        verify(exactly = 1) { userCouponRepository.findByIdAndUserId(any(), any()) }
        verify(exactly = 1) { userCouponRepository.save(any()) }

        assertEquals(UserCouponStatus.USED, userCoupon.status)
        assertNotNull(userCoupon.paymentId)
        assertNotNull(userCoupon.usedAt)

        assertEquals(userPoint - paidAmount, user.point)
    }

    @Test
    fun `order 는 유저가 존재하지 않을 때 결제에 실패 한다`() {
        val userId = 999L
        val orderRequest = OrderRequest(
            userId = userId,
            orderItems = listOf(
                OrderItemRequest(productId = 101L, quantity = 1)
            )
        )

        every { userRepository.findByIdOrNull(userId) } returns null

        val exception = assertThrows(BusinessException::class.java) {
            orderService.order(orderRequest)
        }

        assertEquals(ResponseStatus.USER_NOT_FOUND, exception.status)
        verify(exactly = 1) { userRepository.findByIdOrNull(userId) }
        verify(exactly = 0) { productRepository.findByIdOrNull(any()) }
        verify(exactly = 0) { orderRepository.save(any()) }
    }

    @Test
    fun `order 는 주문 상품이 하나라도 존재하지 않으면 결제에 실패한다`() {
        // Given
        val userId = 1L
        val productId = 999L
        val orderRequest = OrderRequest(
            userId = userId,
            orderItems = listOf(
                OrderItemRequest(productId = productId, quantity = 1)
            )
        )

        val user = User(id = userId, name = "Test User", point = 50000L)

        every { userRepository.findByIdOrNull(userId) } returns user
        every { productRepository.findByIdOrNull(productId) } returns null

        val exception = assertThrows(BusinessException::class.java) {
            orderService.order(orderRequest)
        }

        assertEquals(ResponseStatus.PRODUCT_NOT_FOUND, exception.status)
        verify(exactly = 1) { userRepository.findByIdOrNull(userId) }
        verify(exactly = 1) { productRepository.findByIdOrNull(productId) }
        verify(exactly = 0) { orderRepository.save(any()) }
    }

    @Test
    fun `order 는 상품 재고가 충분하지 않을 때 결제에 실패한다`() {
        val userId = 1L
        val productId = 101L
        val quantity = 10
        val orderRequest = OrderRequest(
            userId = userId,
            orderItems = listOf(
                OrderItemRequest(productId = productId, quantity = quantity)
            )
        )

        val user = User(id = userId, name = "Test User", point = 50000L)
        val product = Product(id = productId, name = "Product 1", price = 10000L, stock = 5) // Stock less than quantity

        every { userRepository.findByIdOrNull(userId) } returns user
        every { productRepository.findByIdOrNull(productId) } returns product

        val exception = assertThrows(BusinessException::class.java) {
            orderService.order(orderRequest)
        }

        assertEquals(ResponseStatus.PRODUCT_OUT_OF_STOCK, exception.status)
        verify(exactly = 1) { userRepository.findByIdOrNull(userId) }
        verify(exactly = 1) { productRepository.findByIdOrNull(productId) }
        verify(exactly = 0) { orderRepository.save(any()) }
    }

    @Test
    fun `order 는 유저가 포인트가 충분하지 않을 때 결제에 실패한다`() {
        val userId = 1L
        val productId = 101L
        val quantity = 2
        val productPrice = 10000L
        val userPoint = 5000L

        val orderRequest = OrderRequest(
            userId = userId,
            orderItems = listOf(
                OrderItemRequest(productId = productId, quantity = quantity)
            )
        )

        val user = User(id = userId, name = "Test User", point = userPoint)
        val product = Product(id = productId, name = "Product 1", price = productPrice, stock = 10)
        val order = Order(id = 1L, user = user, totalPrice = productPrice * quantity)
        val orderItem = OrderItem(
            id = 1L,
            order = order,
            productId = productId,
            productName = "Product 1",
            price = productPrice,
            quantity = quantity
        )

        every { userRepository.findByIdOrNull(userId) } returns user
        every { productRepository.findByIdOrNull(productId) } returns product
        every { orderRepository.save(any()) } returns order
        every { orderItemRepository.saveAll(any<List<OrderItem>>()) } returns listOf(orderItem)

        val exception = assertThrows(BusinessException::class.java) {
            orderService.order(orderRequest)
        }
        assertEquals(ResponseStatus.POINT_NOT_ENOUGH, exception.status)
    }

    @Test
    fun `order 는 쿠폰이 존재하지 않을 때`() {
        val userId = 1L
        val productId = 101L
        val couponId = 999L

        val orderRequest = OrderRequest(
            userId = userId,
            userCouponId = couponId,
            orderItems = listOf(
                OrderItemRequest(productId = productId, quantity = 1)
            )
        )

        val user = User(id = userId, name = "Test User", point = 50000L)
        val product = Product(id = productId, name = "Product 1", price = 10000L, stock = 10)
        val order = Order(id = 1L, user = user, totalPrice = 10000L)

        every { userRepository.findByIdOrNull(userId) } returns user
        every { productRepository.findByIdOrNull(productId) } returns product
        every { orderRepository.save(any()) } returns order
        every { orderItemRepository.saveAll(any<List<OrderItem>>()) } returns listOf(mockk())
        every { userCouponRepository.findByIdAndUserId(couponId, userId) } returns null

        val exception = assertThrows(BusinessException::class.java) {
            orderService.order(orderRequest)
        }
        assertEquals(ResponseStatus.COUPON_NOT_FOUND, exception.status)
    }

    @Test
    fun `order 는 쿠폰이 유효하지 않을 때(이미 사용) 예외를 던진다`() {
        val userId = 1L
        val productId = 101L
        val couponId = 201L

        val orderRequest = OrderRequest(
            userId = userId,
            userCouponId = couponId,
            orderItems = listOf(
                OrderItemRequest(productId = productId, quantity = 1)
            )
        )

        val user = User(id = userId, name = "Test User", point = 50000L)
        val product = Product(id = productId, name = "Product 1", price = 10000L, stock = 10)
        val order = Order(id = 1L, user = user, totalPrice = 10000L)

        val userCoupon = UserCoupon(
            id = couponId,
            userId = userId,
            discountType = DiscountType.PRICE,
            discountAmount = 5000L,
            status = UserCouponStatus.USED
        )

        every { userRepository.findByIdOrNull(userId) } returns user
        every { productRepository.findByIdOrNull(productId) } returns product
        every { orderRepository.save(any()) } returns order
        every { orderItemRepository.saveAll(any<List<OrderItem>>()) } returns listOf(mockk())
        every { userCouponRepository.findByIdAndUserId(couponId, userId) } returns userCoupon

        val exception = assertThrows(BusinessException::class.java) {
            orderService.order(orderRequest)
        }
        assertEquals(ResponseStatus.INVALID_COUPON, exception.status)
    }
}
