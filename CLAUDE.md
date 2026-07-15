# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

Use the Maven wrapper (`./mvnw` on POSIX, `mvnw.cmd` on Windows PowerShell).

- Build: `./mvnw clean package`
- Run the app: `./mvnw spring-boot:run`
- Run all tests: `./mvnw test`
- Run a single test class: `./mvnw test -Dtest=ECommerceApplicationTests`
- Run a single test method: `./mvnw test -Dtest=ClassName#methodName`

Requires JDK 21 (set in `pom.xml` via `java.version`). Spring Boot parent is 4.1.0.

**`SECRET_KEY` environment variable is required to start the app.** `JWTService`'s constructor reads `System.getenv("SECRET_KEY")` and passes it to `Keys.hmacShaKeyFor`, so an unset variable fails context startup with an NPE. It must be long enough for HMAC-SHA (≥ 32 bytes).

## Architecture

This is a Spring Boot e-commerce service organized around **hexagonal (ports & adapters) architecture**. The `User`, `Product`, `Category`, and `Order` verticals are all wired end-to-end (domain → ports → application → persistence adapter → web adapter).

- **`domain/`** — Plain Java domain models (`User`, `Product`, `Category`, `Order`, `OrderLine`, `OrderState`, `Token`) with no framework/persistence annotations. They use Lombok (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@NonNull`, and `@Setter(AccessLevel.NONE)` on identity fields like `productId`/`categoryId`/`orderId`). Domain logic lives here as static validators returning `boolean` plus a few instance methods:
  - `User.validateEmail`, `validateUsername`, `validatePassword`
  - `Product.validatePrice`, `validateStock`, `validateDescription`, `validateCategories`; instance `isOutOfStock()` (`productStock == 0`); a `discontinued` flag used for soft deletion
  - `Category.validateName`, `validateDescription`, `validateProducts`
  - `OrderLine.validateQuantity` (1–100), `validateUnitPrice`
  - `Order.validateHasLines`, static `computeDeliveryDate` (order date + 3 days, pushed off Saturday/Sunday), static `mergeLines` (collapses duplicate `productId` lines summing quantities), instance `computeTotal()` and `isCancellable(now)` (1-hour window after `orderDate`)
  - `OrderState` is an enum with **`CONFIRMED` and `CANCELLED` only** — there is no `PENDING`/cart state. The cart lives in the frontend and `addOrder` creates *and* confirms the order in one step (freezing prices, decrementing stock). This is by design, per `order-business-rules.md`.
  - `Token` is the domain model for a JWT pair (`accessToken` + `refreshToken`).
- **`domain/exceptions/`** — Domain-level validation exceptions: `UserNotValidException`, `ProductNotValidException`, `CategoryNotValidException`, `OrderNotValidException`.
- **`domain/ports/in/`** — Inbound port interfaces (driving side): `UserUseCase` (`login`/`sendRefreshToken` both return `Token`; `addUser`, `updateUser`, `getUserById`), `ProductUseCase` (`getAllProducts` is paginated — takes `Pageable`, returns `Page<Product>`), `CategoryUseCase`, `OrderUseCase` (`addOrder`, `getAllOrders`, `getOrdersByUserId`, `getOrdersByProductId`, `cancelOrder`).
- **`domain/ports/out/`** — Outbound port interfaces (driven side): `UserRepository` (`addUser`, `getUserById`, `getUserByEmail`, `getUserRolesByEmail` — returns `Set<Role>` for loading authorities without putting roles on the domain `User`), `ProductRepository` (includes `productAlreadyExists` plus `removeStock`/`addStock` for inventory moves), `CategoryRepository`, `OrderRepository`. Keep these as persistence primitives returning `Optional`; business operations like login belong in the use case, not the port.
- **`application/`** — Use-case implementations, all `@Service`: `UserUseCaseImpl` (depends on `UserRepository`, `PasswordEncoder`, `JWTService`; `login` fetches roles via `getUserRolesByEmail` and passes them to `generateToken`; `sendRefreshToken` re-reads roles from the DB on every rotation so a role change takes effect on the next refresh), `ProductUseCaseImpl` (`ProductRepository` + `CategoryRepository`; `removeProduct` is a **soft delete** that sets `discontinued = true` and re-saves), `CategoryUseCaseImpl`, `OrderUseCaseImpl` (`OrderRepository` + `UserRepository` + `ProductRepository`; `addOrder` validates `userId` existence, merges duplicate lines before applying the quantity cap, and is `@Transactional` because it decrements stock; `cancelOrder` is also `@Transactional` because it restores stock).
- **`application/exceptions/`** — Use-case-level exceptions, one subpackage per vertical: `user/` (`UserNotFoundException`, `UserAlreadyExistsException`, `LoginFailedException`), `product/` (`ProductNotFoundException`, `ProductAlreadyExistsException`), `category/` (`CategoryNotFoundException`, `CategoryAlreadyExistsException`), `order/` (`OrderNotFoundException`).
- **`infrastructure/persistence/`** — JPA adapter, **one subpackage per aggregate** (`user/`, `product/`, `category/`, `order/`). Each follows the same four-class shape: an `@Entity`/`@Table` persistence model kept **separate** from the framework-free domain model (`UserEntity`, `ProductEntity`, `CategoryEntity`, `OrderEntity`, `OrderLineEntity`), a Spring Data `JpaRepository` (`UserJPARepository`, `ProductJPARepository`, `CategoryJPARepository`, `OrderJPARepository`), an outbound-port implementation delegating to it (`UserRepositoryJPA`, `ProductRepositoryJPA`, `CategoryRepositoryJPA`, `OrderRepositoryJPA`), and a static mapper converting entity ↔ domain (`UserMapper`, `ProductMapper`, `CategoryMapper`, `OrderMapper`, `OrderLineMapper`) so the domain stays free of JPA. `UserMapper.toEntity` always sets `roles = new HashSet<>(Set.of(Role.USER))` — roles default to `USER` on every write; `Role` is an enum (`USER`, `ADMIN`) in `infrastructure/persistence/user/` stored as `@ElementCollection` with `FetchType.EAGER` on `UserEntity`.
- **`infrastructure/web/`** — REST adapter (driving side).
  - `dto/request/` — `UserDTORequest`, `LoginDTORequest` (email + password only, matching the business rule that login takes credentials, not a full `User`), `RefreshTokenDTORequest` (record, `refreshToken`), `ProductDTORequest`, `CategoryDTORequest`, `OrderDTORequest`.
  - `dto/response/` — `UserDTOResponse` (everything **except** `userPassword`, so hashes never leave the API), `TokenDTOResponse` (record: `accessToken`, `refreshToken`), `ProductDTOResponse`, `CategoryDTOResponse`, `OrderDTOResponse`.
  - `web/mapper/` — static mappers: `UserDTOMapper`, `TokenDTOMapper`, `ProductDTOMapper`, `CategoryDTOMapper`, `OrderDTOMapper`. Direction is request → domain and domain → response only (no domain → request, no response → domain), except `TokenDTOMapper` which also exposes `toDomain` since `TokenDTOResponse` is the only Token DTO.
  - `web/controller/` — `@RestController`s that call use cases directly (never repositories) and return `ResponseEntity`:
    - `UserController` (`/api/users`): `POST /login` → `Token` → 200 `TokenDTOResponse`; `POST /auth/refresh` → `sendRefreshToken` → 200 `TokenDTOResponse`; `POST /register`; `PUT /update`; `GET /{userId}`.
    - `ProductController` (`/api/products`): `POST /register`; `GET` (paginated, accepts `Pageable`, returns `Page<ProductDTOResponse>`); `GET /{productId}`; `PUT /update`; `DELETE /{productId}` → 204.
    - `CategoryController` (`/api/categories`): `POST /register`; `GET`; `GET /category?categoriesId=1,2`; `PUT /update`; `DELETE /{categoryId}` → 204.
    - `OrderController` (`/api/orders`): `POST /register`; `GET`; `GET /user/{userId}`; `GET /product/{productId}`; `PATCH /{orderId}/cancel` → 204.
- **`infrastructure/error/`** — Centralized exception-to-HTTP mapping for the web adapter. `GeneralExceptionHandler` (`@ControllerAdvice`, extends `ResponseEntityExceptionHandler`) converts use-case/domain exceptions into an `ErrorDTO` (`statusCode` + `mensaje`). Current mapping: all `*NotFoundException` → 404; all `*NotValidException`, `*AlreadyExistsException`, and `LoginFailedException` → 400. Add new exception handlers here rather than handling errors ad hoc inside controllers.
- **`infrastructure/configuration/`** — Spring Security + JWT wiring.
  - `SecurityConfig` (`@Configuration`) defines the `PasswordEncoder` bean (`BCryptPasswordEncoder`, strength 12) used by `UserUseCaseImpl`, and the `SecurityFilterChain` (CSRF disabled; `POST /api/users/login`, `POST /api/users/register`, `POST /api/users/auth/refresh` are `permitAll()`; `DELETE /api/products/**` and `/api/categories/**` require `ADMIN` role; everything else requires `authenticated()`; `SecurityFilter` registered before `UsernamePasswordAuthenticationFilter`).
  - `JWTService` (`@Service`) issues and inspects JJWT tokens signed with HMAC-SHA from `SECRET_KEY`: `generateToken(userEmail, Set<Role> roles)` (1-hour access token, claims `type=access` and `roles` as a list of role name strings), `generateRefreshToken(userEmail)` (7-day, claim `type=refresh` — roles excluded so role changes take effect on next refresh), `extractUserEmail`, `extractRoles` (returns `List<String>` of role names from the token claim), `isValid`, `isRefreshtoken`.
  - `SecurityFilter` (`OncePerRequestFilter`) reads the `Authorization: Bearer <token>` header. If absent, continues the chain without setting authentication (Spring Security rejects unauthenticated requests per the filter chain config). If present: rejects refresh tokens used as access tokens, validates with `isValid`, extracts the email via `extractUserEmail`, loads roles via `userRepository.getUserRolesByEmail`, builds `SimpleGrantedAuthority` entries, and sets a `UsernamePasswordAuthenticationToken` on the `SecurityContextHolder`. Invalid tokens are forwarded to the `HandlerExceptionResolver` as a `UserNotValidException`.

When adding features, implement the inbound use-case interfaces rather than calling repositories directly, depend on the outbound ports (not the JPA repo) from the application layer, and keep `domain/` free of Spring/JPA dependencies.

## Tests

Tests live under `src/test/java/com/project/e_commerce/` and are split by kind:

- **`unit/`** — one subpackage per vertical (`user/`, `product/`, `category/`, `order/`), each holding domain tests (`UserDomainTest`, `ProductDomainTest`, `CategoryDomainTest`, `OrderDomainTest`, `OrderLineDomainTest`) and use-case tests with Mockito (`UserUseCaseImplTest`, `ProductUseCaseImplTest`, `CategoryUseCaseImplTest`, `OrderUseCaseImplTest`).
- **`integration/`** — persistence-adapter tests (`UserRepositoryTest`, `ProductRepositoryTest`, `CategoryRepositoryTest`, `OrderRepositoryTest`).
- `ECommerceApplicationTests` is the context-load smoke test.

## Pending work

[pending-overall-tasks.md](./pending-overall-tasks.md) tracks the known gaps between these docs and what the code enforces, plus planned work. Check it before starting on security or the order flow.

## Business rules

- [user-business-rules.md](./user-business-rules.md) — validation/email/password/lifecycle rules for the `User` domain and `UserUseCase`, including known debt and planned mitigations.
- [product-business-rules.md](./product-business-rules.md) — category/name/price/stock/lifecycle rules for the `Product` domain and `ProductUseCase`, including known debt (category management, variants, overselling).
- [order-business-rules.md](./order-business-rules.md) — rules for the `Order` domain, `OrderLine` line-item model, and `OrderUseCase`: the frontend-only cart, frozen `productUnitPrice`/`productName` per line, stored `orderTotal`, quantity rules, the two-state `CONFIRMED`/`CANCELLED` lifecycle, the 1-hour cancellation window (restocks), delivery-date estimation, and known debt (returns/refunds, overselling).

## Key conventions & gotchas

- **Package name:** The Maven artifact is `e-commerce`, but the Java package is `com.project.e_commerce` (underscore), because `com.project.e-commerce` is an invalid Java identifier. New files must use the underscore form.
- **Persistence:** Depends on Spring Data JPA + the PostgreSQL JDBC driver (`org.postgresql:postgresql`). `application.properties` has a PostgreSQL datasource configured (`jdbc:postgresql://localhost:5432/e_commerce`). Persistence models live in `infrastructure/persistence/<aggregate>/` and are mapped to/from the domain via the static mappers; JPQL `@Query` strings must reference the **entity** name (`UserEntity`, `ProductEntity`, …), not the domain class.
- **Security:** `spring-boot-starter-security` plus JJWT are wired up in `infrastructure/configuration/` (see above). Passwords are hashed with `BCryptPasswordEncoder` (strength 12) via the `PasswordEncoder` bean.
- **Credentials in `application.properties` are committed in plaintext** (datasource password, `spring.security.user.password`). Don't add more; prefer environment variables, as `SECRET_KEY` already does.
- **Lombok** is an optional/provided dependency and is configured as an annotation processor in the compiler plugin; it is excluded from the repackaged boot jar.
