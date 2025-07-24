package kr.hhplus.be.server.application

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.Product
import kr.hhplus.be.server.domain.ProductRepository
import kr.hhplus.be.server.global.exception.BusinessException
import kr.hhplus.be.server.global.exception.ResponseStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.*

class ProductServiceTest {

    private val productRepository = mockk<ProductRepository>()
    private val productService = ProductService(productRepository)

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
}