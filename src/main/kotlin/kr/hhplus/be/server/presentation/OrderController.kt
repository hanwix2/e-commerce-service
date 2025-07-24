package kr.hhplus.be.server.presentation

import kr.hhplus.be.server.presentation.request.OrderRequest
import kr.hhplus.be.server.presentation.response.OrderItemResponse
import kr.hhplus.be.server.presentation.response.OrderResponse
import kr.hhplus.be.server.presentation.docs.OrderApiDocs
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/orders")
class OrderController : OrderApiDocs {

    @PostMapping
    override fun order(@RequestBody req: OrderRequest): OrderResponse {

        return OrderResponse(
            UUID.randomUUID().toString(), req.userId, 30000, 10000, 20000,
            listOf(
                OrderItemResponse(UUID.randomUUID().toString(), 1001, "컴퓨터", 1, 150000),
                OrderItemResponse(UUID.randomUUID().toString(), 1002, "키보드", 1, 30000)
            ),
        )
    }
}