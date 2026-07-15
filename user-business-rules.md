# User business rules

These were decided deliberately during design. Follow them when implementing the `User` domain and `UserUseCase`.

**Validation philosophy (chosen "Option A"):** Domain validation methods return `true/false`. The **use cases** call them, decide, and **throw** on `false`. The domain does **not** guarantee a `User` is valid — that guarantee lives in the use-case layer.

- **Identity:** `userId` is the unique id, assigned by the database and never modified afterwards. `setUserId` is disabled (`@Setter(AccessLevel.NONE)` on the field) so `@Data`'s generated setter can't violate this.
- **Email:** format validated by regex in the domain (`validateEmail`, returns boolean, takes the incoming email as a parameter). **Uniqueness and existence** are validated in the use-case / infrastructure layer, not the domain. Email **can be updated** (revalidate format + uniqueness). Email is the login credential.
- **Username:** display name only, **may repeat** across users. 3–15 chars, **trimmed before measuring**, cannot be empty. Allowed: letters, numbers, and symbols like `_` / `-`; **`@` and similar are forbidden** (regex-checkable).
- **Password:** validated in **plaintext** (`validatePassword`, returns boolean) and **then hashed**. Hashing happens **in the use case** (on register and on login check); afterwards the `User.userPassword` field holds the **hash**. Length **8–20**, any character allowed, **never trimmed** (kept exactly as typed). On update: the caller always resends the plaintext password in the `User` object (a single-object update, no separate "change password" action). If that plaintext **matches the currently stored hash** (`passwordEncoder.matches`), it's treated as a **no-op** — keep the existing hash, skip format revalidation, don't re-hash; this lets a user update just their name/email without being forced to also change their password. If it **doesn't match**, that's a genuine password change request: revalidate format and **re-hash** it. Out of scope for now: blocking passwords that contain the email/username, and blocking common passwords.
- **Login:** takes **only email + password**, not a full `User`.
- **Lifecycle:** `updateUser` may change name, password, and email (each revalidated). `getUserById` returns an **`Optional`**; the use case throws if a missing user must be an error.
- **Out of scope for now:** roles (no roles yet) and account state (active/disabled/blocked, lockout on failed logins) — to be added after the basic rules are in place.

Known accepted debt from using Lombok `@Data`: `userPassword` means "plaintext" or "hash" depending on the moment.

**Debt mitigations (planned, staying with Option A):**

- **`userPassword` should mean one thing only — the hash.** The persisted `User` holds the **hashed** password only. The plaintext typed by the user is a transient input that lives in the use case (e.g. a separate parameter or a register/login command object) and is never stored on the persisted `User`. `validatePassword` runs on that transient plaintext before hashing; the domain object never carries the raw password at rest.
