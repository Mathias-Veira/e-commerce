# e-commerce

Spring Boot e-commerce service organized around **hexagonal (ports & adapters) architecture**. The `User`, `Product`, `Category`, and `Order` verticals are wired end-to-end: domain → ports → application → persistence adapter → web adapter.

The project is **dockerized**: a multi-stage `Dockerfile` builds and runs the app, and `docker-compose.yml` wires it up together with its own PostgreSQL container, so the whole stack runs with a single `docker compose up --build` — see [How to run](#how-to-run).

## Technologies

- **Java 21**
- **Spring Boot 4.1.0** (parent), built with **Maven** (use the wrapper: `./mvnw` on POSIX, `mvnw.cmd` on Windows)
- **Spring Data JPA** + **PostgreSQL** (`org.postgresql:postgresql` driver)
- **Spring Security** for authentication/authorization
- **JJWT** (`io.jsonwebtoken`, 0.12.6) for JWT issuing/validation
- **Lombok** (compile-time only, excluded from the packaged jar)
- **Docker** + **Docker Compose** for containerized builds/runs (multi-stage `Dockerfile`, `docker-compose.yml` with an `app` + `db` service)
- **JUnit 5 / Mockito / Testcontainers (PostgreSQL)** for testing

## Architecture

- **`domain/`** — Plain Java domain models (`User`, `Product`, `Category`, `Order`, `OrderLine`, `OrderState`, `Token`) with no framework/persistence annotations. Business rules are enforced as static validators (`validateEmail`, `validatePrice`, `validateQuantity`, etc.) that return `boolean`; the **use-case layer** decides and throws on `false` — the domain itself does not guarantee validity.
  - `OrderState` has only **`CONFIRMED`** and **`CANCELLED`** — there is no persisted "pending"/cart state. The cart lives in the frontend; `addOrder` creates *and* confirms an order in one step (freezing prices, decrementing stock).
- **`domain/exceptions/`** — Domain-level validation exceptions (`UserNotValidException`, `ProductNotValidException`, `CategoryNotValidException`, `OrderNotValidException`).
- **`domain/ports/in/`** — Inbound use-case interfaces: `UserUseCase`, `ProductUseCase`, `CategoryUseCase`, `OrderUseCase`.
- **`domain/ports/out/`** — Outbound repository interfaces: `UserRepository`, `ProductRepository`, `CategoryRepository`, `OrderRepository`. These are persistence primitives (return `Optional`); business logic stays in the use cases.
- **`application/`** — `@Service` use-case implementations (`UserUseCaseImpl`, `ProductUseCaseImpl`, `CategoryUseCaseImpl`, `OrderUseCaseImpl`), plus one exceptions subpackage per vertical (`user/`, `product/`, `category/`, `order/`).
- **`infrastructure/persistence/`** — JPA adapter, one subpackage per aggregate (`user/`, `product/`, `category/`, `order/`). Each has a JPA `@Entity`, a Spring Data `JpaRepository`, a repository-port implementation, and a static entity↔domain mapper, keeping the domain framework-free.
- **`infrastructure/web/`** — REST adapter: request/response DTOs, static DTO mappers, and `@RestController`s that call use cases directly (never repositories).
- **`infrastructure/error/`** — `GeneralExceptionHandler` (`@ControllerAdvice`) centrally maps exceptions to HTTP responses: `*NotFoundException` → 404; `*NotValidException`, `*AlreadyExistsException`, `LoginFailedException` → 400.
- **`infrastructure/configuration/`** — Spring Security + JWT wiring (`SecurityConfig`, `JWTService`, `SecurityFilter`).

## Endpoints & security

Authentication is JWT-based (`Authorization: Bearer <token>`). Unless noted, endpoints require a valid **access token**.

### Users — `/api/users`

| Method | Path | Security |
|---|---|---|
| POST | `/register` | Public |
| POST | `/login` | Public — returns an access + refresh token pair |
| POST | `/auth/refresh` | Public — exchanges a refresh token for a new pair |
| PUT | `/update` | Authenticated |
| GET | `/{userId}` | Authenticated |

### Products — `/api/products`

| Method | Path | Security |
|---|---|---|
| POST | `/register` | **ADMIN** role |
| GET | `/` (paginated) | Authenticated |
| GET | `/{productId}` | Authenticated |
| PUT | `/update` | **ADMIN** role |
| DELETE | `/{productId}` (soft delete) | **ADMIN** role |

### Categories — `/api/categories`

All `/api/categories/**` endpoints require the **ADMIN** role (not just the `GET` reads):

| Method | Path | Security |
|---|---|---|
| POST | `/register` | **ADMIN** role |
| GET | `/` | **ADMIN** role |
| GET | `/category?categoriesId=1,2` | **ADMIN** role |
| PUT | `/update` | **ADMIN** role |
| DELETE | `/{categoryId}` | **ADMIN** role |

### Orders — `/api/orders`

| Method | Path | Security |
|---|---|---|
| POST | `/register` | Authenticated — creates *and* confirms the order in one step |
| GET | `/` | Authenticated |
| GET | `/user/{userId}` | Authenticated |
| GET | `/product/{productId}` | Authenticated |
| PATCH | `/{orderId}/cancel` | Authenticated — only within 1 hour of confirmation |

### Security details

- Passwords are hashed with `BCryptPasswordEncoder` (strength 12).
- Access tokens are valid for **1 hour** and carry the user's roles; refresh tokens are valid for **7 days** and carry no roles (a role change takes effect on the next refresh).
- Roles: `USER` (default on every registration) and `ADMIN`.
- CSRF is disabled (stateless JWT API).

## How to run

### With Docker (recommended)

Requires **Docker** and **Docker Compose**. This spins up the app and a PostgreSQL container together, with Postgres schema created automatically on first boot (`spring.jpa.hibernate.ddl-auto=update`).

1. Clone the repo:
   ```bash
   git clone <repo-url>
   cd e-commerce
   ```
2. Create a `.env` file in the project root (gitignored — see `.env.example`) with the required environment variables:
   ```
   SECRET_KEY=<a-secret-at-least-32-bytes-long>
   DB_PASSWORD=<a-postgres-password>
   ```
3. Build and start the containers:
   ```bash
   docker compose up --build
   ```

This starts a `db` service (PostgreSQL) and an `app` service (the Spring Boot API on `http://localhost:8080`), with the app waiting for Postgres to be healthy before starting.

### Without Docker

Requires **JDK 21** and a running **PostgreSQL** instance matching `application.properties` (`jdbc:postgresql://localhost:5432/e_commerce`).

A `SECRET_KEY` environment variable is **required** to start the app — it must be long enough for HMAC-SHA (≥ 32 bytes). Without it, context startup fails.

```bash
# POSIX
export SECRET_KEY=<a-secret-at-least-32-bytes-long>
./mvnw spring-boot:run

# Windows PowerShell
$env:SECRET_KEY = "<a-secret-at-least-32-bytes-long>"
.\mvnw.cmd spring-boot:run
```

Build a jar:

```bash
./mvnw clean package
```

## How to run tests

```bash
# All tests
./mvnw test

# A single test class
./mvnw test -Dtest=ECommerceApplicationTests

# A single test method
./mvnw test -Dtest=ClassName#methodName
```

Tests live under `src/test/java/com/project/e_commerce/`:

- **`unit/`** — one subpackage per vertical (`user/`, `product/`, `category/`, `order/`), each with domain tests and Mockito-based use-case tests.
- **`integration/`** — persistence-adapter tests (backed by Testcontainers PostgreSQL).
- `ECommerceApplicationTests` — context-load smoke test.
