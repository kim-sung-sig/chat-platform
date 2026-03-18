Review the specified scope for security risks.

Scope: $ARGUMENTS

## Instructions

1. Read the specified files.
2. Check for:
   - **Authentication/Authorization**: endpoints missing `@PreAuthorize` or JWT validation; improper role checks
   - **Input validation**: missing `@Valid`, unvalidated user input reaching the database or Kafka
   - **SQL injection**: JPQL/native queries with string concatenation
   - **Sensitive data exposure**: tokens, passwords, or PII logged or returned in responses
   - **Mass assignment**: DTOs mapped directly to entities without filtering
   - **IDOR**: resource access not scoped to the authenticated user
   - **Kafka/Redis**: unauthenticated topic access or sensitive data in cache without TTL
3. For each finding, provide:
   - Severity: CRITICAL / HIGH / MEDIUM / LOW
   - Location (file, line)
   - Description and exploit scenario
   - Recommended fix
