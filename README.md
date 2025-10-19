# Chat Platform (skeleton)

이 저장소는 문서 `채팅_어플리케이션_아키텍처_및_기획서.md`를 바탕으로 최소 동작하는 멀티 모듈 스켈레톤을 구성합니다.

주요 추가사항:
- 공통 로깅 유틸: `chat-common`에 `MdcUtil`, `RequestLoggingFilter`
- 모듈별 `logback-spring.xml` (structured JSON logging)
- `chat-message-server`에 Logstash encoder 의존성 추가

TODO (우선순위):
- [ ] Outbox 패턴 기반 이벤트 발행 구현 (message-server)
- [ ] 채널/메시지 도메인 엔티티 및 JPA 매핑 (storage)
- [ ] 인증/권한 모듈 설계 및 JWT 연동
- [ ] WebSocket 핸들러/세션 관리, Redis 기반 채널-유저 매핑
- [ ] 통합 테스트 (RabbitMQ, Redis, DB를 사용하는 E2E 테스트)

추가 변경 내용은 코드에 `TODO` 주석으로 남겨두었습니다. 프런트엔드/운영 연동을 위한 세부 설정이 필요합니다.