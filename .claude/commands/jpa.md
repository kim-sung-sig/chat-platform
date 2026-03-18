Apply the project's JPA conventions to: $ARGUMENTS

## Entity

- Extend `BaseEntity` (provides `id`, `createdAt`, `updatedAt`, `@PrePersist`, `@PreUpdate`)
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` — hide default constructor
- No `@Builder` on class — use a named static factory: `public static XxxEntity create(...)`
- Cohesive field groups → `@Embeddable` value object (e.g. `contentText + contentMediaUrl + contentMimeType` → `MessageContent`)
- Business state changes as methods on the entity — no setters

## Repository

- Named methods first: `findByChannelIdAndCreatedAtBefore(...)` over `@Query` JPQL
- Use `@EntityGraph` for eager-loading associations in a single query
- `@Query` JPQL only when named methods cannot express the query (bulk UPDATE/DELETE is acceptable)
- **Exception — complex multi-table JOIN**: When aggregate roots have no JPA association (`@OneToMany`/`@ManyToOne`) and the query requires a multi-table JOIN that cannot be expressed as a named method, `@Query` SELECT is permitted. Each such method **must** include a comment block beginning with `[APPROVED @Query EXCEPTION]` explaining why a named method cannot work.
- Cursor pagination: `findByXxxAndCreatedAtBefore(cursor, Pageable)` — never offset

## Checklist before finishing

- [ ] Entity extends `BaseEntity`
- [ ] No public default constructor
- [ ] Static factory method present
- [ ] Flat primitive groups extracted to `@Embeddable` where applicable
- [ ] No new JPQL `SELECT` queries — use named methods or `@EntityGraph` (exception: approved multi-table JOIN with `[APPROVED @Query EXCEPTION]` comment)
