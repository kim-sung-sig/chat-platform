Analyze performance hotspots and suggest concrete fixes.

Scope: $ARGUMENTS

## Instructions

1. Read the specified code to identify hotspots.
2. Focus on:
   - **N+1 queries**: missing `JOIN FETCH` or batch loading in JPA
   - **Missing indexes**: columns used in `WHERE` / `JOIN` without an index
   - **Cache misses**: data fetched repeatedly without Redis caching
   - **Blocking I/O on virtual threads**: synchronized blocks, ThreadLocal usage
   - **Kafka consumer lag**: slow processing or missing parallelism
   - **Cursor pagination**: check if offset-based pagination was used instead
3. For each finding, provide:
   - Location (file, line)
   - Problem description
   - Concrete fix with code example
4. Prioritize by impact; label each as HIGH / MEDIUM / LOW.
