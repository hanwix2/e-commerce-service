package kr.hhplus.be.server.application

import kr.hhplus.be.server.domain.OrderItem
import kr.hhplus.be.server.infrastructure.OrderItemRepository
import kr.hhplus.be.server.domain.Product
import kr.hhplus.be.server.infrastructure.ProductRepository
import kr.hhplus.be.server.global.exception.BusinessException
import kr.hhplus.be.server.global.exception.ResponseStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductServiceIntegrationTest @Autowired constructor(
    private val productService: ProductService,
    private val productRepository: ProductRepository,
    private val orderItemRepository: OrderItemRepository
) {

    private lateinit var product1: Product
    private lateinit var product2: Product
    private lateinit var product3: Product
    private lateinit var product4: Product
    private lateinit var product5: Product


    @BeforeEach
    fun setUp() {
        product1 = Product(name = "Test Product 1", price = 100L, stock = 10)
        product2 = Product(name = "Test Product 2", price = 200L, stock = 20)
        product3 = Product(name = "Test Product 3", price = 300L, stock = 30)
        product4 = Product(name = "Test Product 4", price = 400L, stock = 10)
        product5 = Product(name = "Test Product 5", price = 500L, stock = 20)

        productRepository.saveAll(
            listOf(product1, product2, product3, product4, product5)
        )
    }

    @Test
    fun `getProductInfo - 상품 정보를 조회`() {
        // Given
        val productId = product1.id

        // When
        val response = productService.getProductInfo(productId)

        // Then
        assertThat(response.productId).isEqualTo(productId)
        assertThat(response.productName).isEqualTo(product1.name)
        assertThat(response.price).isEqualTo(product1.price)
        assertThat(response.stock).isEqualTo(product1.stock)
    }

    @Test
    fun `getProductInfo - 존재하지 않는 상품 ID로 조회 시 실패한다`() {
        // Given
        val productId = 0L

        // When & Then
        assertThrows<BusinessException> {
            productService.getProductInfo(productId)
        }.also { exception ->
            assertThat(exception.status).isEqualTo(ResponseStatus.PRODUCT_NOT_FOUND)
        }
    }

    @Test
    fun `getPopularProducts - 인기 상품 상위 5개를 순서대로 조회`() {
        // Given
        orderItemRepository.saveAll(
            listOf(
                OrderItem(productId = product1.id, quantity = 5),
                OrderItem(productId = product2.id, quantity = 3),
                OrderItem(productId = product3.id, quantity = 2),
                OrderItem(productId = product4.id, quantity = 1),
                OrderItem(productId = product5.id, quantity = 4)
            )
        )

        // When
        val response = productService.getPopularProducts()

        // Then
        assertThat(response).hasSize(5)
        assertThat(response[0].productId).isEqualTo(product1.id)
        assertThat(response[1].productId).isEqualTo(product5.id)
        assertThat(response[2].productId).isEqualTo(product2.id)
        assertThat(response[3].productId).isEqualTo(product3.id)
        assertThat(response[4].productId).isEqualTo(product4.id)
    }
}