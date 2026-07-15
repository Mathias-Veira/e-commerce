# ToDo — Use cases del slice `User`

> Objetivo: dejar la capa de aplicación (`UserUseCaseImpl`) completa y alineada con las reglas de `CLAUDE.md`. **Estado: completo, con refactors de revisión pendientes (ver sección 5)**, todos los puntos de abajo están implementados y cubiertos por `UserUseCaseImplTest`.

## 1. Hashing de contraseñas en `addUser`

- [x] Inyectar un `PasswordEncoder` (BCrypt) en `UserUseCaseImpl`.
- [x] Validar el password en **plaintext** (`validatePassword`) **antes** de hashear.
- [x] Hashear el password y guardar el **hash** en `User.userPassword` antes de llamar a `userRepository.addUser(user)`.
- [x] Comprobar **unicidad de email** con `getUserByEmail` antes de insertar (lanza `UserAlreadyExistsException` si ya existe).

## 2. Login real (comprobar contra la contraseña guardada)

- [x] Validar formato de email y password de entrada (`validateEmail` / `validatePassword`).
- [x] Usar `getUserByEmail` para recuperar el usuario real → **esto cubre el punto 1 del análisis** (el puerto de salida ya no queda sin usar).
- [x] Si no existe usuario con ese email, login falla (lanza `UserNotFoundException`).
- [x] Comparar el password de entrada con el **hash** guardado usando `passwordEncoder.matches(plaintext, hash)`, **no** comparar plaintext con plaintext.
- [x] Dejar de crear un `new User()` fantasma solo para validar.

## 3. `updateUser`

- [x] No delegar en `addUser` (eso inserta en vez de actualizar).
- [x] Verificar que el usuario existe (por id).
- [x] Revalidar formato de email y username siempre.
- [x] Si cambia el email, revalidar **unicidad**.
- [x] Password: si el plaintext recibido **coincide** con el hash guardado (`encoder.matches`), es un **no-op** — se mantiene el hash existente sin rehashear (así cambiar solo nombre/email no obliga a "cambiar" la contraseña). Si **no coincide**, es un cambio real de password: se re-hashea.

## 4. Refactors menores (rápidos, no dependen de lógica pendiente)

- [x] `getUserById`: sustituir `orElse(null)` + `if` por `orElseThrow(() -> new UserNotFoundException(...))`.
- [x] Convertir las validaciones de `User` (`validateEmail`, `validateUsername`, `validatePassword`) en métodos `static` (no usan estado del objeto).
- [x] Quitar la validación redundante `user.validateEmail(user.getUserEmail())` en `addUser`.

## 5. Refactors de revisión (pendientes)

> Detectados al revisar `UserUseCaseImpl` contra las reglas de `CLAUDE.md`. Los dos primeros son de **comportamiento** (contradicen reglas explícitas aunque los tests pasen); el resto son de calidad/deuda.

### Comportamiento (corregir antes de seguir)

- [x] **A. No-op de password en `updateUser` sin revalidar formato.** Hoy se valida el formato del password siempre, *antes* de comprobar el no-op. CLAUDE.md exige que si el plaintext coincide con el hash guardado (`encoder.matches`), es no-op: mantener el hash, **no revalidar formato y no rehashear**. Reordenar para comprobar el no-op primero y solo revalidar+rehashear en cambios reales.
- [x] **B. Preservar `userId` desde el usuario existente.** `updateUser` persiste el `User` entrante con el id del caller. Construir el objeto a persistir con `existing.getUserId()` para que un id distinto en el input no tenga efecto (regla: el id lo asigna la BD y nunca se modifica).

### Calidad / deuda

- [x] **C. No mutar el `User` de entrada.** `addUser`/`updateUser` hacen `user.setUserPassword(...)` sobre el parámetro recibido. Construir un `new User(...)` con el hash ya resuelto (encaja con la deuda de que `userPassword` signifique "solo hash").
- [x] **D. Mensajes de excepción más específicos.** Email/username/password inválidos comparten `"User does not pass security checks"`. Separar al menos el de password.
- [ ] **E. Extraer la validación de formato duplicada** (`!validateEmail || !validateUsername || ...`) a un helper privado reutilizado por `addUser` y `updateUser`.

---

### Notas
- Reglas de negocio de referencia: ver sección **User business rules** en `CLAUDE.md`.
- El puerto `getUserByEmail` ya está implementado en `UserRepositoryJPA`; solo falta consumirlo.
