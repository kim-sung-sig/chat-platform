# chat-system-server 리팩토링 진행 상황

## 작업 계획

### 1. ScheduleService 재작성
- [ ] Domain Service 활용
- [ ] ScheduleRule 도메인 메서드 사용
- [ ] Early Return 패턴 적용

### 2. DTO 수정
- [ ] ScheduleResponse
- [ ] CreateOneTimeScheduleRequest
- [ ] CreateRecurringScheduleRequest

### 3. MessagePublishJob 수정
- [ ] ScheduleRule 도메인 연동
- [ ] 동시성 제어

## 현재 상황

chat-message-server ✅ 완료
chat-websocket-server ✅ 완료
chat-system-server 🔄 진행 중
