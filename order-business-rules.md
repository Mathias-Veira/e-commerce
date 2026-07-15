# Order business rules

These were decided deliberately during design (via a grilling session). Follow them when implementing the `Order` domain, `OrderUseCase`, the `OrderLine` line-item model, and the persistence/web adapters. They build on and must stay consistent with [product-business-rules.md](./product-business-rules.md) (price freezing, stock decrement, discontinued products) and [user-business-rules.md](./user-business-rules.md).

**Validation philosophy:** same as `User` and `Product` ("Option A") — domain validation methods return `true`/`false`. The **use cases** call them, decide, and **throw** on `false`. The domain does **not** guarantee an `Order` is valid — that guarantee lives in the use-case layer.

## Modeling: `Order` + `OrderLine` (not `List<Product>`)

- An `Order` does **not** hold a `List<Product>`. It holds a **list of line items** (`List<OrderLine> items`). Pointing an order at `Product` objects would make it read the **live** catalog price (`Product.productPrice`), which changes freely — that would silently mutate historical orders and violate the price-freezing requirement in `product-business-rules.md`. `List<Product>` also has nowhere to store a quantity.
- **`OrderLine`** is the intermediate entity between `Order` and `Product`. Each line carries:
  - `productId` — reference to the catalog `Product` (the live entity, kept for linkage; never hard-deleted because `Product` is soft-deleted/discontinued).
  - `productName` — **frozen** copy of the product name at confirmation time (for the invoice).
  - `productUnitPrice` — **frozen** copy of the catalog price at confirmation time (`BigDecimal`, euros, ≤ 2 decimals, same rules as `Product.productPrice`).
  - `quantity` — units of this product in the order.
- **Why freeze both name and price:** a past order/invoice must never change when the catalog price or product name changes later. The frozen `productUnitPrice`/`productName` are an independent snapshot; they are **not** derived by reading `Product` at render time. (This supersedes the earlier "resolve the name live via `productId`" note in `product-business-rules.md` for orders — the name is snapshotted, not read live.)
- The domain model is framework-free; `OrderEntity`/`OrderLineEntity` in `infrastructure/persistence/order/` are the JPA side (tables `Orders` / `Order_Lines`, one-to-many with `cascade = ALL` and eager fetch), mapped via `OrderMapper`/`OrderLineMapper`.

## Identity & relationships

- **Identity:** `orderId` is the unique id, assigned by the database (`GenerationType.IDENTITY`) and never modified afterwards. Same treatment as `userId`/`productId` — no setter for it in the domain (`@Setter(AccessLevel.NONE)`). There is no human-readable order/invoice number for the MVP (known debt: a real invoice will likely need one later).
- **Order → User:** an order belongs to **one** user (`userId`, many-to-one). The owner is set at creation and never changes.
- **Order → OrderLine:** one-to-many; the order **owns** its lines.
- **OrderLine → Product:** each line references one `Product` by `productId` (plus the frozen `productName`/`productUnitPrice` snapshot).

## Totals

- `Order` stores its own **`orderTotal`** field (`BigDecimal`, euros). It is the sum of every line's `productUnitPrice × quantity`, **computed at confirmation time and stored** — not re-derived on every read. This keeps the order/invoice immutable even if lines or catalog prices change afterwards. Implemented as the domain instance method `Order.computeTotal()`, which the use case calls before persisting.

## Line quantity rules

- Minimum quantity per line is **1**; `0` and negatives are **rejected** (mirrors `Product`'s "negative stock blocked").
- Maximum quantity per line is **100** units. This is not a business limit but an anti-typo / anti-abuse guard-rail; it can be raised if a wholesale use case ever appears. Both bounds live in `OrderLine.validateQuantity`.
- The **same product appearing twice merges into a single line with the summed `quantity`** — there are never two lines for the same `productId` in one order. Since the cart is not persisted (see "Lifecycle & state"), this merge is enforced **when the order is assembled at confirmation time** (the backend never trusts the incoming cart to have deduped its lines). Implemented as the static domain method `Order.mergeLines`, which collapses by `productId` preserving first-seen order.
- An order **must contain at least one line**. Creating an order with **zero lines** (or a null list) is rejected — `Order.validateHasLines`.

## Lifecycle & state

- Order state is the **`OrderState` enum** with two states for the MVP: `CONFIRMED` → `CANCELLED`, persisted as `EnumType.STRING`. The enum is intentionally extensible (more states will come with the returns/shipping work, see "Out of scope"). There is **no persisted `PENDING`/draft state** — see the next point.
- **The cart is NOT a persisted order.** It lives in the frontend (client state) for the MVP; the backend has **no cart entity** and never writes a draft row to the `Orders` table. Carts are ephemeral, high-churn, and mostly abandoned — keeping them out of the durable `Orders` table keeps that table free of junk and consistent with "an order is an immutable financial record". (Server-side cart persistence — e.g. sharing a cart across devices via a session/Redis store kept **separate** from `Orders` — is known debt, see "Out of scope".)
- **An `Order` row is created only at confirmation, and creation *is* confirmation.** `OrderUseCase.addOrder` (`POST /api/orders/register`) receives the cart's contents and is the single moment that:
  1. Rejects an order with **zero lines** (`Order.validateHasLines`).
  2. Validates each incoming line's `quantity` and `productUnitPrice` (`OrderLine.validateQuantity` / `validateUnitPrice`).
  3. **Freezes** each line's `productUnitPrice` and `productName` from the current catalog (overwriting whatever the client sent — the client's prices are never trusted).
  4. Rejects the order if any line's product is **discontinued** or has **insufficient stock**.
  5. **Decrements** each product's `productStock` by the line `quantity` (`ProductRepository.removeStock`).
  6. Sets `orderState = CONFIRMED` and `orderDate = now`, and computes `deliveryDate` (see Dates).
  7. **Merges** duplicate `productId`s into one line each (`Order.mergeLines`).
  8. Computes and stores `orderTotal` (`Order.computeTotal`).

  `addOrder` is `@Transactional`, since it moves stock across several rows.
- **Confirmation guards:** an order **cannot be created** if it has **zero lines**, or if any line's product is **discontinued** or has **insufficient stock** (`productStock < quantity`). This is a best-effort check at confirmation time; robust **overselling prevention under concurrency is deferred** exactly as in `product-business-rules.md` (transaction + conditional decrement, planned alongside the Kafka/queue work — Kafka is for throughput, not correctness).

## Cancellation

- Cancelling is **not** a physical DELETE. `OrderUseCase.cancelOrder(orderId)` (`PATCH /api/orders/{orderId}/cancel`) sets the state to **`CANCELLED`** (soft, like `Product`'s discontinued flag) so the order stays as a financial/historical record, then re-saves it.
- **Cancellation window:** only a `CONFIRMED` order can be cancelled, and only within **1 hour** of its confirmation (`Order.isCancellable(now)`; the hour boundary itself is exclusive — at exactly `orderDate + 1h` it is no longer cancellable). After that hour the user must wait to receive the order and use the (future) returns flow instead. An order that is **already `CANCELLED` cannot be cancelled again**. (Discarding an unconfirmed cart is a pure frontend action — there is no backend row to cancel — so the 1-hour rule only ever concerns confirmed orders.)
- **Cancelling returns stock:** moving to `CANCELLED` **re-increments** each product's `productStock` by the line `quantity` (`ProductRepository.addStock`), undoing the confirmation decrement. `cancelOrder` is `@Transactional` for the same reason as `addOrder`.
- Enforcing the 1-hour window **requires time-of-day precision** — this is why `orderDate` is a `LocalDateTime`, not a `LocalDate` (see Dates).

## Dates

- **`orderDate`** is a **`LocalDateTime`** and holds the **confirmation instant** — which, since the order is *created* at confirmation, is also its creation time. It is the **single timestamp** the order needs: it drives both the estimated `deliveryDate` and the 1-hour cancellation window (`isCancellable` measures the hour from `orderDate`). No separate cart-creation date is needed, because the order does not exist before confirmation. Time-of-day precision is mandatory for the 1-hour window, hence `LocalDateTime` and not `LocalDate`.
- **`deliveryDate`** is an **estimated** `LocalDate`, computed and stored **at confirmation time** (from `orderDate`).
- **Estimated delivery calculation** lives in the **`Order` domain** (`Order.computeDeliveryDate`, a static domain method rather than use-case logic, since it is pure business logic): `deliveryDate = orderDate + 3 days`, and if that lands on a **weekend** (Saturday or Sunday) it is pushed to the following **Monday**.

## `OrderUseCase` operations

- Because the cart is not persisted, there is **no cart-editing operation on the backend** — building and editing the cart (add/remove lines, change quantities) is a frontend concern. There is likewise **no `updateOrder`**: a confirmed order is an immutable financial record, so the only state change available is cancellation.
- **`addOrder(order)`** — creates *and confirms* the order in one step (the full sequence under "Lifecycle & state"). Earlier drafts of this document called this operation `confirmOrder` and treated `addOrder` as a separate cart-write; the implementation collapsed the two, and `addOrder` is the name that exists.
- **`cancelOrder(orderId)`** — cancel (sets `CANCELLED`, restocks), subject to the cancellation window above.
- Read operations: `getAllOrders`, `getOrdersByUserId` (404s if the user does not exist), `getOrdersByProductId` (404s if the product does not exist).
- The outbound `OrderRepository` port additionally exposes `getOrderById` (returning `Optional`), used internally by `cancelOrder`; it is not surfaced as a use case or endpoint.
- The use case calls domain validation and outbound ports; it never talks to the JPA repository directly (same rule as `User`).

## Exceptions

Mirroring `User`/`Product`: `OrderNotFoundException` (→ 404) and `OrderNotValidException` (→ 400), both mapped in `GeneralExceptionHandler`. For now **`OrderNotValidException` covers every validation failure** — empty order, quantity out of `[1, 100]`, ordering a discontinued/out-of-stock product, cancelling outside the 1-hour window, cancelling twice. More specific exceptions (e.g. `OrderEmptyException`, `ProductNotAvailableException`) are **known debt** to be split out later. `addOrder` also propagates `ProductNotFoundException` when a line references a product that does not exist.

## Known debt in the current implementation

Gaps between the rules above and what the code actually enforces. These are known, not accidents to "fix" silently — but don't rely on the rule holding until they're closed.

- **The 100-unit cap can be bypassed by duplicate lines.** `addOrder` validates `quantity` per *incoming* line and merges duplicates *afterwards*, so two lines of 60 for the same product pass validation and merge into a single 120-unit line. Enforcing the cap on the merged result would close this.
- **`addOrder` never checks that `userId` exists.** An order can be created for a non-existent user, even though `getOrdersByUserId` does validate the user. The owner should be validated (or taken from the authenticated principal once `SecurityFilter` is real — it currently authenticates every request as a hardcoded `"userTest"`, see `CLAUDE.md`).
- **`OrderDTORequest` accepts server-owned fields** (`orderId`, `orderDate`, `deliveryDate`, `orderTotal`, `orderState`). They are all overwritten by `addOrder`, so this is not exploitable today, but the request DTO should only carry `userId` + `items`.
- **`OrderDTOResponse` exposes the domain `OrderLine` directly** instead of a line DTO, leaking a domain type through the web adapter.

## Out of scope for now

- **Returns / refunds** (the 14-days-from-delivery return flow): deferred until the rest of the order flow works. This will require extra states (e.g. `DELIVERED`, `RETURNED`) and a **real** delivery date rather than an estimate.
- **Shipping/delivery tracking** (real delivery dates, carrier states).
- **Server-side / cross-device cart persistence.** For the MVP the cart lives only in the frontend; a persistent cart (shared across devices, stored **separately** from `Orders`, e.g. session/Redis) is deferred. This replaces the earlier "multiple active draft carts per user" question, which is now a frontend concern.
- A human-readable invoice/order number.
- Concurrency-safe overselling prevention (shared with `product-business-rules.md`).
