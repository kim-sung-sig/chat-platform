# Summary - WebSocket (Bounded Context)

Purpose: ??? ??? ? ?? ??? ?????.

Key packages:
- com.example.chat.websocket

Inbound:
- WebSocket endpoints and handlers

Outbound:
- Persistence via chat-storage
- Messaging/Infrastructure integrations (Kafka/Redis where applicable)

Notes:
- Keep module boundaries and minimize cross-module access
