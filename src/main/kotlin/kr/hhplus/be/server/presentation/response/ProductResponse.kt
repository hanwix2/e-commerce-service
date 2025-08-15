package kr.hhplus.be.server.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.server.domain.Product
import java.io.Serializable

@Schema(description = "상품 정보")
data class ProductResponse(
    @Schema(description = "상품 ID")
    val productId: Long,

    @Schema(description = "상품명")
    val productName: String,

    @Schema(description = "상품 가격")
    val price: Long,

    @Schema(description = "재고 수량")
    val stock: Int,
) : Serializable {
    companion object {
        fun from(product: Product): ProductResponse {
            return ProductResponse(
                productId = product.id,
                productName = product.name,
                price = product.price,
                stock = product.stock
            )
        }
    }
}
