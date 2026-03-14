# Summary - Message (Bounded Context)

Purpose: ??? ??/??? ?? ?? ??? ?????.

Key packages:
- com.example.chat.message

Inbound:
- REST API (rest.controller)

Outbound:
- Persistence via chat-storage
- Messaging/Infrastructure integrations (Kafka/Redis where applicable)

Notes:
- Keep module boundaries and minimize cross-module access
