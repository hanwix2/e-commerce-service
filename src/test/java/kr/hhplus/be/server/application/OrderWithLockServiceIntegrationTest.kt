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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.*
import java.util.concurrent.CountDownLatch

@SpringBootTest
@ActiveProfiles("test")
class OrderWithLockServiceIntegrationTest @Autowired constructor(
    private val orderWithLockService: OrderWithLockService,
    private val userRepository: UserRepository,
    private val userPointHistoryRepository: UserPointHistoryRepository,
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val couponRepository: CouponRepository,
    private val userCouponRepository: UserCouponRepository
) {

    @BeforeEach
    fun setUp() {
        orderItemRepository.deleteAll()
        orderRepository.deleteAll()
        userPointHistoryRepository.deleteAll()
        userCouponRepository.deleteAll()
        couponRepository.deleteAll()
        productRepository.deleteAll()
        userRepository.deleteAll()
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

                        orderWithLockService.order(request)
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
                        orderWithLockService.order(request)
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