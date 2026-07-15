# Pending order tasks

Gaps found between `OrderUseCaseImpl` / `OrderUseCaseImplTest` and `order-business-rules.md`.

## 1. Delete `updateOrder`

- Remove `updateOrder` from `OrderUseCase` (inbound port).
- Remove the `updateOrder` method from `OrderUseCaseImpl`.
- Delete all `updateOrder_*` tests from `OrderUseCaseImplTest`.

**Rule:** Once an order exists it is CONFIRMED and is an immutable financial record. The only post-confirmation operation is cancellation within the 1-hour window. Cart editing is a frontend concern — there is no persisted PENDIENTE state to update.

---

## 2. Freeze price and name from the catalog in `confirmOrder`

Inside `confirmOrder`, for each incoming `OrderLine`:
- Call `productRepository.getProductById(line.getProductId())` to fetch the live product.
- Overwrite `line.setProductUnitPrice(product.getProductPrice())` and `line.setProductName(product.getProductName())` — never trust the client-supplied values.

**Rule:** "Confirmation is the single moment that freezes each line's `unitPrice` and `productName` from the current catalog."

---

## 3. Guard against discontinued products and insufficient stock in `confirmOrder`

Before calling `productRepository.removeStock(...)`, check:
- If `product.isDiscontinued()` → throw `OrderNotValidException`.
- If `product.getProductStock() < line.getQuantity()` → throw `OrderNotValidException`.

**Rule:** "An order cannot be confirmed if any line's product is discontinued or has insufficient stock."

Add tests: `confirmOrder_discontinuedProduct_throwsOrderNotValidException`, `confirmOrder_insufficientStock_throwsOrderNotValidException`.

---

## 4. Fix `isCancellable` bug in `Order.java`

Current code compares hours-of-day, which breaks across midnight:

```java
// WRONG
now.getHour() >= this.orderDate.plusHours(1).getHour()

// CORRECT
now.isAfter(this.orderDate.plusHours(1))
```

Add a test that confirms an order at 23:30 and attempts cancellation at 00:15 the next day — it must throw `OrderNotValidException`.
