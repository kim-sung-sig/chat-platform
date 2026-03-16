# Skeleton: WebRTC Voice

## Inputs
- SDD: docs/specs/SDD_webrtc-voice.md

## Package Layout (Generated)
- `com.example.chat.voice`
  - `domain.model`
    - `VoiceRoom`
    - `VoiceParticipant`
    - `VoiceSignal`
    - `VoiceSignalType`
    - `VoiceRoomStatus`
  - `domain.repository`
    - `VoiceRoomRepository`
    - `VoiceSignalRepository`
  - `application.service`
    - `VoiceRoomCommandService`
    - `VoiceRoomQueryService`
    - `VoiceSignalService`
  - `infrastructure.inmemory`
    - `InMemoryVoiceRoomRepository`
    - `InMemoryVoiceSignalRepository`
  - `rest.controller`
    - `VoiceRoomController`
  - `rest.dto.request`
    - `VoiceJoinRequest`
    - `VoiceLeaveRequest`
  - `rest.dto.response`
    - `VoiceRoomResponse`
    - `VoiceParticipantResponse`
  - `websocket`
    - `VoiceSignalWebSocketController`
    - `VoiceStompAuthChannelInterceptor`
  - `config`
    - `VoiceWebSocketConfig`

## REST API (Join/Leave/Participants)
- `POST /api/v1/channels/{channelId}/voice/join`
- `POST /api/v1/channels/{channelId}/voice/leave`
- `GET /api/v1/channels/{channelId}/voice/participants`

## WebSocket Signaling
- Endpoint: `/ws/voice`
- Client send: `/app/voice/{channelId}/signal`
- Server push (user): `/user/queue/voice/{channelId}`

## SDD Coverage Notes
- 채팅방 멤버십 검증은 `JpaChannelMemberRepository`로 확인
- 시그널링은 WebSocket(STOMP) 기반, 인메모리 저장소 사용

## Open Questions
- 인증/인가 연동 방식
- 음성 채팅방 자동 종료 정책
