package kr.hhplus.be.server.application

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.infrastructure.OrderItemRepository
import kr.hhplus.be.server.domain.Product
import kr.hhplus.be.server.infrastructure.ProductRepository
import kr.hhplus.be.server.global.exception.BusinessException
import kr.hhplus.be.server.global.exception.ResponseStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class ProductServiceTest {

    private val productRepository = mockk<ProductRepository>()
    private val orderItemRepository = mockk<OrderItemRepository>()
    private lateinit var productService: ProductService

    @BeforeEach
    fun setUp() {
        productService = ProductService(productRepository, orderItemRepository)
    }

    @Test
    fun `getProductInfo 는 상품 정보를 반환한다`() {
        val productId = 1L
        val productName = "Test Product"
        val price = 1000L
        val stock = 100

        val product = Product(
            id = productId,
            name = productName,
            price = price,
            stock = stock
        )

        every { productRepository.findById(productId) } returns Optional.of(product)

        val result = productService.getProductInfo(productId)

        assertEquals(productId, result.productId)
        assertEquals(productName, result.productName)
        assertEquals(price, result.price)
        assertEquals(stock, result.stock)

        verify(exactly = 1) { productRepository.findById(productId) }
    }

    @Test
    fun `getProductInfo 는 상품이 존재하지 않을 시 예외를 던진다`() {
        val productId = 2L

        every { productRepository.findById(productId) } returns Optional.empty()

        val exception = assertThrows(BusinessException::class.java) {
            productService.getProductInfo(productId)
        }

        assertEquals(ResponseStatus.PRODUCT_NOT_FOUND, exception.status)
        verify(exactly = 1) { productRepository.findById(productId) }
    }

    @Test
    fun `getPopularProducts 는 최근 3일간 가장 많이 팔린 상위 5개 상품을 반환한다`() {
        val product1 = Product(id = 1L, name = "Product 1", price = 1000L, stock = 100)
        val product2 = Product(id = 2L, name = "Product 2", price = 2000L, stock = 200)
        val product3 = Product(id = 3L, name = "Product 3", price = 3000L, stock = 300)
        val product4 = Product(id = 4L, name = "Product 4", price = 4000L, stock = 400)
        val product5 = Product(id = 5L, name = "Product 5", price = 5000L, stock = 500)

        val productIds = listOf(1L, 2L, 3L, 4L, 5L)
        val products = listOf(product1, product2, product3, product4, product5)

        every {
            orderItemRepository.findTopDistinctPurchasedProductIdsByCreatedAtAfter(
                ProductService.POPULAR_PRODUCT_LIMIT, 
                any()
            ) 
        } returns productIds

        every { productRepository.findAllByIdIn(productIds) } returns products

        val result = productService.getPopularProducts()

        assertEquals(5, result.size)

        for (i in 0 until 5) {
            assertEquals(productIds[i], result[i].productId)
            assertEquals("Product ${i+1}", result[i].productName)
        }

        verify(exactly = 1) {
            orderItemRepository.findTopDistinctPurchasedProductIdsByCreatedAtAfter(
                ProductService.POPULAR_PRODUCT_LIMIT, 
                any()
            ) 
        }
        verify(exactly = 1) { productRepository.findAllByIdIn(productIds) }
    }

    @Test
    fun `getPopularProducts 는 인기 상품이 없을 경우 빈 리스트를 반환한다`() {
        val emptyProductIds = emptyList<Long>()

        every {
            orderItemRepository.findTopDistinctPurchasedProductIdsByCreatedAtAfter(
                ProductService.POPULAR_PRODUCT_LIMIT, 
                any()
            ) 
        } returns emptyProductIds

        every { productRepository.findAllByIdIn(emptyProductIds) } returns emptyList()

        val result = productService.getPopularProducts()

        assertEquals(0, result.size)

        verify(exactly = 1) {
            orderItemRepository.findTopDistinctPurchasedProductIdsByCreatedAtAfter(
                ProductService.POPULAR_PRODUCT_LIMIT, 
                any()
            ) 
        }
        verify(exactly = 1) { productRepository.findAllByIdIn(emptyProductIds) }
    }
}
