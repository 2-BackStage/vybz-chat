# VYBZ Chat Service

VYBZ 플랫폼의 실시간 채팅을 담당하는 마이크로서비스입니다.

## 📋 목차

-   [개요](#개요)
-   [기술 스택](#기술-스택)
-   [주요 기능](#주요-기능)
-   [프로젝트 구조](#프로젝트-구조)
-   [API 문서](#api-문서)
-   [설치 및 실행](#설치-및-실행)
-   [환경 설정](#환경-설정)
-   [채팅 시스템](#채팅-시스템)
-   [라이브 채팅 시스템](#라이브-채팅-시스템)
-   [이벤트 처리](#이벤트-처리)

## 🎯 개요

VYBZ Chat Service는 다음과 같은 기능을 제공합니다:

-   **1:1 채팅**: 사용자 간 개인 메시지 송수신
-   **채팅방 관리**: 채팅방 생성, 참여, 퇴장 관리
-   **메시지 처리**: 채팅 메시지 송수신 및 저장
-   **실시간 알림**: SSE(Server-Sent Events)를 통한 실시간 메시지 알림
-   **이벤트 처리**: Kafka를 통한 채팅 이벤트 발행
-   **데이터 저장**: MySQL과 MongoDB를 통한 메시지 및 사용자 데이터 저장
-   **반응형 프로그래밍**: WebFlux를 통한 비동기 비차단 처리

## 🛠 기술 스택

### Backend

![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![JPA](https://img.shields.io/badge/JPA-59666C?style=for-the-badge)
![Spring WebFlux](https://img.shields.io/badge/Spring_WebFlux-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)

### Infra

![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)
![Amazon EC2](https://img.shields.io/badge/Amazon_EC2-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white)
![Nginx](https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

### 협업

![Discord](https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white)
![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)

### Database & Cache

-   **MongoDB**: 실시간 채팅 메시지 저장 (Change Stream 활용)
-   **Redis**: 실시간 세션 관리 및 캐싱

### Message Queue

-   **Apache Kafka**: 비동기 이벤트 처리

### Documentation

-   **Swagger/OpenAPI 3.0**: API 문서화

### Build & Deploy

-   **Gradle**: 빌드 도구
-   **Docker**: 컨테이너화

## 🚀 주요 기능

### 1. 1:1 채팅 시스템

-   **SSE 연결**: Server-Sent Events를 통한 실시간 메시지 수신
-   **채팅방 생성**: 자동 채팅방 생성 및 참여자 관리
-   **메시지 송수신**: 실시간 메시지 전송 및 수신
-   **읽음 처리**: 메시지 읽음 상태 관리
-   **메시지 히스토리**: 과거 메시지 조회 (커서 기반 페이징)
-   **MongoDB Change Stream**: 실시간 메시지 변경 감지

### 2. WebFlux 기반 반응형 시스템

-   **비동기 처리**: WebFlux를 통한 비차단 I/O 처리
-   **반응형 스트림**: Reactor를 활용한 메시지 스트리밍
-   **백프레셔 처리**: 메시지 흐름 제어 및 백프레셔 관리
-   **Sink 기반 메시징**: Reactor Sink를 읽음 처리 메시지 전달

### 3. 채팅방 관리

-   **채팅방 생성**: 자동 채팅방 생성
-   **참여자 관리**: 채팅방 참여자 목록 관리
-   **숨김 처리**: 채팅방 숨김/재참여 기능
-   **마지막 메시지**: 채팅방별 마지막 메시지 정보

### 4. 메시지 처리

-   **메시지 타입**: 텍스트, 이미지, 비디오, 시스템 메시지 지원
-   **메시지 검증**: 부적절한 메시지 필터링
-   **읽지 않은 메시지**: 읽지 않은 메시지 수 관리
-   **메시지 저장**: MongoDB에 채팅 메시지 저장

### 5. 실시간 알림

-   **새 메시지 알림**: 실시간 새 메시지 알림
-   **읽음 상태**: 메시지 읽음 상태 실시간 업데이트
-   **시스템 메시지**: 시스템 공지사항 전송

## 📁 프로젝트 구조

```
src/main/java/back/vybz/chat_service/
├── common/                    # 공통 모듈
│   ├── config/               # 설정 클래스들
│   │   ├── WebSocketConfig.java
│   │   ├── ChatKafkaConfig.java
│   │   ├── LiveChatKafkaConfig.java
│   │   ├── CommonKafkaConfig.java
│   │   ├── MongoConfig.java
│   │   ├── ReactiveRedisConfig.java
│   │   ├── SwaggerConfig.java
│   │   └── WebMvcAsyncConfig.java
│   ├── entity/               # 공통 엔티티
│   │   ├── BaseResponseEntity.java
│   │   └── BaseResponseStatus.java
│   ├── exception/            # 예외 처리
│   │   ├── BaseException.java
│   │   ├── BaseExceptionHandler.java
│   │   ├── BaseExceptionHandlerFilter.java
│   │   └── AsyncExceptionHandler.java
│   └── util/                 # 유틸리티
│       ├── ChatSinkManager.java
│       ├── ChatMessageChangeFilter.java
│       ├── ChatMessageChangeStreamListener.java
│       ├── RedisUtil.java
│       ├── CursorPageUtil.java
│       └── MongoCursorHelper.java
├── kafka/                    # Kafka 이벤트 처리
│   ├── config/               # Kafka 설정
│   │   ├── ChatKafkaConfig.java
│   │   └── CommonKafkaConfig.java
│   ├── event/                # 이벤트 모델
│   │   └── ChatEvent.java
│   └── producer/             # 이벤트 프로듀서
│       └── ChatKafkaProducer.java
├── chat/                     # 1:1 채팅 도메인
│   ├── application/          # 채팅 서비스 로직
│   │   ├── ChatMessageService.java
│   │   ├── ChatMessageServiceImpl.java
│   │   ├── ChatRoomService.java
│   │   ├── ChatRoomServiceImpl.java
│   │   ├── ParticipantManager.java
│   │   └── ParticipantManagerImpl.java
│   ├── domain/               # 채팅 도메인 모델
│   │   ├── ChatRoom.java
│   │   ├── ChatMessage.java
│   │   ├── Participant.java
│   │   ├── LastMessage.java
│   │   ├── MessageType.java
│   │   └── SenderType.java
│   ├── dto/                  # 채팅 DTO
│   │   ├── request/
│   │   │   ├── RequestCreateChatRoomDto.java
│   │   │   ├── RequestEnterChatRoomDto.java
│   │   │   ├── RequestLeaveChatRoomDto.java
│   │   │   └── RequestSendMessageDto.java
│   │   └── response/
│   │       ├── ResponseChatMessageDto.java
│   │       └── ResponseChatRoomDto.java
│   ├── infrastructure/       # 채팅 리포지토리
│   │   ├── ChatMessageReactiveRepository.java
│   │   ├── ChatMessageReactiveRepositoryCustom.java
│   │   ├── ChatMessageReactiveRepositoryCustomImpl.java
│   │   ├── ChatRoomRepository.java
│   │   ├── ChatRoomRepositoryCustom.java
│   │   └── ChatRoomRepositoryCustomImpl.java
│   ├── presentation/         # 채팅 컨트롤러
│   │   ├── ChatMessageController.java
│   │   └── ChatRoomController.java
│   └── vo/                   # 채팅 VO
│       ├── request/
│       │   ├── RequestCreateChatRoomVo.java
│       │   ├── RequestEnterChatRoomVo.java
│       │   ├── RequestLeaveChatRoomVo.java
│       │   └── RequestSendMessageVo.java
│       └── response/
│           ├── ResponseChatMessageVo.java
│           └── ResponseChatRoomVo.java

```

## 📚 API 문서

Swagger UI를 통해 API 문서를 확인할 수 있습니다:

-   **URL**: `http://localhost:8000/chat-service/swagger-ui.html`
-   **API 그룹**: CHAT-SERVICE

### 주요 API 엔드포인트

#### 1:1 채팅 API

-   `POST /api/v1/chat-room` - 채팅방 생성/재참여
-   `GET /api/v1/chat-room/search` - 사용자 UUID로 채팅방 조회
-   `DELETE /api/v1/chat-room/leave` - 채팅방 나가기
-   `POST /api/v1/chat-message` - 메시지 전송
-   `GET /api/v1/chat-message/subscribe` - 실시간 채팅방 메시지 구독 (SSE)
-   `GET /api/v1/chat-message/previous` - 이전 메시지 조회
-   `POST /api/v1/chat-message/read` - 읽지 않은 메시지를 읽음으로 표시
-   `POST /api/v1/chat-message/reset-unread` - 읽지 않은 메시지 수 초기화

### API 요청/응답 예시

#### 채팅방 생성 요청

```json
{
    "participantUuid": "user-uuid-1",
    "receiverUuid": "user-uuid-2"
}
```

#### 메시지 전송 요청

```json
{
    "chatRoomId": "chat-room-id",
    "senderUuid": "user-uuid-1",
    "receiverUuid": "user-uuid-2",
    "messageType": "TEXT",
    "content": "안녕하세요!"
}
```

## 🚀 설치 및 실행

### 1. 사전 요구사항

-   Java 17
-   Gradle 8.4+
-   Docker (선택사항)
-   MongoDB 6.0+
-   Redis 6.0+
-   Kafka 3.0+

### 2. 로컬 실행

```bash
# 프로젝트 클론
git clone <repository-url>
cd vybz-chat

# Gradle 빌드
./gradlew clean build

# 애플리케이션 실행
./gradlew bootRun
```

### 3. Docker 실행

```bash
# Docker 이미지 빌드
docker build -t vybz-chat .

# Docker 컨테이너 실행
docker run -p 8000:8000 vybz-chat
```

## ⚙️ 환경 설정

### 주요 설정 파일

-   `application.yml`: 기본 설정

### 환경 변수

```yaml
# 데이터베이스 설정
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

# MongoDB 설정
spring:
  data:
    mongodb:
      uri: mongodb://${MONGO_USERNAME}:${MONGO_PASSWORD}@${MONGO_HOST}:${MONGO_PORT}/${MONGO_DATABASE}

# Redis 설정
spring:
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}

# Kafka 설정
spring:
  kafka:
    bootstrap-servers: ${KAFKA_SERVERS}
```

## 💬 채팅 시스템

### 1:1 채팅 시스템

#### SSE 연결

```javascript
// SSE 연결 예시
@RestController
public class SseClientController {

    @GetMapping("/sse-test")
    public Flux<String> subscribeToChatMessages() {
        WebClient client = WebClient.create("http://localhost:8080");
        return client.get()
            .uri("/api/v1/chat-message/subscribe?chatRoomId=room-id&participantUuid=user-uuid")
            .retrieve()
            .bodyToFlux(String.class);
    }
}
```

#### 메시지 타입

-   **TEXT**: 텍스트 메시지
-   **IMAGE**: 이미지 메시지
-   **VIDEO**: 비디오 메시지
-   **SYSTEM**: 시스템 메시지
-   **LEFT**: 참여자 나감 메시지

#### MongoDB Change Stream

-   **실시간 감지**: MongoDB Change Stream을 통한 실시간 메시지 변경 감지
-   **필터링**: insert/update 이벤트만 처리
-   **읽음 처리**: 읽음 상태 변경 시 실시간 알림

### WebFlux 기반 반응형 시스템

#### Reactor 스트림 처리

```java
// WebFlux를 통한 비동기 메시지 처리 예시
public Flux<ResponseChatMessageDto> subscribeChatMessageByChatRoomId(String chatRoomId, String participantUuid) {
    // 참가자 등록
    participantManager.registerParticipant(chatRoomId, participantUuid);

    // 메시지 수신용 Flux 생성 (Sink 기반)
    Flux<ResponseChatMessageDto> messageFlux = chatSinkManager.getOrCreateSink(chatRoomId).asFlux();

    // 핑 전송용 Flux 생성
    Flux<ResponseChatMessageDto> pingFlux = makePingFlux(chatRoomId);

    // SSE 스트림 반환
    return Flux.merge(messageFlux, pingFlux)
            .doOnSubscribe(sub -> log.info("SSE 구독 시작: chatRoomId={}, participantUuid={}", chatRoomId, participantUuid))
            .doFinally(signalType -> {
                log.info("SSE 종료 감지: {}, chatRoomId={}, participantUuid={}", signalType, chatRoomId, participantUuid);
                participantManager.unregisterParticipant(chatRoomId, participantUuid);
            });
}
```

#### Sink 기반 메시징

-   **Sink 생성**: 채팅방별 Reactor Sink 생성
-   **백프레셔 처리**: 메시지 흐름 제어
-   **자동 정리**: 참가자가 없을 때 Sink 자동 정리
-   **메모리 효율성**: 효율적인 메시지 전달

### 채팅방 관리

#### 채팅방 상태

-   **참여자 관리**: 채팅방 참여자 목록 및 상태 관리
-   **숨김 처리**: 채팅방 숨김/재참여 기능
-   **읽지 않은 메시지**: 참여자별 읽지 않은 메시지 수 관리
-   **마지막 메시지**: 채팅방별 마지막 메시지 정보

#### 참여자 권한

-   **일반 사용자**: 메시지 송수신, 채팅방 나가기
-   **시스템**: 시스템 메시지 전송

### 메시지 처리

#### 메시지 저장

-   **MySQL**: 채팅방 정보, 사용자 정보
-   **MongoDB**: 실시간 채팅 메시지 (Change Stream 활용)
-   **Redis**: 실시간 세션 및 캐시

#### 메시지 검증

-   **길이 제한**: 메시지 길이 제한
-   **금지어 필터링**: 부적절한 단어 필터링
-   **스팸 방지**: 과도한 메시지 전송 방지

## 📡 이벤트 처리

### Kafka 이벤트

#### 발행 이벤트

-   **ChatEvent**: 1:1 채팅 이벤트
    -   `chatRoomId`: 채팅방 ID
    -   `senderUuid`: 발신자 UUID
    -   `receiverUuid`: 수신자 UUID

### 이벤트 프로듀서

-   `ChatKafkaProducer`: 1:1 채팅 이벤트 발행

### Kafka 토픽

-   `chat-message`: 1:1 채팅 이벤트 토픽

### 이벤트 발행 시점

-   **메시지 전송**: 사용자가 메시지 전송 시

## 🏗 아키텍처

### 도메인 주도 설계 (DDD)

-   **Domain Layer**: 채팅 도메인 모델과 비즈니스 로직
-   **Application Layer**: 채팅 서비스 로직과 유스케이스
-   **Infrastructure Layer**: 데이터베이스 접근과 외부 시스템 연동
-   **Presentation Layer**: REST API 및 WebSocket 엔드포인트

### 마이크로서비스 패턴

-   **Service Discovery**: Eureka Client를 통한 서비스 등록
-   **Event-Driven**: Kafka를 통한 비동기 이벤트 처리
-   **Stateless**: 상태 없는 서비스 설계

### 데이터베이스 설계

-   **MySQL**: 채팅방 정보, 사용자 정보, 시스템 데이터
-   **MongoDB**: 실시간 채팅 메시지 저장 (Change Stream 활용)
-   **Redis**: 실시간 세션 관리 및 캐싱

### WebFlux 아키텍처

-   **비동기 처리**: WebFlux를 통한 비차단 I/O 처리
-   **반응형 스트림**: Reactor를 활용한 메시지 스트리밍
-   **백프레셔 처리**: 메시지 흐름 제어 및 백프레셔 관리
-   **Sink 기반 메시징**: Reactor Sink를 통한 효율적인 메시지 전달

### SSE 아키텍처

-   **Sink 기반**: Reactor Sink를 통한 메시지 스트리밍
-   **참가자 관리**: 채팅방별 참가자 등록/해제
-   **핑 메시지**: 연결 상태 유지를 위한 핑 메시지
-   **자동 정리**: 참가자가 없을 때 Sink 자동 정리

## 🔧 개발 가이드

### 코드 컨벤션

-   **패키지 구조**: 도메인별 계층 분리
-   **네이밍**: 명확하고 일관된 네이밍 규칙
-   **예외 처리**: BaseException을 통한 통일된 예외 처리
-   **로깅**: Slf4j를 통한 구조화된 로깅

### 테스트

```bash
# 단위 테스트 실행
./gradlew test

# 통합 테스트 실행
./gradlew integrationTest

# WebFlux 테스트
./gradlew test --tests "*WebFluxTest*"
```

### WebFlux 테스트

```java
// WebFlux SSE 연결 테스트
@Test
public void testSSESubscription() {
    WebTestClient webTestClient = WebTestClient.bindToServer()
            .baseUrl("http://localhost:8000")
            .build();

    webTestClient.get()
            .uri("/api/v1/chat-message/subscribe?chatRoomId=test-room&participantUuid=test-user")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM);
}
```

### 성능 최적화

#### 데이터베이스 최적화

-   **인덱싱**: 자주 조회되는 필드에 인덱스 설정
-   **배치 처리**: 대량 데이터 처리 시 배치 사용
-   **캐싱**: Redis를 통한 자주 조회되는 데이터 캐싱

#### WebFlux 최적화

-   **비동기 처리**: 비차단 I/O를 통한 성능 향상
-   **백프레셔 제어**: 메시지 흐름 제어 및 메모리 효율성
-   **Sink 관리**: 효율적인 메시지 전달 및 자동 정리

#### SSE 최적화

-   **Sink 관리**: 메모리 효율적인 Sink 관리
-   **백프레셔**: 메시지 백프레셔 처리
-   **자동 정리**: 사용하지 않는 Sink 자동 정리

#### Kafka 최적화

-   **배치 전송**: 메시지 배치 처리
-   **파티션 관리**: 토픽 파티션 최적화
-   **컨슈머 그룹**: 컨슈머 그룹 설정

## 📊 모니터링

### 로깅

-   **애플리케이션 로그**: Spring Boot 로깅
-   **WebSocket 로그**: 연결 및 메시지 로깅
-   **SSE 로그**: SSE 연결 및 메시지 로깅
-   **데이터베이스 로그**: 쿼리 성능 로깅

### 메트릭

-   **연결 수**: 활성 SSE 연결 수
-   **메시지 처리량**: 초당 처리 메시지 수
-   **응답 시간**: API 응답 시간
-   **에러율**: 에러 발생률

### 알림

-   **연결 실패**: SSE 연결 실패 알림
-   **데이터베이스 오류**: DB 연결 오류 알림
-   **Kafka 오류**: 메시지 전송 실패 알림

## 🚨 트러블슈팅

### 일반적인 문제

#### SSE 연결 실패

```bash
# 포트 확인
netstat -an | grep 8000

# 방화벽 설정 확인
sudo ufw status
```

#### SSE 연결 실패

```bash
# 애플리케이션 로그 확인
tail -f logs/application.log

# Redis 연결 확인
redis-cli -h <탄력적 IP> -p 63379 -a vybz1234 ping
```

#### 데이터베이스 연결 오류

```bash
# MySQL 연결 확인
mysql -h <탄력적 IP> -P 33306 -u vybz -p

# MongoDB 연결 확인
mongo mongodb://vybz:<비밀번호>@<탄력적 IP>:27020/vybz?authSource=admin
```

#### Kafka 연결 오류

```bash
# Kafka 브로커 상태 확인
kafka-topics.sh --bootstrap-server <탄력적 IP>:10000 --list
```

### 로그 확인

```bash
# 애플리케이션 로그 확인
tail -f logs/application.log

# 에러 로그 확인
grep "ERROR" logs/application.log

# WebFlux 로그 확인
grep "WebFlux" logs/application.log
```

## 📝 라이선스

이 프로젝트는 VYBZ 팀의 내부 프로젝트입니다.

## 👥 팀

-   **개발팀**: VYBZ Backend Team

---

**VYBZ Chat Service** - WebFlux 기반 실시간 채팅 서비스
