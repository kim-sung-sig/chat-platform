# Test Plan: WebRTC Voice

## Inputs
- SDD: docs/specs/SDD_webrtc-voice.md

## Requirement -> Test Matrix
- T1 (FR1): 채팅방당 단일 음성 채팅방 생성/재사용
- T2 (FR2): 채팅방 멤버만 참여 가능
- T3 (FR3): 참여/퇴장 처리
- T4 (FR4): 시그널 전송(WebSocket)
- T5 (FR5): 시그널 수신자에게 전달

## Unit Tests
- VoiceRoomCommandService
  - join 성공/실패
  - leave 성공
- VoiceRoomQueryService
  - participants 조회 성공/실패
- VoiceSignalService
  - send 성공/실패
  - pull 성공
- VoiceSignalWebSocketController
  - WebSocket 전송 라우팅
- VoiceStompAuthChannelInterceptor
  - CONNECT 인증 처리

## Edge Cases
- 비활성 채팅방에서 join 실패
- 수신자 없는 시그널 전송 실패
- Authorization 헤더 누락 시 WebSocket 연결 실패

