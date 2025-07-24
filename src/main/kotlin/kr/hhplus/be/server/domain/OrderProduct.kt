package kr.hhplus.be.server.domain

data class OrderProduct(
    val product: Product,
    val quantity: Int
) {

    fun getTotalPrice(): Long {
        return product.price * quantity
    }
}
