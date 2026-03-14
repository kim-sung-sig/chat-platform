# Summary - Storage (Bounded Context)

Purpose: ?? ??? ???(JPA ???/?????)? ????? ??? ?????.

Key packages:
- com.example.chat.storage

Inbound:
- Application services using repositories

Outbound:
- Persistence via chat-storage
- Messaging/Infrastructure integrations (Kafka/Redis where applicable)

Notes:
- Keep module boundaries and minimize cross-module access
