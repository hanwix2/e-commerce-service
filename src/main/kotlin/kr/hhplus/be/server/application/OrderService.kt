package kr.hhplus.be.server.application

import kr.hhplus.be.server.domain.*
import kr.hhplus.be.server.global.exception.BusinessException
import kr.hhplus.be.server.global.exception.ResponseStatus
import kr.hhplus.be.server.infrastructure.*
import kr.hhplus.be.server.presentation.request.OrderRequest
import kr.hhplus.be.server.presentation.response.OrderItemResponse
import kr.hhplus.be.server.presentation.response.OrderResponse
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val paymentRepository: PaymentRepository,
    private val userCouponRepository: UserCouponRepository,
    private val userPointHistoryRepository: UserPointHistoryRepository
) {

    @Transactional
    @Retryable(
        value = [ObjectOptimisticLockingFailureException::class],
        maxAttempts = 5
    )
    fun order(request: OrderRequest): OrderResponse {
        val user = userRepository.findByIdOrThrow(request.userId)

        // 주문 정보 검증 및 생성
        val orderProducts = getOrderProducts(request)
        val totalPrice = orderProducts.sumOf { it.getTotalPrice() }

        val order = orderRepository.save(Order.createPurchase(user, totalPrice))
        val orderItems = createOrderItems(orderProducts, order)

        // 할인 정보 검증 및 결제 정보 생성
        val (discountAmount, paidAmount) = createPaymentInfoAndGetAmount(request, totalPrice, user, orderProducts, order)

        usePoint(user, paidAmount)

        return buildOrderResponse(order, user, totalPrice, discountAmount, paidAmount, orderItems)
    }

    private fun createPaymentInfoAndGetAmount(
        request: OrderRequest,
        totalPrice: Long,
        user: User,
        orderProducts: List<OrderProduct>,
        order: Order
    ): Pair<Long, Long> {
        val userCoupon = getUserCoupon(request)
        val discountAmount = userCoupon?.getDiscountPriceAmount(totalPrice) ?: 0L

        val paidAmount = totalPrice - discountAmount
        if (user.isPointInsufficient(paidAmount)) {
            throw BusinessException(ResponseStatus.POINT_NOT_ENOUGH)
        }

        orderProducts.forEach { orderProduct ->
            val product = orderProduct.product
            product.reduceStock(orderProduct.quantity)
        }

        val payment = Payment(
            order = order,
            discountAmount = discountAmount,
            paidAmount = paidAmount
        )
        paymentRepository.save(payment)

        userCoupon?.let {
            it.use(payment.id, payment.createdAt)
            userCouponRepository.save(it)
        }
        return Pair(discountAmount, paidAmount)
    }

    private fun buildOrderResponse(
        order: Order,
        user: User,
        totalPrice: Long,
        discountAmount: Long,
        paidAmount: Long,
        orderItems: List<OrderItem>
    ) = OrderResponse(
        orderId = order.id.toString(),
        userId = user.id,
        totalPrice = totalPrice,
        discountAmount = discountAmount,
        paidAmount = paidAmount,
        items = orderItems.map { orderItem ->
            OrderItemResponse(
                orderItemId = orderItem.id.toString(),
                productId = orderItem.productId,
                productName = orderItem.productName,
                quantity = orderItem.quantity,
                price = orderItem.price
            )
        }
    )

    private fun getUserCoupon(request: OrderRequest): UserCoupon? {
        return request.userCouponId?.let { couponId ->
            val userCoupon = userCouponRepository.findByIdOrThrow(couponId, request.userId)

            if (!userCoupon.isAvailable()) {
                throw BusinessException(ResponseStatus.INVALID_COUPON)
            }
            return userCoupon
        }
    }

        private fun createOrderItems(orderProducts: List<OrderProduct>, order: Order): List<OrderItem> {
        val orderItems = orderProducts.map { product -> OrderItem.of(product, order) }
        return orderItemRepository.saveAll(orderItems)
    }

    private fun usePoint(user: User, paidAmount: Long) {
        user.reducePoint(paidAmount)
        userRepository.save(user)

        val pointHistory = UserPointHistory.createUseHistory(user.id, paidAmount)
        userPointHistoryRepository.save(pointHistory)
    }

    private fun getOrderProducts(request: OrderRequest) =
        request.orderItems.map { itemRequest ->
            val product = productRepository.findByIdOrThrow(itemRequest.productId)

            if (product.isStockInsufficient(itemRequest.quantity)) {
                throw BusinessException(ResponseStatus.PRODUCT_OUT_OF_STOCK)
            }

            OrderProduct(
                product = product,
                quantity = itemRequest.quantity
            )
        }
}
