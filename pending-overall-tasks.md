# Pending overall tasks

Known gaps between what the docs say and what the code enforces, plus planned work. Each entry says **what** to change, **why** it matters, and **where**. Ordered roughly by priority within each section.

Longer-term product debt that is deliberately out of scope (returns/refunds, shipping tracking, server-side cart, invoice numbers) lives in the business-rules docs, not here — this file is for work that is actually next.

## Security

### ~~1. Make `SecurityFilter` actually authenticate the request~~ ✅ Done

### ~~2. Add roles to the JWT and authorize by role~~ ✅ Done

### 3. Stop hardcoding credentials in `application.properties`

`src/main/resources/application.properties` commits `spring.datasource.password=admin123` and `spring.security.user.password=<uuid>` in plaintext. Move them to environment variables (`${DB_PASSWORD}` etc.), as `SECRET_KEY` already does. Also consider whether `spring.security.user.*` is needed at all once the JWT filter is real — it configures Spring's default in-memory user, which the JWT flow does not use.

### 4. Fail fast on a missing `SECRET_KEY`

`JWTService`'s constructor does `System.getenv("SECRET_KEY").getBytes()` with no null check, so an unset variable fails context startup with a bare `NullPointerException` that does not say what is wrong. Read it via `@Value`/`Environment` and throw an `IllegalStateException` naming the variable, or supply it through configuration properties so Spring reports the missing value itself.

## Order correctness

### ~~5. Enforce the 100-unit line cap **after** merging duplicate lines~~ ✅ Done

### ~~6. Validate the order's `userId`~~ ✅ Done

### ~~7. Trim `OrderDTORequest` to what the client actually sends~~ ✅ Done

## Web adapter hygiene

### 8. Stop leaking the domain `OrderLine` through the API

`OrderDTORequest` and `OrderDTOResponse` both hold `List<OrderLine>` — the domain type — instead of a line DTO. This is the only place a domain class crosses the web boundary; every other vertical maps to DTOs. Add `OrderLineDTORequest`/`OrderLineDTOResponse` and map them in `OrderDTOMapper`. `OrderDTOResponse` also exposes the `OrderState` enum directly, which is a narrower version of the same issue.

### ~~9. Finish the `cancelOrder` rename~~ ✅ Done

## Cross-cutting

### 10. Concurrency-safe overselling prevention

Shared with [product-business-rules.md](./product-business-rules.md) and [order-business-rules.md](./order-business-rules.md), and already documented there as deferred. `addOrder` reads a product's stock and then decrements it in a separate step; under concurrency two orders can both pass the check and oversell. `@Transactional` alone does not prevent this at the default isolation level. The plan of record is a conditional decrement (`UPDATE … SET stock = stock - ? WHERE product_id = ? AND stock >= ?`) checked by affected-row count, or a pessimistic lock. Kafka is for throughput, not correctness — it does not solve this.
