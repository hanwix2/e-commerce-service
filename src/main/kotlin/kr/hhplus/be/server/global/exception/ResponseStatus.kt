package kr.hhplus.be.server.global.exception

import org.springframework.http.HttpStatus

enum class ResponseStatus(
    val httpStatus: HttpStatus,
    val code: Int,
    val message: String,
) {
    // Common (0xxx)
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, 101, "유효하지 않은 파라미터가 존재합니다."),

    // User (1xxx)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 1001, "사용자를 찾을 수 없습니다."),
    POINT_CHARGE_FAILED(HttpStatus.CONFLICT, 1002, "포인트 충전이 실패했습니다."),
    POINT_NOT_ENOUGH(HttpStatus.BAD_REQUEST, 1003, "포인트가 부족합니다."),

    // Product (2xxx)
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, 2001, "존재하지 않는 상품입니다."),
    PRODUCT_OUT_OF_STOCK(HttpStatus.UNPROCESSABLE_ENTITY, 2002, "상품의 재고가 부족합니다."),

    // Order (3xxx)
    ORDER_FAILED(HttpStatus.CONFLICT, 3001, "주문이 실패했습니다."),

    // Coupon (4xxx)
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, 4001, "쿠폰을 찾을 수 없습니다."),
    INVALID_COUPON(HttpStatus.BAD_REQUEST, 4002, "쿠폰이 유효하지 않습니다."),
    COUPON_EXPIRED(HttpStatus.BAD_REQUEST, 4003, "쿠폰이 만료되었습니다."),
    COUPON_OUT_OF_STOCK(HttpStatus.UNPROCESSABLE_ENTITY, 4004, "쿠폰이 모두 소진되었습니다."),
    COUPON_ISSUE_FAILED(HttpStatus.CONFLICT, 4005, "쿠폰 발급에 실패했습니다."),

    // Infra (5xxxx)
    DISTRIBUTED_LOCK_ACQUISITION_FAILED(HttpStatus.CONFLICT, 50001, "분산 락 획득에 실패했습니다.")

}