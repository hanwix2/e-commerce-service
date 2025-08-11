package kr.hhplus.be.server.application

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kr.hhplus.be.server.domain.*
import kr.hhplus.be.server.global.exception.BusinessException
import kr.hhplus.be.server.global.exception.ResponseStatus
import kr.hhplus.be.server.infrastructure.*
import kr.hhplus.be.server.presentation.request.OrderItemRequest
import kr.hhplus.be.server.presentation.request.OrderRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.*
import java.util.concurrent.CountDownLatch

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceIntegrationTest @Autowired constructor(
    private val orderService: OrderService,
    private val userRepository: UserRepository,
    private val userPointHistoryRepository: UserPointHistoryRepository,
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val couponRepository: CouponRepository,
    private val userCouponRepository: UserCouponRepository
) {

    private lateinit var user: User
    private lateinit var product: Product

    @BeforeEach
    fun setUp() {
        user = userRepository.save(User(name = "Test User", point = 200L))
        product = Product(name = "Test Product", price = 100L, stock = 100)
        product = productRepository.save(product)
    }

    @Test
    fun `order - 포인트를 차감하고 주문,결제 정보를 저장한다`() {
        // Given
        val request = OrderRequest(
            userId = user.id,
            orderItems = listOf(OrderItemRequest(productId = product.id, quantity = 1)),
            userCouponId = null
        )
        val initialPoint = user.point
        val paymentAmount = request.orderItems.sumOf { it.quantity * product.price }

        // When
        val response = orderService.order(request)

        // Then
        assertThat(response.orderId).isNotNull
        assertThat(response.userId).isEqualTo(user.id)
        assertThat(response.totalPrice).isEqualTo(product.price * request.orderItems[0].quantity)
        assertThat(response.discountAmount).isEqualTo(0L)
        assertThat(response.paidAmount).isEqualTo(100L)
        assertThat(response.items).hasSize(1)
        assertThat(response.items[0].productId).isEqualTo(product.id)
        assertThat(response.items[0].quantity).isEqualTo(request.orderItems[0].quantity)

        val orders = orderRepository.findByUserId(user.id).also { orders ->
            assertThat(orders).isNotEmpty
            assertThat(orders[0].totalPrice).isEqualTo(response.totalPrice)
            assertThat(orders[0].status).isEqualTo(OrderStatus.PURCHASE)
        }

        orderItemRepository.findByOrder(orders[0]).also { orderItems ->
            assertThat(orderItems).hasSize(1)
            assertThat(orderItems[0].productId).isEqualTo(product.id)
            assertThat(orderItems[0].quantity).isEqualTo(request.orderItems[0].quantity)
        }

        userPointHistoryRepository.findByUserId(user.id).also { pointHistories ->
            assertThat(pointHistories).hasSize(1)
            assertThat(pointHistories[0].userId).isEqualTo(user.id)
            assertThat(pointHistories[0].type).isEqualTo(PointHistoryType.USE)
            assertThat(pointHistories[0].amount).isEqualTo(paymentAmount)
        }

        userRepository.findById(user.id).ifPresent {
            assertThat(it.point).isEqualTo(initialPoint - paymentAmount)
        }
    }

    @Test
    fun `order - 쿠폰을 사용하여 주문, 결제 정보를 저장한다`() {
        // Given
        val coupon =
            couponRepository.save(Coupon(name = "Test Coupon", discountAmount = 50L, discountType = DiscountType.PRICE))
        val userCoupon = userCouponRepository.save(
            UserCoupon(
                userId = user.id,
                coupon = coupon,
                discountType = coupon.discountType,
                discountAmount = coupon.discountAmount
            )
        )

        val request = OrderRequest(
            userId = user.id,
            orderItems = listOf(OrderItemRequest(productId = product.id, quantity = 1)),
            userCouponId = userCoupon.id
        )

        val totalPrice = request.orderItems.sumOf { it.quantity * product.price }

        // When
        val response = orderService.order(request)

        // Then
        assertThat(response.orderId).isNotNull
        assertThat(response.userId).isEqualTo(user.id)
        assertThat(response.totalPrice).isEqualTo(totalPrice)
        assertThat(response.discountAmount).isEqualTo(coupon.discountAmount)
        assertThat(response.paidAmount).isEqualTo(totalPrice - coupon.discountAmount)
        assertThat(response.items).hasSize(1)
        assertThat(response.items[0].productId).isEqualTo(product.id)
        assertThat(response.items[0].quantity).isEqualTo(request.orderItems[0].quantity)

        userCouponRepository.findById(userCoupon.id).ifPresent {
            assertThat(it.status).isEqualTo(UserCouponStatus.USED)
            assertThat(it.paymentId).isNotNull
            assertThat(it.usedAt).isNotNull
        }
    }

    @Test
    fun `order - 상품의 재고가 부족하면 실패한다`() {
        // Given
        val savedProduct = productRepository.save(Product(name = "Test Product", price = 100L, stock = 0))

        val request = OrderRequest(
            userId = user.id,
            orderItems = listOf(OrderItemRequest(productId = savedProduct.id, quantity = 1)),
            userCouponId = null
        )

        // When & Then
        assertThrows<BusinessException> {
            orderService.order(request)
        }.also { exception ->
            assertThat(exception.status).isEqualTo(ResponseStatus.PRODUCT_OUT_OF_STOCK)
        }
    }

    @Test
    fun `order - 유저의 포인트가 부족하면 실패한다`() {
        // Given
        val request = OrderRequest(
            userId = user.id,
            orderItems = listOf(OrderItemRequest(productId = product.id, quantity = 10)),
            userCouponId = null
        )

        // When & Then
        assertThrows<BusinessException> {
            orderService.order(request)
        }.also { exception ->
            assertThat(exception.status).isEqualTo(ResponseStatus.POINT_NOT_ENOUGH)
        }
    }

    @Test
    fun `order - 존재하지 않는 쿠폰으로 주문을 시도하면 실패한다`() {
        // Given
        val request = OrderRequest(
            userId = user.id,
            orderItems = listOf(OrderItemRequest(productId = product.id, quantity = 1)),
            userCouponId = 0L
        )

        // When & Then
        assertThrows<BusinessException> {
            orderService.order(request)
        }.also { exception ->
            assertThat(exception.status).isEqualTo(ResponseStatus.COUPON_NOT_FOUND)
        }
    }

    @Test
    fun `order - 주문 상품이 하나라도 존재하지 않으면 실패한다`() {
        // Given
        val request = OrderRequest(
            userId = user.id,
            orderItems = listOf(OrderItemRequest(productId = 0L, quantity = 1)),
            userCouponId = null
        )

        // When & Then
        assertThrows<BusinessException> {
            orderService.order(request)
        }.also { exception ->
            assertThat(exception.status).isEqualTo(ResponseStatus.PRODUCT_NOT_FOUND)
        }
    }

    @Test
    fun `order - (동시성 테스트) 여러 사용자가 동시에 주문을 하는 경우 재고 수량만큼 주문이 성공해야 한다`() {
        // Given
        val users = listOf(
            userRepository.save(User(name = "User1", point = 1000L)),
            userRepository.save(User(name = "User2", point = 1000L)),
            userRepository.save(User(name = "User3", point = 1000L)),
            userRepository.save(User(name = "User4", point = 1000L)),
            userRepository.save(User(name = "User5", point = 1000L)),
            userRepository.save(User(name = "User6", point = 1000L)),
            userRepository.save(User(name = "User7", point = 1000L)),
            userRepository.save(User(name = "User8", point = 1000L)),
            userRepository.save(User(name = "User9", point = 1000L)),
            userRepository.save(User(name = "User10", point = 1000L))
        )

        val stock = 2

        val product = productRepository.save(Product(name = "Product", price = 100L, stock = stock))


        val latch = CountDownLatch(users.size)

        // When
        val exceptions = Collections.synchronizedCollection(mutableListOf<Exception>())

        runBlocking {
            users.forEach { user ->
                launch {
                    try {
                        val request = OrderRequest(
                            userId = user.id,
                            orderItems = listOf(OrderItemRequest(productId = product.id, quantity = 1))
                        )

                        orderService.order(request)
                    } catch (e: BusinessException) {
                        if (e.status == ResponseStatus.PRODUCT_OUT_OF_STOCK) {
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
        val productResult = productRepository.findByIdOrThrow(product.id)
        assertThat(productResult.stock).isEqualTo(0)

        assertThat(exceptions).hasSize(users.size - stock)

    }

    @Test
    fun `order - (동시성 테스트) 한명의 사용자가 동시에 여러 번 구매를 시도할 때 포인트 여유가 되는만큼 주문이 성공해야 한다`() {
        // Given
        val userPoint = 300L
        val user = userRepository.save(User(name = "User", point = userPoint))

        val stock = 10
        val productPrice = 100L
        val product = productRepository.save(Product(name = "Product", price = productPrice, stock = stock))

        val orderCount = 10
        val latch = CountDownLatch(orderCount)

        val request = OrderRequest(
            userId = user.id,
            orderItems = listOf(OrderItemRequest(productId = product.id, quantity = 1))
        )

        // When
        val exceptions = Collections.synchronizedCollection(mutableListOf<Exception>())

        runBlocking {
            repeat(orderCount) {
                launch {
                    try {
                        orderService.order(request)
                    } catch (e: BusinessException) {
                        if (e.status == ResponseStatus.POINT_NOT_ENOUGH) {
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
        val productResult = productRepository.findByIdOrThrow(product.id)
        assertThat(productResult.stock).isEqualTo(stock - (userPoint / productPrice).toInt())

        val orders = orderRepository.findByUserId(user.id)
        assertThat(orders).hasSize((userPoint / productPrice).toInt())

        assertThat(exceptions).hasSize(
            orderCount - (userPoint / productPrice).toInt()
        )
    }

}