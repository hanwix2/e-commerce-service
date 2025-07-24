package kr.hhplus.be.server.application

import kr.hhplus.be.server.domain.ProductRepository
import kr.hhplus.be.server.global.exception.BusinessException
import kr.hhplus.be.server.global.exception.ResponseStatus
import kr.hhplus.be.server.presentation.response.ProductResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository
) {

    @Transactional(readOnly = true)
    fun getProductInfo(productId: Long): ProductResponse {
        val product = productRepository.findById(productId)
            .orElseThrow { BusinessException(ResponseStatus.PRODUCT_NOT_FOUND) }

        return ProductResponse(
            productId = product.id,
            productName = product.name,
            price = product.price,
            stock = product.stock
        )
    }

}