# WebRTC Voice WebSocket Usage

## Endpoint
- WebSocket: `/ws/voice`

## STOMP Connect
- Header: `Authorization: Bearer <JWT>`

## Send Signal
- Destination: `/app/voice/{channelId}/signal`
- Payload:
```json
{
  "toUserId": "user-2",
  "type": "OFFER",
  "payload": "<sdp or ice json>"
}
```

## Receive Signal
- Subscription: `/user/queue/voice/{channelId}`
- Payload:
```json
{
  "id": "signal-id",
  "fromUserId": "user-1",
  "toUserId": "user-2",
  "type": "OFFER",
  "payload": "<sdp or ice json>",
  "createdAt": "2026-03-16T12:00:00Z"
}
```

## Notes
- 사용자 ID는 JWT subject에서 추출됨
- 채팅방 멤버가 아니면 시그널 전송/수신 불가
