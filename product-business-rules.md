# Product business rules

These were decided deliberately during design (via a grilling session). Follow them when implementing the `Product` domain, `ProductUseCase`, and the eventual `Category`/`Order` line-item modeling.

**Validation philosophy:** same as `User` ("Option A") — domain validation methods return `true`/`false`. The **use cases** call them, decide, and **throw** on `false`. The domain does **not** guarantee a `Product` is valid — that guarantee lives in the use-case layer.

- **Identity:** `productId` is the unique id, assigned by the database and never modified afterwards. Same treatment as `userId` — no setter for it in the domain. There is no SKU/business code; with no categories beyond a fixed seeded list, `productId` is the only identifier.
- **Categories:** a separate entity, related many-to-many (a product holds a **list** of categories). Category names are **unique**. The category catalog is **fixed/seeded directly in the database** for the MVP — there is no create/delete endpoint; managing categories requires direct DB access. Known debt: this is an operational shortcut, not a final answer — it will need a real CRUD once an admin role exists. A product **must** have at least one category; `addProduct` **rejects** a request referencing a category that doesn't exist. Deleting a category that still has products attached is out of scope/unresolved for now (direct DB access only).
- **Name (`productName`):** **can repeat** across products (mirrors `userName`'s "may repeat" rule) — this is the realistic option for a catalog. This implies a "flat products" model: there are **no variants** (e.g. "T-shirt S" and "T-shirt M" are two independent `Product` rows, each with its own stock and price), not one product with a variant list.
- **Price (`productPrice`):**
  - `BigDecimal`, currency is **euros only** for now (no multi-currency).
  - **Negative is blocked** in the domain; **zero is valid** (e.g. promotions/free items).
  - **Maximum 2 decimals.** If a request carries more than 2 decimals, it is **rejected** (no silent rounding) — never silently alter a price the caller typed.
  - `Product.productPrice` always reflects the **current catalog price** and can change freely.
  - **Order line items must copy ("freeze") the price at confirmation time.** The product's price is a live value; the order line's price is an independent snapshot taken when the order is confirmed. This is required so that past orders/invoices never change when the catalog price changes later — it is not optional debt, it's how the price-change requirement is satisfied. Consequence: `Order` (or its line items) needs its own price field; it must not be derived by reading `Product.productPrice` at render time.
- **Stock (`productStock`):**
  - "Out of stock" is **derived** from `productStock == 0` — it is not stored as a separate state, to avoid the stock count and a stored status disagreeing.
  - Negative stock is blocked in the domain.
  - Decremented once an order is **confirmed** (not at add-to-cart time).
  - **Overselling prevention is deferred** (planned alongside Kafka/queue work, after basic Product/Order/Category are done) — but the eventual fix is a transaction + conditional update ("decrement only if stock ≥ quantity"); Kafka is for throughput/scale, not correctness, so this is not blocked on Kafka.
- **Discontinued / lifecycle (`removeProduct`):**
  - A single boolean flag (e.g. `discontinued`) marks a product as removed. `removeProduct` is a **soft delete** — it sets this flag, it does not physically delete the row — so historical orders can still resolve the product (name, etc.) for a user's order history.
  - This flag is independent from the derived "out of stock" state: a product can be in stock and discontinued, or out of stock and not discontinued.
- **Description (`productDescription`):** required but may be an **empty string**; **never `null`**. Max length **2000** characters (`VARCHAR(2000)`).
- **Reads:**
  - `getProductById` returns the product **regardless of discontinued status** (needed so historical orders can still display it).
  - `getAllProducts` **must** filter out discontinued products by default while remaining **paginated**; the filter is enforced in the **backend** (never trust the frontend to hide data), with an explicit opt-in (e.g. an `includeDiscontinued` parameter, reserved for an eventual admin use case) to include them. This filtering is not optional/deferred — it is required from the first implementation of `getAllProducts`, not a later hardening pass.
- **Out of scope for now:** product variants (sizes/colors as a single product with sub-entities), category CRUD/admin management, roles (who can call `getAllProducts`/admin-only filters), and overselling prevention under concurrent orders.

**Exceptions (mirroring `User`'s pattern):** `ProductNotFoundException`, `ProductNotValidException`, `ProductAlreadyExistsException`. A product is a **duplicate** when another product already exists with the same `productName`, `productPrice`, **and** `productDescription` all three matching (name alone repeating is still fine — two products can share a name as long as price or description differs). `addProduct` checks this before persisting and throws `ProductAlreadyExistsException` when a match is found.
