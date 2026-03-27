# Rule 11 — Java Coding Standards

**베이스라인**: `java-best-practices` 스킬 (VirtusLab, Java 21+)

Java 코드 작성·리뷰 시 `java-best-practices` 스킬을 기준으로 따른다.
아래는 프로젝트 특화 **오버라이드 및 확장** 규칙이다.

---

## 프로젝트 오버라이드 (java-best-practices와 충돌 시 이 규칙 우선)

### Lombok 사용 정책
`java-best-practices`는 Lombok을 `@Builder`만 허용하지만, **이 프로젝트에서는**:

| 대상 | 허용 어노테이션 | 이유 |
|------|----------------|------|
| JPA `@Entity` 클래스 | `@Getter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` | JPA는 no-arg constructor 필요 — record 불가 |
| `api/request/`, `api/response/` | **record 사용** (`@Getter` 금지) | java-best-practices 일치 |
| Application DTO | record 사용 | java-best-practices 일치 |
| `@Data` | **금지** (JPA 엔티티 포함) | equals/hashCode 오작동 위험 |

```java
// ✅ JPA Entity — Lombok 허용
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ScheduledMessage { ... }

// ✅ Response DTO — record 사용
public record ScheduledMessageResponse(Long id, String content, LocalDateTime scheduledAt) {
    public static ScheduledMessageResponse from(ScheduledMessage m) {
        return new ScheduledMessageResponse(m.getId(), m.getContent(), m.getScheduledAt());
    }
}
```

### 테스트 네이밍
`java-best-practices`는 `methodName_shouldExpected_whenCondition` 권장하지만,
**이 프로젝트 테스트는 `@DisplayName` 한글 필수**:

```java
// ✅ 프로젝트 표준
@Nested
@DisplayName("createSchedule 메서드는")
class CreateSchedule {
    @Test
    @DisplayName("예약 시간이 현재보다 미래이면 예약을 생성한다")
    void success() { ... }
}

// ❌ 금지 — 영어 DisplayName (java-best-practices 스타일은 적용 안 함)
@DisplayName("should create schedule when scheduled time is future")
```

### Virtual Threads
프로젝트에서 이미 활성화됨 (`spring.threads.virtual.enabled=true`).
별도 `Executors.newVirtualThreadPerTaskExecutor()` 설정 불필요.
`ThreadLocal` 패턴은 피할 것 (Virtual Thread와 충돌 가능).

---

## java-best-practices 주요 규칙 요약 (프로젝트 적용 기준)

| 규칙 | 상태 |
|------|------|
| `record` for immutable data | ✅ 적용 (api/request, api/response, event/model) |
| `Optional` — null 반환 금지 | ✅ 적용 |
| Switch expressions (pattern matching) | ✅ 적용 |
| `var` for obvious local types | ✅ 적용 |
| `List.of()` / `Map.of()` immutable collections | ✅ 적용 |
| SLF4J parameterized logging | ✅ 적용 |
| `java.time` (no Date/Calendar) | ✅ 적용 |
| Sealed classes for domain ADT | ⚠️ 선택적 (복잡한 도메인 상태 모델링 시) |
| Structured Concurrency (Java 25) | ❌ 미적용 (Java 21 프로젝트) |
| RxJava 금지 | ✅ 적용 |

---

## 리뷰 체크리스트

코드 리뷰 시 아래 항목 확인:

```
□ JPA 엔티티에 @Data 없는가?
□ api/ 레이어가 record + factory method 패턴 사용하는가?
□ null 반환하는 메서드 없는가? (Optional or throw)
□ System.out.println() 없는가? (SLF4J 사용)
□ ThreadLocal 새로 추가하지 않았는가?
□ @DisplayName이 한글인가?
□ 도메인 객체를 mock하지 않았는가?
```

---

## 스킬 호출 기준

- Java 21+ 최신 문법/패턴 상세 가이드 → `/java-best-practices` 스킬
- TDD 사이클 & 테스트 패턴 → `/tdd-cycle` 스킬
- JPA 엔티티 작성 → `/jpa` 스킬
