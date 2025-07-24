package kr.hhplus.be.server.domain

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class ProductTest {

    @Test
    fun isStockSufficient() {
        val product = Product(name = "Test Product", price = 1000L, stock = 10)

        assertTrue(product.isStockSufficient(5))

        assertFalse(product.isStockSufficient(15))

        assertThrows<IllegalArgumentException> {
            product.isStockSufficient(-1)
        }
    }

    @Test
    fun reduceStock() {
        val product = Product(name = "Test Product", price = 1000L, stock = 10)

        product.reduceStock(5)
        assertEquals(5, product.stock)

        assertThrows<IllegalStateException> {
            product.reduceStock(10)
        }

        assertThrows<IllegalArgumentException> {
            product.reduceStock(-1)
        }
    }
}