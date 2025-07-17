package kr.hhplus.be.server.common.exception

enum class ResponseStatus(
    val code: Int,
    val message: String,
) {

    OK(200, "요청이 성공적으로 처리되었습니다."),

    // User (1xxx)
    USER_NOT_FOUND(1001, "사용자를 찾을 수 없습니다."),
    POINT_CHARGE_FAILED(1002, "포인트 충전이 실패했습니다."),
    POINT_NOT_ENOUGH(1003, "포인트가 부족합니다."),

    // Product (2xxx)
    PRODUCT_NOT_FOUND(2001, "존재하지 않는 상품입니다."),
    PRODUCT_OUT_OF_STOCK(2002, "상품의 재고가 부족합니다."),


    // Order (3xxx)
    ORDER_FAILED(3001, "주문이 실패했습니다."),

    // Coupon (4xxx)
    COUPON_NOT_FOUND(4001, "쿠폰을 찾을 수 없습니다."),
    INVALID_COUPON(4002, "쿠폰이 유효하지 않습니다."),
    COUPON_EXPIRED(4003, "쿠폰이 만료되었습니다."),
    COUPON_OUT_OF_STOCK(4004, "쿠폰이 모두 소진되었습니다."),
    COUPON_ISSUE_FAILED(4005, "쿠폰 발급에 실패했습니다.")

}