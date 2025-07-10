# 🎵 VYBZ Chat Service

VYBZ 플랫폼의 실시간 채팅/메시지 관리 마이크로서비스
채팅방 생성/입장/퇴장, 메시지 송수신, 실시간 구독, 읽음/안읽음, 송신자 타입 등 다양한 채팅 도메인 관리와 확장성을 고려한 Spring Boot 기반 서비스입니다.

---

## 🛠 Tech Stack

| 구분            | 기술/버전                                      |
|----------------|-----------------------------------------------|
| Language       | Java 17                                       |
| Framework      | Spring Boot 3.4.5, Spring WebFlux (리액티브)   |
| Database       | MongoDB (Spring Data MongoDB, Reactive 지원), MySQL (JPA) |
| Service Discovery | Netflix Eureka Client                      |
| Build          | Gradle 8.4                                    |
| Test           | JUnit 5, Spring Boot Test                     |
| 기타           | Lombok, Swagger(OpenAPI 3.0, 어노테이션 기반), Layered Architecture     |

---

## 🏗️ 아키텍처 및 레이어 구조

- **Layered Architecture**: Presentation(Controller), Application(Service), Domain, DTO/VO, Infrastructure, Common 등 계층 분리
- **Domain-Driven Design (DDD)**: 채팅 도메인 모델 중심 설계
- **Reactive Programming**: WebFlux, MongoDB 리액티브 저장소, SSE 기반 실시간 메시지 구독
- **확장성**: MongoDB 기반 메시지/채팅방 관리, MySQL 연동 가능

### 📁 프로젝트 구조

```
vybz-chat/
├── build.gradle
├── settings.gradle
├── src/
│   └── main/
│       ├── java/
│       │   └── back/
│       │       └── vybz/
│       │           └── chat_service/
│       │               ├── chat/
│       │               │   ├── presentation/    # 💡 Controller(API)
│       │               │   ├── application/     # ⚙️ Service
│       │               │   ├── domain/          # 🧩 Domain Model
│       │               │   ├── vo/              # 🧾 VO (값 객체)
│       │               │   ├── dto/             # 📦 DTO
│       │               │   └── infrastructure/  # 🗄 Repository
│       │               ├── common/              # 🛠 공통 유틸, 예외, 설정
│       │               └── ChatServiceApplication.java
└── ...
```

---

## 💡 Presentation Layer (API)

### 주요 엔드포인트

#### [채팅방 API] `/api/v1/chat-room`

- **POST /** : 채팅방 생성/재입장  
  - Request: `RequestCreateChatRoomVo`
  - Response: `BaseResponseEntity<Void>`
- **GET /search** : 참여자 UUID로 채팅방 목록 조회 (커서 기반 페이지네이션)
  - Request: `participantUuid`, `sentAt`, `pageSize`
  - Response: `BaseResponseEntity<CursorPageUtil<ResponseChatRoomVo, Instant>>`
- **DELETE /leave** : 채팅방 퇴장
  - Request: `RequestLeaveChatRoomVo`
  - Response: `BaseResponseEntity<Void>`

#### [메시지 API] `/api/v1/chat-message`

- **POST /** : 메시지 전송
  - Request: `RequestSendMessageVo`
  - Response: `BaseResponseEntity<Void>`
- **GET /subscribe** : 채팅방 실시간 메시지 구독 (SSE, Flux)
  - Request: `chatRoomId`, `participantUuid`
  - Response: `Flux<ResponseChatMessageVo>`
- **GET /search** : 채팅방 이전 메시지 조회 (커서 기반 페이지네이션)
  - Request: `chatRoomId`, `participantUuid`, `sentAt`, `pageSize`
  - Response: `BaseResponseEntity<CursorPageUtil<ResponseChatMessageVo, Instant>>`

---

## ⚙️ Application Layer (Service)

- **ChatRoomService/Impl**: 채팅방 생성, 입장, 퇴장, 조회 등 비즈니스 로직
- **ChatMessageService/Impl**: 메시지 전송, 구독, 이전 메시지 조회, 시스템 메시지 등 비즈니스 로직

---

## 🧩 Domain Layer

- **ChatRooms**: 채팅방 정보, 참여자 UUID, 읽지 않은 메시지 수, 마지막 메시지, 생성/수정일시 등 관리
- **ChatMessages**: 메시지 본문, 송신자 ID/타입, 읽음 여부, 생성/수정일시 등 관리
- **LastMessage**: 채팅방의 마지막 메시지 정보(내용, 송신자, 타입, 전송시각)
- **SenderType**: 송신자 구분 (USER/사용자, BUSKER/버스커)

---

## 🧾 VO/DTO

- **VO**: API 요청/응답용 값 객체 (ex. `RequestSendMessageVo`, `ResponseChatRoomVo`)
- **DTO**: 계층 간 데이터 전달 객체 (ex. `RequestSendMessageDto`, `ResponseChatMessageDto`)

---

## 🗄 Infrastructure Layer

- **Repository**: MongoDB 기반 채팅방/메시지 저장소, 리액티브/커스텀 쿼리 지원

---

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Gradle 8.4+
- MongoDB

### Local Development

```bash
# 1. 프로젝트 클론
git clone <YOUR_REPO_URL>
cd vybz-chat

# 2. 빌드 및 실행 (로컬)
./gradlew clean build -x test
# java -jar build/libs/*.jar 또는 IDE 실행

# 3. API 문서 (Swagger 등)
# http://localhost:8080/swagger-ui/index.html (Swagger 어노테이션 기반, springdoc-openapi 필요)
```

---

## 🔧 주요 기능

- 채팅방 생성/입장/퇴장
- 메시지 송수신, 읽음/안읽음 관리
- 실시간 메시지 구독 (SSE, WebFlux)
- 송신자 타입(사용자/버스커) 구분
- 커서 기반 페이지네이션
- 도메인 모델 기반 확장성
- (추후) Kafka, 실시간 동기화, API 문서화 등 확장 가능

---

## 🧪 테스트

- Spring Boot Test, JUnit5 기반 기본 컨텍스트 로딩 테스트 포함
- `src/test/java/back/vybz/chat_service/ChatServiceApplicationTests.java`

---

## 📞 Contact

- Team: VYBZ Development Team
- Made with ❤️ by VYBZ Team 