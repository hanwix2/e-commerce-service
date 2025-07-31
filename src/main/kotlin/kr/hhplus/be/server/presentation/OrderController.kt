package kr.hhplus.be.server.presentation

import jakarta.validation.Valid
import kr.hhplus.be.server.application.OrderService
import kr.hhplus.be.server.presentation.docs.OrderApiDocs
import kr.hhplus.be.server.presentation.request.OrderRequest
import kr.hhplus.be.server.presentation.response.OrderResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val orderService: OrderService
) : OrderApiDocs {

    @PostMapping
    override fun order(@Valid @RequestBody request: OrderRequest): OrderResponse {
        return orderService.order(request);
    }
}