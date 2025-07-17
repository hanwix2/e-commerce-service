# 시퀀스 다이어그램
- 클라이언트와 도메인 기반으로 다이어그램 작성
- 동시성 이슈 처리에 대한 내용은 비교적 간단한 DB 단에서의 Optimistic Lock 을 사용하는 것을 전제. 
    - 트래픽 규모에 따라 변경될 가능성 있음 (e.g. Redis, Message Queue 등)

## 포인트(잔액) 충전
```mermaid
sequenceDiagram
    actor Client as Client
    participant Point as Point
    participant PointHistory as PointHistory
    participant User as User

    Client ->>+ Point: 포인트 충전 요청
    Point ->>+ User: 유저 조회
    opt 유저 없음
        User -->> Client: ❌ - 유저 없음. 에러 반환
    end
        User -->>- Point: 유저 정보
        
    loop 포인트 증가 최대 5회까지 재시도 (동시성 이슈)
        Point ->>+ Point: 포인트 증가 (optimistic lock 적용)
            
        break 포인트 증가 성공 시
            Point -->- Point: 포인트 증가 성공
        end 
    end
    opt 포인트 충전 재시도 횟수 초과
        Point -->> Client: ❌ - 포인트 충전 재시도 횟수 초과. 에러 반환
    end

    Point ->>+ PointHistory: 포인트 충전 내역 기록
    PointHistory -->>- Point: 내역 저장 완료
    Point -->>- Client: ✅ - 충전 완료 응답(잔액 포함)
```

## 포인트(잔액) 조회
```mermaid
sequenceDiagram
    actor Client as Client
    participant Point as Point
    participant User as User

    Client ->>+ Point: 포인트(잔액) 조회 요청
    Point ->>+ User: 유저 조회
    opt 유저 없음
        User -->> Client: ❌ - 유저 없음. 에러 반환
    end
        User -->>- Point: 유저 정보
        Point -->>- Client: ✅ - 포인트(잔액) 조회 결과
```

## 상품 조회
```mermaid
sequenceDiagram
    actor Client as Client
    participant Product as Product

    Client ->>+ Product: 상품 정보 조회 요청
    opt 상품 정보 없음
        Product -->> Client: ❌ - 상품 정보 없음. 에러 반환
    end
    Product -->>- Client: ✅ - 상품 정보 (ID, 이름, 가격, 잔여수량) 응답
```

## 주문/결제
```mermaid
sequenceDiagram
    actor Client as Client
    participant Order as Order
    participant User as User
    participant UserPoint as UserPoint
    participant Product as Product
    participant Payment as Payment
    participant UserCoupon as UserCoupon
    participant ExternalDataPlatform as ExternalDataPlatform

    Client ->>+ Order: 주문/결제 요청
    Order ->>+ User: 유저 검증 요청
    opt 유저 없음
        User -->> Client: ❌ - 주문 실패(유저 없음)
    end

    User -->>- Order: 유저 정보

    loop 상품 재고 차감 최대 10회 재시도 (동시성 이슈)
        Order ->>+ Product: 상품 조회 요청
        opt 상품 없음
            Product -->> Client: ❌ - 주문 실패(상품 정보 없음)
        end
        opt 상품 재고 없음
            Product -->> Client: ❌ - 주문 실패(상품 재고 없음)
        end 
        Product -->>- Order: 상품 정보

        Order ->>+ UserCoupon: 쿠폰 정보 조회 요청
        opt 쿠폰 미존재
            UserCoupon -->> Client: ❌ - 주문 실패(쿠폰 미존재)
        end
        opt 쿠폰 사용 불가 상태
            UserCoupon -->> Client: ❌ - 주문 실패(쿠폰 사용 불가)
        end
            UserCoupon -->>- Order: 쿠폰 정보

        Order ->>+ UserPoint: 포인트 정보 조회 요청
        UserPoint -->>- Order: 포인트 정보

        Order ->>+ Order: 최종 금액 산정
        Order -->>- Order: 최종 금액

        opt 포인트 부족
            Order -->> Client: ❌ - 주문 실패(포인트 부족)
        end 

        Order ->>+ Product: 상품 재고 차감(optimistic lock)
        break 재고 차감 성공
            Product -->>- Order: 재고 차감 성공
        end
    end

    opt 재고 차감 시도 횟수 초과
        Order -->> Client: ❌ - 주문 실패(재고 차감 실패)
    end

    Order ->> Order: 주문 정보 저장
    
    Order ->>+ Payment: 결제 정보 생성
    Payment ->>- Order: 결제 정보 생성 완료

    Order ->>+ UserPoint: 포인트 차감 & 이력 저장
    UserPoint -->>- Order: 포인트 차감 완료

    Order ->>+ UserCoupon: 쿠폰 사용 이력 저장
    UserCoupon -->>- Order: 쿠폰 사용 이력 완료

    Order -) ExternalDataPlatform: 주문 정보 비동기 전달

    Order -->>- Client: ✅ - 주문 및 결제 완료 응답


```
> 구매할 상품을 미리 주문서에 넣어두는 API 와 결제를 별도로 처리하는 API 를 구분하여 동시성 이슈를 분산시키려 했으나 
> 시스템이 복잡해질 것을 우려해 하나의 API 동작(트랜잭션) 내에서 처리하는 것을 선택했습니다.  
> (대신 하나의 트랜잭션이 복잡해지는 결과 발생)

## 선착순 쿠폰 발급
```mermaid
sequenceDiagram
  actor Client as Client
  participant Coupon as Coupon
  participant UserCoupon as UserCoupon
  participant User as User

  Client ->>+ Coupon: 쿠폰 발급 요청
  Coupon ->>+ User: 유저 존재 확인
  opt 유저 없음
    User -->> Client: ❌ - 유저 없음. 에러 반환

  end
  User -->>- Coupon: 유저 정보 응답
  Coupon ->>+ Coupon: 쿠폰 정보 조회
  opt 쿠폰 없음
    Coupon -->> Client: ❌ - 쿠폰 정보 없음. 에러 응답
  end

  opt 쿠폰 발급 불가 상태
    Coupon -->> Client: ❌ - 발급 불가 상태. 에러 응답
  end
  Coupon -->>- Coupon: 쿠폰 정보

  loop 쿠폰 수량 차감 최대 10회 재시도 (동시성 이슈)
    opt 수량 부족
        Coupon -->> Client: ❌ - 수량 부족. 에러 응답
    end
    
    Coupon ->>+ Coupon: 쿠폰 잔여 수량 차감(optimistic lock 적용)

    break 쿠폰 수량 차감 성공 시
      Coupon -->>- Coupon: 쿠폰 잔여 수량 차감 성공
    end

  end

  opt 쿠폰 수량 차감 시도 횟수 초과
    Coupon -->> Client: ❌ - 쿠폰 발급 시도 횟수 초과. 에러 응답
  end 

  Coupon ->>+ UserCoupon: 쿠폰 발급 내역 기록
  UserCoupon -->>- Coupon: 내역 저장 완료
  Coupon -->>- Client: ✅ - 쿠폰 발급 완료
```

## 인기 판매 상품 조회
```mermaid
sequenceDiagram
    actor Client as Client
    participant OrderItem as OrderItem
    participant Product as Product
    participant Cache as Cache

    Client ->>+ OrderItem: 인기 판매 상품 정보 조회
    OrderItem ->>+ Cache: 인기 판매 상품 정보 조회
    alt 캐싱 데이터 존재
        Cache -->> OrderItem: 인기 판매 상품 정보 반환
    else 캐싱 데이터 없음
        Cache -->>- OrderItem: 캐싱 데이터 없음
        OrderItem ->>+ OrderItem: 최근 3일간 판매 수 상위 5개 상품 ID 조회
        OrderItem -->>- OrderItem: 상품 ID 조회
        OrderItem ->>+ Product: 상품 정보 조회(상품 ID 목록)
        Product -->>- OrderItem: 상품 정보 반환
        OrderItem ->>+ Cache: 인기 상품 정보 캐싱 요청
        Cache -->>- OrderItem: 캐시 저장 완료
    end
OrderItem -->>- Client: ✅ 인기 판매 상품 정보 반환
```

