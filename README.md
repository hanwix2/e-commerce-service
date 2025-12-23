## 개요
이커머스 서비스에서 대용량 트래픽 상황을 가정하여 설계 및 데이터 처리를 하기 위한 프로젝트  
TDD 로 개발을 진행하였습니다.

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


> ### Layered Architecture 
> - Presentation Layer: 사용자 인터페이스와 상호작용하는 부분으로, API 요청과 응답을 처리합니다.
> - Application Layer: 비즈니스 로직을 처리하는 서비스 클래스가 위치합니다. 도메인 모델 간의 상호작용을 조율합니다.
> - Domain Layer: 도메인 모델과 엔티티, 레포지토리 인터페이스를 포함합니다. 비즈니스 로직을 도메인 모델에 집중시킵니다.
> - Infrastructure(persistence) Layer: 데이터베이스와의 상호작용을 처리하는 부분으로, 도메인 레이어에 포함되어 있습니다. 레포지토리 구현체가 위치합니다.

<br>

### 이와 같은 구조를 적용한 이유
1. Layered 아키텍처는 보다 익숙하고 빠르게 적용 가능하여 추후 대용량 트래픽, 동시성 이슈 문제 처리와 같은 복잡한 문제를 해결하는 데 집중할 수 있도록 하기 위함입니다.
2. 도메인 중심 설계(DDD)를 적용하려고 노력했습니다. 비즈니스 로직을 도메인 모델에 집중시킴으로써 객체지향적으로 코드를 작성해보려 했습니다.

</div>
</details>
