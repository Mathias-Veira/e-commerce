# Tareas pendientes — Product y Category

## Product

### 1. Añadir chequeo de duplicados en `addProduct`

Un producto se considera duplicado si ya existe otro con **el mismo nombre, precio y descripción** exactos.
Usar `ProductAlreadyExistsException` cuando se detecte el duplicado.

Pasos:
- Añadir método al outbound port `ProductRepository`:
  ```java
  boolean existsByNameAndPriceAndDescription(String name, BigDecimal price, String description);
  ```
- Implementar el método en `ProductRepositoryJPA` (consulta JPQL o Spring Data derived query sobre `ProductEntity`).
- En `ProductUseCaseImpl.addProduct`, llamar al nuevo método antes de persistir y lanzar `ProductAlreadyExistsException` si devuelve `true`.
- Añadir `ProductAlreadyExistsException` a `GeneralExceptionHandler` → HTTP 400.

### 2. Actualizar `ProductUseCaseImplTest` tras implementar el chequeo

Los tests de happy path de `addProduct` que no mockean el nuevo método del repositorio fallaran cuando se implemente el chequeo.
Stubear `productRepository.existsByNameAndPriceAndDescription(...)` devolviendo `false` en todos los casos positivos.

### 3. Correr toda la suite de tests de Product

```
mvnw test -Dtest=ProductDomainTest,ProductUseCaseImplTest
```

Verificar que todos pasan antes de avanzar a Category.

---

## Category

### 4. Añadir métodos de validación al dominio `Category`

Implementar en `Category.java` los métodos estáticos que ya tienen tests en `CategoryDomainTest`:

```java
public static boolean validateName(String categoryName)       // no null, no blank, max 100 chars
public static boolean validateDescription(String description) // no null, empty OK, max 500 chars
public static boolean validateProducts(List<Product> products)// no null, empty list OK
```

Correr `CategoryDomainTest` para verificar.

### 5. Crear las excepciones de Category

| Clase | Paquete | HTTP en el handler |
|---|---|---|
| `CategoryNotValidException` | `domain/exceptions/` | 400 |
| `CategoryNotFoundException` | `application/exceptions/` | 404 |
| `CategoryAlreadyExistsException` | `application/exceptions/` | 400 |

Seguir la misma estructura que `ProductNotValidException` / `ProductNotFoundException`.

### 6. Ampliar el outbound port `CategoryRepository`

Añadir el método de borrado físico que necesita `removeCategory`:

```java
void removeCategory(int categoryId);
```

Implementarlo en `CategoryRepositoryJPA`.

### 7. Implementar `CategoryUseCaseImpl`

Implementar los cinco métodos guiados por `CategoryUseCaseImplTest`:

- `addCategory` — validar nombre y descripción (`CategoryNotValidException`), comprobar unicidad de nombre case-insensitive usando `getAllCategories()` (`CategoryAlreadyExistsException`), persistir.
- `getAllCategories` — delegar directamente al repositorio.
- `getCategoryById` — delegar; lanzar `CategoryNotFoundException` si `Optional` vacío.
- `updateCategory` — comprobar existencia (`CategoryNotFoundException`), validar (`CategoryNotValidException`), persistir vía `addCategory` del repositorio (upsert).
- `removeCategory` — comprobar existencia (`CategoryNotFoundException`), llamar a `categoryRepository.removeCategory(id)`.

Anotar la clase con `@Service`.

Correr `CategoryUseCaseImplTest` para verificar.

### 8. Añadir las excepciones de Category a `GeneralExceptionHandler`

En `infrastructure/error/GeneralExceptionHandler.java` añadir tres handlers siguiendo el patrón existente:

```java
CategoryNotValidException      → 400
CategoryAlreadyExistsException → 400
CategoryNotFoundException      → 404
```

### 9. Capa web de Category (pendiente de diseño)

Aún no existe controlador ni DTOs para Category. Cuando se aborde:
- Crear `CategoryDTORequest` / `CategoryDTOResponse` en `infrastructure/web/dto/`.
- Crear `CategoryDTOMapper` en `infrastructure/web/mapper/`.
- Crear `CategoryController` (`@RestController`, base path `/api/categories`) con endpoints para los cinco casos de uso.
- Añadir los endpoints al `SecurityFilterChain` en `SecurityConfig`.

---

## Product (tras completar Category)

### 10. Verificar que las categorías referenciadas existen en `addProduct`

Regla de negocio (`product-business-rules.md`): `addProduct` **rechaza** una request que referencie una categoría que no existe en el catálogo.

Depende de que los pasos 4-9 (Category) estén completos, ya que hace falta `CategoryRepository`/`CategoryUseCase` para comprobar existencia.

Pasos:
- En `ProductUseCaseImpl.addProduct`, tras el resto de validaciones (precio/stock/descripción/categorías no vacías) y antes del chequeo de duplicados, comprobar que cada categoría de `product.getCategories()` existe (p. ej. vía `CategoryRepository.getCategoryById` por cada id).
- Si alguna categoría no existe, lanzar `ProductNotValidException` (mantiene el patrón existente: la capa de validación de `addProduct` ya lanza esta excepción para el resto de reglas).
- Actualizar `ProductUseCaseImplTest` con casos: categoría inexistente → `ProductNotValidException` y nunca persiste; categorías existentes → no lanza.
