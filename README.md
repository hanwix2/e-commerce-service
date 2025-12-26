## 개요
이커머스 서비스의 대규모 트래픽 상황을 가정하여 시스템 설계, 데이터 처리를 하기 위한 프로젝트.  
TDD 로 개발을 진행하였습니다.

<br>

## 주요 기능(API)
| 트래픽이 몰리는 상황에서 동시성 이슈 처리와 안정적인 조회 성능에 집중하기 위해 별도 고객, 상품 관리 그리고 인증 기능은 구현하지 않았습니다.

### 1. 포인트 조회
- 주문/결제에 필요한 사용자의 보유 포인트 조회 기능

### 2. 포인트 충전
- 주문/결제에 필요한 사용자의 포인트 충전 기능

### 3. 쿠폰 발급
- 할인을 받을 수 있는 쿠폰 발급 기능
- 쿠폰의 수량은 한정되어 있어 선착순으로 발급 받을 수 있습니다. (중복 발급 불가)

### 4. 상품 조회
- 특정 상품의 정보(재고, 가격 등) 조회 기능

### 5. 인기 상품 조회
- 최근 3일간 판매량이 높은 상위 5개 상품을 조회하는 기능

### 6. 주문/결제 
- 사용자가 보유한 포인트와 쿠폰(optional)으로 특정 상품을 주문/결제하는 기능
- 하나의 API 내에서 주문과 결제 모두 이뤄집니다.
- 상품 재고를 초과하여 주문이 진행되지 않습니다.

<br>

## Getting Started

### Prerequisites

#### Running Docker Containers

`local` profile 로 실행하기 위하여 인프라가 설정되어 있는 Docker 컨테이너를 실행해주어야 합니다.

```bash
docker-compose up -d
```

<br>

## 아키텍처

### 패키지 구조
레이어드(Layered) 아키텍처를 기반으로 도메인 중심으로 설계하려고 의도하였습니다.

<details>
<summary>패키지 구조</summary>
<div markdown="1">

```aiignore
.
├── application
├── domain
├── global
│   ├── config
│   └── exception
├── infrastructure
└── presentation
    ├── docs
    ├── request
    └── response
```

<br>

**패키지는 크게 presentation, application, domain, global 로 나뉘어져 있습니다.**
- **presentation**: API 요청과 응답을 처리하는 컨트롤러와 DTO(Request, Response)를 포함합니다. docs 패키지에는 API 문서(swagger)와 관련된 파일이 위치합니다.
- **application**: 비즈니스 로직은 최대한 도메인에 위치하도록 했고 각 도메인의 상호작용을 조율(orchestration)하는 서비스 클래스가 위치합니다.
- **domain**: 도메인 모델과 Entity 를 포함합니다. 도메인 모델은 비즈니스 로직을 포함하고 있습니다.
- **infrastructure**: 레포지토리 인터페이스를 포함합니다. 레포지토리는 데이터베이스(Persistence/Data Layer)와의 상호작용을 정의합니다.
- **global**: 전역 설정 및 예외 처리를 담당하는 클래스가 위치합니다.


> #### Layered Architecture 
> - Presentation Layer: 사용자 인터페이스와 상호작용하는 부분으로, API 요청과 응답을 처리합니다.
> - Application Layer: 비즈니스 로직을 처리하는 서비스 클래스가 위치합니다. 도메인 모델 간의 상호작용을 조율합니다.
> - Domain Layer: 도메인 모델과 엔티티, 레포지토리 인터페이스를 포함합니다. 비즈니스 로직을 도메인 모델에 집중시킵니다.
> - Infrastructure(persistence) Layer: 데이터베이스와의 상호작용을 처리하는 부분으로, 도메인 레이어에 포함되어 있습니다. 레포지토리 구현체가 위치합니다.

<br>

#### 이와 같은 구조를 적용한 이유
1. Layered 아키텍처는 보다 익숙하고 빠르게 적용 가능하여 추후 대용량 트래픽, 동시성 이슈 문제 처리와 같은 복잡한 문제를 해결하는 데 집중할 수 있도록 하기 위함입니다.
2. 도메인 중심 설계(DDD)를 적용하려고 노력했습니다. 비즈니스 로직을 도메인 모델에 집중시킴으로써 객체지향적으로 코드를 작성해보려 했습니다.

</div>
</details>

### 시스템 아키텍처 다이어그램
<p align="center">
<img width="641" height="424" alt="E-Commerce Server Architecture drawio" src="https://github.com/user-attachments/assets/38abac4c-f909-461d-9555-4fd017914776" />
</p>

<br>

## Troubleshooting [[wiki](https://github.com/hanwix2/e-commerce-service/wiki)]
1. [동시성 문제 - DB 를 활용한 해결 방안](https://github.com/hanwix2/e-comerce-service/wiki/%EB%8F%99%EC%8B%9C%EC%84%B1-%EB%AC%B8%EC%A0%9C-%E2%80%90-DB-%EB%A5%BC-%ED%99%9C%EC%9A%A9%ED%95%9C-%ED%95%B4%EA%B2%B0-%EB%B0%A9%EC%95%88)
1. [성능 저하 예상 쿼리 분석](https://github.com/hanwix2/e-comerce-service/wiki/%EC%84%B1%EB%8A%A5-%EC%A0%80%ED%95%98-%EC%98%88%EC%83%81-%EC%BF%BC%EB%A6%AC-%EB%B6%84%EC%84%9D)
1. [Redis 캐싱 적용 전 후 비교 분석 보고서](https://github.com/hanwix2/e-commerce-service/wiki/Redis-%EC%BA%90%EC%8B%B1-%EC%A0%81%EC%9A%A9-%EC%A0%84-%ED%9B%84-%EB%B9%84%EA%B5%90-%EB%B6%84%EC%84%9D-%EB%B3%B4%EA%B3%A0%EC%84%9C)
1. [Redis 기반 랭킹 시스템 설계 (인기 상품 조회)](https://github.com/hanwix2/e-commerce-service/wiki/Redis-%EA%B8%B0%EB%B0%98-%EB%9E%AD%ED%82%B9-%EC%8B%9C%EC%8A%A4%ED%85%9C-%EC%84%A4%EA%B3%84-(%EC%9D%B8%EA%B8%B0-%EC%83%81%ED%92%88-%EC%A1%B0%ED%9A%8C))
1. [Redis 기반 비동기 시스템 설계 (선착순 쿠폰 발급 기능)](https://github.com/hanwix2/e-commerce-service/wiki/Redis-%EA%B8%B0%EB%B0%98-%EB%B9%84%EB%8F%99%EA%B8%B0-%EC%8B%9C%EC%8A%A4%ED%85%9C-%EC%84%A4%EA%B3%84-(%EC%84%A0%EC%B0%A9%EC%88%9C-%EC%BF%A0%ED%8F%B0-%EB%B0%9C%EA%B8%89-%EA%B8%B0%EB%8A%A5))
1. [분산 환경에서의 트랜잭션 처리방안](https://github.com/hanwix2/e-commerce-service/wiki/%EB%B6%84%EC%82%B0-%ED%99%98%EA%B2%BD%EC%97%90%EC%84%9C%EC%9D%98-%ED%8A%B8%EB%9E%9C%EC%9E%AD%EC%85%98-%EC%B2%98%EB%A6%AC%EB%B0%A9%EC%95%88)
1. [Kafka 개념 학습 문서](https://github.com/hanwix2/e-commerce-service/wiki/Kafka-%EA%B0%9C%EB%85%90-%ED%95%99%EC%8A%B5-%EB%AC%B8%EC%84%9C)
1. [Kafka 를 이용한 선착순 쿠폰 발급 개선](https://github.com/hanwix2/e-commerce-service/wiki/Kafka-%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%9C-%EC%84%A0%EC%B0%A9%EC%88%9C-%EC%BF%A0%ED%8F%B0-%EB%B0%9C%EA%B8%89-%EA%B0%9C%EC%84%A0)
1. [부하테스트 및 개선 방안](https://github.com/hanwix2/e-commerce-service/wiki/%EB%B6%80%ED%95%98%ED%85%8C%EC%8A%A4%ED%8A%B8-%EB%B0%8F-%EA%B0%9C%EC%84%A0-%EB%B0%A9%EC%95%88)
1. [가상 장애 대응 시나리오 문서](https://github.com/hanwix2/e-commerce-service/wiki/%EC%9E%A5%EC%95%A0-%EB%8C%80%EC%9D%91-%EC%8B%9C%EB%82%98%EB%A6%AC%EC%98%A4-%EB%AC%B8%EC%84%9C)
