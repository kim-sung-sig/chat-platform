# Rule 10 — Project Conventions

**Authoritative source**: `docs/conventions/CONVENTIONS.md`

모든 코드 작성·검토 시 아래 규칙을 반드시 준수한다.
세부 내용은 CONVENTIONS.md를 참조한다.

---

## 핵심 5대 규칙 (즉시 적용)

### 1. DDD 레이어 의존 방향
```
api/ → application/ → domain/
infrastructure/ → application/ → domain/
```
역방향 의존 **절대 금지**. 위반 시 즉시 지적하고 수정안 제시.

### 2. domain/service vs application/service 경계
| 레이어 | 허용 | 금지 |
|--------|------|------|
| `domain/service/` | 순수 비즈니스 로직, domain model 조작 | Kafka, Redis, HTTP, Repository 직접 호출 |
| `application/service/` | Repository, Kafka, Redis, @Transactional | 여러 bounded context 간 직접 DB 접근 |

→ 아키텍처 판단이 필요하면 `/arch-policy` 스킬 호출.

### 3. CQRS 명명
- 쓰기: `XxxCommandService` (`@Transactional` 포함)
- 읽기: `XxxQueryService` (cursor-based pagination, replica datasource)

### 4. api/ 레이어
- `api/request/`, `api/response/` → **Java `record`** 필수
- `XxxResponse.from(domainObj)` factory method 패턴 필수
- 비즈니스 로직 **완전 금지**

### 5. 테스트
- `@Nested` per method, `@DisplayName` **한글** 필수
- Given / When / Then 구조
- domain 객체는 Mock 금지 (실객체 사용)
- Fixture는 `fixture/XxxFixture` 클래스로 분리

---

## 관련 스킬 연결

| 상황 | 사용 스킬 |
|------|----------|
| DDD 레이어 판단 | `/arch-policy` |
| JPA 엔티티/리포지토리 작성 | `/jpa` |
| 테스트 작성 | `/tdd-cycle` |
| SDD 설계 → 뼈대 생성 | `/sdd-craft` → `/spec-to-skeleton` |
| Java 코딩 표준 확인 | `/java-best-practices` (Rule 11 참조) |
