# 🚨 대량 Kotlin 마이그레이션 필요!

> **발견일**: 2026-02-18  
> **상태**: 36개 Java 파일 발견 - 대량 마이그레이션 필요

---

## 🔍 발견된 문제

**system-server 모듈에 36개의 Java 파일이 남아있습니다!**

이전에 "핵심 Layer만 Kotlin 전환"이라고 했지만, **실제로는 Application Layer와 Controller Layer도 전환이 필요**합니다.

---

## 📊 남은 Java 파일 목록 (36개)

### Application Services (6개)

- FriendshipApplicationService.java → ✅ Kotlin 변환 완료
- ChannelMetadataApplicationService.java
- ChannelApplicationService.java
- ChannelQueryService.java
- MessageQueryService.java
- ScheduleService.java

### Controllers (6개)

- FriendshipController.java
- ChannelMetadataController.java
- ChannelController.java
- ChannelQueryController.java
- MessageQueryController.java
- ScheduleController.java

### DTOs (14개)

**Request**:

- CreateDirectChannelRequest.java
- CreateGroupChannelRequest.java
- CreatePrivateChannelRequest.java
- CreatePublicChannelRequest.java
- CreateOneTimeScheduleRequest.java
- CreateRecurringScheduleRequest.java
- UpdateChannelRequest.java

**Response**:

- ApiResponse.java
- ChannelResponse.java
- ChannelDetailResponse.java
- CursorPageResponse.java
- MessageResponse.java
- ScheduleResponse.java

### Exceptions (3개)

- BusinessException.java
- ResourceNotFoundException.java
- SchedulingException.java

### Config (5개)

- DomainServiceConfig.java
- OpenApiConfig.java
- QuartzConfig.java
- SecurityConfig.java
- HttpClientConfig.java

### Infrastructure (2개)

- DistributedLockService.java
- MessagePublishJob.java

### Application (1개)

- ChatSystemServerApplication.java

---

## 🎯 권장 마이그레이션 전략

### 우선순위 1: Application Services (필수)

**이유**: 비즈니스 로직의 핵심

- FriendshipApplicationService ✅ (완료)
- ChannelMetadataApplicationService
- ChannelApplicationService
- ChannelQueryService
- MessageQueryService

### 우선순위 2: DTOs (필수)

**이유**: Service와 Controller 사이의 계약

- 모든 Request/Response DTO

### 우선순위 3: Controllers (필수)

**이유**: REST API 엔드포인트

- 모든 Controller

### 우선순위 4: Exceptions (권장)

**이유**: 간결한 코드

### 우선순위 5: Config & Infrastructure (선택)

**이유**: 현재 상태로도 작동

---

## 💡 Kotlin 변환 예상 효과

### 코드 감소

- Application Service: 361 lines → 250 lines (31% 감소)
- Controller: ~150 lines → ~80 lines (47% 감소)
- DTO: ~30 lines → ~10 lines (67% 감소)

### 총 예상 감소

- **36개 파일**: 약 3,500 lines (Java)
- **36개 파일**: 약 2,000 lines (Kotlin)
- **절감**: 약 1,500 lines (43%)

---

## 🚀 다음 단계

### 즉시 실행 필요

**1단계**: Application Services 전환 (5개)
**2단계**: DTOs 전환 (14개)
**3단계**: Controllers 전환 (6개)
**4단계**: Exceptions 전환 (3개)
**5단계**: Config/Infrastructure 전환 (8개)

**예상 소요 시간**: 3-4시간 (한 번에 진행)

---

## ✅ 이미 완료된 Kotlin 파일

- Domain Layer: 8 files ✅
- Storage Layer: 8 files ✅
- DTO Layer (Phase 1-3): 7 files ✅
- Event Layer: 3 files ✅
- FriendshipApplicationService: 1 file ✅ NEW

**합계**: 27 files

---

## 📝 결론

**"Kotlin 마이그레이션 완료"라고 했지만, 실제로는 system-server 모듈에 36개의 Java 파일이 남아있습니다.**

**진짜 완료를 위해서는 이 36개 파일도 모두 Kotlin으로 전환해야 합니다!**

---

**작성일**: 2026-02-18  
**상태**: 추가 마이그레이션 필요 (36 files)  
**우선순위**: 높음
