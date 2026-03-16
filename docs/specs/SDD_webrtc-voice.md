# SDD: WebRTC Voice Chat

## 1. Title / Version / Status / Owners
- Title: WebRTC Voice Chat
- Version: 0.1
- Status: Draft
- Owners: TBD
- Related Docs (Planning/Skeleton/Test):
  - Skeleton: docs/design/skeletons/webrtc-voice/README.md
  - Tests: docs/tests/webrtc-voice_testplan.md

## 2. Problem Statement
- 채팅방 내 멤버 간에 WebRTC 기반 음성 채팅을 제공해야 한다.
- 음성 채팅방은 채팅방에 종속되며, 채팅방 멤버만 참여할 수 있다.

## 3. Goals / Non-Goals
### Goals
- 채팅방당 단일 음성 채팅방(Voice Room)을 제공한다.
- 채팅방 멤버만 음성 채팅방에 참여할 수 있다.
- WebRTC 시그널링 메시지 교환을 위한 서버 API를 제공한다.

### Non-Goals
- 실제 미디어 중계(미디어 서버/MCU/SFU) 구현은 포함하지 않는다.
- 외부 WebRTC TURN/STUN 서버 구성은 범위 밖이다.

## 4. Stakeholders / Target Users
- 일반 사용자(채팅방 참여자)
- 관리자(모니터링/운영)

## 5. Requirements
### Functional Requirements
- FR1: 채팅방당 단일 음성 채팅방이 존재한다.
- FR2: 채팅방 멤버만 음성 채팅방에 참여할 수 있다.
- FR3: 음성 채팅방 참여/퇴장 처리를 제공한다.
- FR4: WebRTC 시그널링 메시지(offer/answer/ice) 교환 API를 제공한다.
- FR5: 특정 사용자 대상의 시그널링 메시지를 조회할 수 있다.

### Non-Functional Requirements
- NFR1: 음성 채팅방 상태 및 참여자 변화는 로그로 남긴다.
- NFR2: 시그널링 메시지 저장은 인메모리로 처리하며, 장애 시 유실될 수 있다.

## 6. Domain Knowledge
- Glossary:
  - Voice Room: 채팅방에 종속된 음성 채팅 공간
  - Participant: Voice Room에 참여한 사용자
  - Signal: WebRTC 연결 설정을 위한 메시지
- Invariants:
  - Voice Room은 채팅방당 1개
  - 참여자는 채팅방 멤버여야 함
- Domain rules and edge cases:
  - 채팅방 비활성 상태에서는 참여 불가
  - 시그널링 메시지는 수신자 기준으로 조회 후 제거됨

## 7. Domain Model & Boundaries
- Bounded Context: Voice
- Aggregates:
  - VoiceRoom (aggregate root)
- Entities / Value Objects:
  - VoiceParticipant
  - VoiceSignal
  - VoiceSignalType
  - VoiceRoomStatus

## 8. Interfaces
- Commands:
  - JoinVoiceRoom
  - LeaveVoiceRoom
  - SendVoiceSignal
- Queries:
  - ListVoiceParticipants
  - PullVoiceSignals
- Error codes (draft):
  - VOICE-ROOM-NOT-FOUND
  - VOICE-NOT-CHANNEL-MEMBER
  - VOICE-CHANNEL-NOT-ACTIVE
  - VOICE-SIGNAL-INVALID
- External systems / integrations:
  - None (in-memory signaling)

## 9. Data Model
- VoiceRoom
  - channelId, status, createdAt, participants[]
- VoiceParticipant
  - userId, joinedAt
- VoiceSignal
  - id, channelId, fromUserId, toUserId, type, payload, createdAt

## 10. Workflow / State Transitions
- Join -> Active
- Leave -> (participants empty) still Active
- Pull signals -> messages removed for that user

## 11. Validation Rules & Edge Cases
- 채팅방 멤버가 아니면 참여/시그널 송수신 불가
- 시그널 타입은 OFFER/ANSWER/ICE만 허용
- 수신자 미지정 시 시그널 전송 실패

## 12. Security / Privacy / Compliance
- 채팅방 멤버만 참여 및 시그널 송수신 가능

## 13. Observability
- 참여/퇴장/시그널 송신 로그

## 14. Test Strategy
- 멤버십 검증 테스트
- 시그널 송수신 및 큐 제거 테스트
- 경계값(빈 참여자, 빈 시그널 큐)

## 15. Risks & Assumptions
- 인메모리 저장이므로 서버 재시작 시 시그널 유실

## 16. Open Questions
- 실제 인증/인가 연동 방식은?
- 음성 채팅방 자동 종료 정책 필요 여부?

## 17. Traceability
- FR1 -> T1
- FR2 -> T2
- FR3 -> T3
- FR4 -> T4
- FR5 -> T5

## 18. References
- SDD Template
