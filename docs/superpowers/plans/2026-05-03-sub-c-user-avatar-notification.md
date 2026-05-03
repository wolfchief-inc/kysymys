# Sub-C: User / Avatar / Notification — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (inline) or superpowers:subagent-driven-development. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the remaining three bounded contexts (User / Avatar / Notification) on the new stack, plus complete Sub-B's deferred items (`ListFollowerAnswers`, role-based authorization).

**Architecture:** Sub-A/B patterns are reused verbatim — `data/` records, `dao/` hand-written jOOQ DSL, `behavior/` pure functions, `resource/` kotowari-restful classes with Raoh decoders. New cross-cutting pieces: `events/` shared event types, `system/KysymysEventBus` (in-memory pub/sub component), `system/EnsureUserRegisteredMiddleware` (lazy signup).

**Tech Stack:** Same as Sub-B. Adds `com.talanlabs:avatar-generator-8bit:1.1.0` for the avatar PNG generator.

**PR strategy:** 5 internal phases mergeable as 1 large PR or 5 sequential PRs. Plan executes inline with phase-end commits.

---

## File Map

### New (Sub-C-1: schema + cross-cutting)

```
kysymys-app/src/main/resources/db/migration/V4__notification_enhancements.sql
kysymys-app/src/main/java/net/unit8/kysymys/events/
  KysymysEvent.java                    -- sealed marker
  SubmittedAnswerEvent.java
  OfferedToFollowEvent.java
  UserCreatedEvent.java
kysymys-app/src/main/java/net/unit8/kysymys/system/
  KysymysEventBus.java                 -- SystemComponent
  EnsureUserRegisteredMiddleware.java
kysymys-app/src/test/java/net/unit8/kysymys/system/
  KysymysEventBusTest.java
```

### Modified (Sub-C-1)

```
kysymys-app/src/main/resources/db/migration/V1__CREATE_INITIAL.sql
kysymys-app/src/main/resources/db/migration/V2__CreateWhatsNews.sql
kysymys-app/src/main/java/net/unit8/kysymys/lesson/behavior/SubmitAnswer.java   -- publish event after tx
kysymys-app/src/main/java/net/unit8/kysymys/KysymysDevSystemFactory.java         -- register eventBus component
kysymys-app/src/main/java/net/unit8/kysymys/KysymysSystemFactory.java
kysymys-app/src/main/java/net/unit8/kysymys/KysymysApplicationFactory.java       -- inject eventBus, mount EnsureUserRegisteredMiddleware
```

### New (Sub-C-2: User core)

```
kysymys-app/src/main/java/net/unit8/kysymys/user/data/
  User.java   EmailAddress.java   UserName.java   Role.java   Roles.java   Permission.java
kysymys-app/src/main/java/net/unit8/kysymys/user/dao/UserDao.java
kysymys-app/src/main/java/net/unit8/kysymys/user/behavior/
  EnsureUserRegistered.java   UpdateProfile.java
kysymys-app/src/main/java/net/unit8/kysymys/user/resource/
  UsersResource.java   UserResource.java   UserJsonDecoders.java   UserJsonEncoders.java
kysymys-app/src/test/java/net/unit8/kysymys/user/dao/UserDaoTest.java
```

### New (Sub-C-3: Follow + GrantTeacherRole)

```
kysymys-app/src/main/java/net/unit8/kysymys/user/data/
  Connection.java   OfferId.java   Offer.java
kysymys-app/src/main/java/net/unit8/kysymys/user/dao/
  ConnectionDao.java   OfferDao.java
kysymys-app/src/main/java/net/unit8/kysymys/user/behavior/
  GrantTeacherRole.java   OfferToFollow.java   AcceptFollow.java
kysymys-app/src/main/java/net/unit8/kysymys/user/resource/
  TeachersResource.java   GrantTeacherRoleResource.java
  OffersResource.java   AcceptOfferResource.java   FollowersResource.java
kysymys-app/src/test/java/net/unit8/kysymys/user/dao/{ConnectionDaoTest,OfferDaoTest}.java
```

### New (Sub-C-4: Avatar)

```
kysymys-app/src/main/java/net/unit8/kysymys/avatar/data/UserAvatar.java
kysymys-app/src/main/java/net/unit8/kysymys/avatar/dao/UserAvatarDao.java
kysymys-app/src/main/java/net/unit8/kysymys/avatar/behavior/EnsureAvatar.java
kysymys-app/src/main/java/net/unit8/kysymys/avatar/image/EightBitAvatarGenerator.java
kysymys-app/src/main/java/net/unit8/kysymys/avatar/resource/AvatarResource.java
kysymys-app/src/test/java/net/unit8/kysymys/avatar/dao/UserAvatarDaoTest.java
```

### New (Sub-C-5: Notification + Lesson Sub-B follow-up)

```
kysymys-app/src/main/java/net/unit8/kysymys/notification/data/
  WhatsNewId.java   WhatsNew.java   TemplatePath.java   UnreadWhatsNew.java
kysymys-app/src/main/java/net/unit8/kysymys/notification/dao/WhatsNewDao.java
kysymys-app/src/main/java/net/unit8/kysymys/notification/behavior/
  RecordWhatsNew.java   MarkAsRead.java
kysymys-app/src/main/java/net/unit8/kysymys/notification/resource/
  WhatsNewsResource.java   WhatsNewJsonEncoders.java
kysymys-app/src/main/java/net/unit8/kysymys/lesson/behavior/ListFollowerAnswers.java
kysymys-app/src/main/java/net/unit8/kysymys/lesson/resource/FollowerAnswersResource.java
kysymys-app/src/test/java/net/unit8/kysymys/notification/dao/WhatsNewDaoTest.java
kysymys-app/src/test/hurl/{user,avatar,notification}.hurl
```

### Modified (Sub-C-2 — role-based authorization for Lesson)

```
kysymys-app/src/main/java/net/unit8/kysymys/lesson/resource/ProblemsResource.java   -- @Decision(ALLOWED) checks TEACHER for POST
kysymys-app/src/main/java/net/unit8/kysymys/lesson/resource/ProblemResource.java    -- @Decision(ALLOWED) checks TEACHER for PUT/DELETE
```

### Modified (Sub-C-2 — JWT secret needs `permissions` claim)

`/tmp/GenToken.java` is for dev manual testing; no app code change needed (the BouncrBackend already extracts `permissions`).

---

## Phase 1 (Sub-C-1): Schema + cross-cutting infra

Operate identically to Sub-B Phase 1 — V1/V2 edits via direct file mutation, V4 adds new constraints, then add the `events/` package and `system/` components.

- [ ] **1.1** Edit V1 to use `VARCHAR(21)` for `users.id`, `connections.*_id`, `offers.*_id`, `user_avatars.user_id`, `user_roles.user_id`, `whats_news.id`/`user_id`. Run `mvn compile exec:java` to confirm Flyway re-applies on a fresh H2.
- [ ] **1.2** Edit V2 (`unread_whats_news`) to use `VARCHAR(21)` for both id columns.
- [ ] **1.3** Create `V4__notification_enhancements.sql` adding `unread_whats_news.user_id VARCHAR(21)` + FK to `users(id)`. Verify migrations apply v1+v2+v3+v4.
- [ ] **1.4** Create `events/KysymysEvent.java` (sealed) and the three subtype records (Submitted/OfferedToFollow/UserCreated).
- [ ] **1.5** Write `system/KysymysEventBus.java` extending `enkan.component.SystemComponent<KysymysEventBus>`. Test: subscribe + publish in `KysymysEventBusTest`.
- [ ] **1.6** Add the eventBus component to both `KysymysDevSystemFactory` and `KysymysSystemFactory` (under name `"eventBus"`, no relationships needed; resources fetch it via injection).
- [ ] **1.7** Write `system/EnsureUserRegisteredMiddleware.java` (mounts after authentication, before `ResourceInvokerMiddleware`). On principal present, call `UserDao.upsertFromPrincipal(...)`. Add it to `KysymysApplicationFactory` middleware stack (between authentication and resource invoker).
  - Note: this middleware lazily references `UserDao` which doesn't exist yet — Phase 2 creates it. Defer adding to `KysymysApplicationFactory` until Phase 2.
- [ ] **1.8** Update Lesson `SubmitAnswer` to receive the eventBus via constructor and publish `SubmittedAnswerEvent` after the transaction commits. Followers list comes from `ConnectionDao.listFollowersOf(answererId)` — also doesn't exist yet, so for Sub-C-1 just publish with `List.of()` and revisit in Sub-C-3.
- [ ] **1.9** Commit per logical step. Run `mvn test` to confirm Sub-B tests still pass.

## Phase 2 (Sub-C-2): User core + lazy signup + role-based auth on Lesson

- [ ] **2.1** Create `user/data/` records: `EmailAddress`, `UserName`, `Permission`, `Role`, `Roles`, `User`. Each follows Sub-B value-object pattern (compact constructor validation).
- [ ] **2.2** Create `user/dao/UserDao.java` with: `upsertFromPrincipal(UserId, EmailAddress, UserName, Roles)`, `findById`, `listAll(query)`, `update(User)`, `listByRole(Role)`. Test in `UserDaoTest`.
- [ ] **2.3** Create `user/behavior/EnsureUserRegistered.java` (called by `EnsureUserRegisteredMiddleware`). Wire the middleware into `KysymysApplicationFactory` now that `UserDao` exists.
- [ ] **2.4** Create `user/behavior/UpdateProfile.java`.
- [ ] **2.5** Create `user/resource/UsersResource.java` (GET /users), `UserResource.java` (GET/PUT /users/:id), `UserJsonDecoders.java`, `UserJsonEncoders.java`.
- [ ] **2.6** Add `@Decision(ALLOWED)` to `ProblemsResource` and `ProblemResource` checking `caller.permissions().contains("TEACHER")` for write methods. Use `kotowari-restful`'s `UserPermissionPrincipal.permissions()` (Sub-A's `MeResource` already extracts the principal).
- [ ] **2.7** Add Lesson resource permission check: `PUT /users/:id` rejects if `caller.userId != path.id`. Implement via `@Decision(ALLOWED)` reading `Parameters` and comparing.
- [ ] **2.8** Update `KysymysApplicationFactory` routes for User. Commit.

## Phase 3 (Sub-C-3): Follow + GrantTeacherRole

- [ ] **3.1** Create `user/data/`: `OfferId`, `Offer`, `Connection`.
- [ ] **3.2** Create `user/dao/`: `OfferDao`, `ConnectionDao` (`listFollowersOf(UserId) -> List<UserId>`, `listFolloweesOf(UserId)`, `add(followee, follower)`).
- [ ] **3.3** Behaviors: `OfferToFollow`, `AcceptFollow`, `GrantTeacherRole`. Each opens its own transaction.
- [ ] **3.4** Resources: `OffersResource`, `AcceptOfferResource`, `FollowersResource`, `TeachersResource`, `GrantTeacherRoleResource`. Routes wiring.
- [ ] **3.5** Update Lesson `SubmitAnswer` to read followers via `ConnectionDao.listFollowersOf(answererId)` (Sub-C-1 deferred this).
- [ ] **3.6** Commit + dao tests.

## Phase 4 (Sub-C-4): Avatar

- [ ] **4.1** Add Maven dependency `com.talanlabs:avatar-generator-8bit:1.1.0`.
- [ ] **4.2** Create `avatar/data/UserAvatar.java` (record).
- [ ] **4.3** Create `avatar/dao/UserAvatarDao.java`. Note: `user_avatars.image_content` is `BYTEA` (V1 was fixed in Sub-A) — read/write as `byte[]`.
- [ ] **4.4** Create `avatar/image/EightBitAvatarGenerator.java` wrapping `com.talanlabs.avatargenerator.eightbit.EightBitAvatar`. Returns `byte[]`.
- [ ] **4.5** Create `avatar/behavior/EnsureAvatar.java` — `find existing or generate+save`.
- [ ] **4.6** Create `avatar/resource/AvatarResource.java` returning `image/png`. Note: kotowari-restful's `SerDesMiddleware` only supports JSON by default — `AvatarResource` needs to bypass it. Approach: `AvatarResource.handleOk()` returns an `HttpResponse` directly (kotowari-restful allows this in handler methods).
- [ ] **4.7** Routes wiring + dao test + Hurl scenario for `/users/:id/avatar`.

## Phase 5 (Sub-C-5): Notification + ListFollowerAnswers + finalize

- [ ] **5.1** `notification/data/`: `WhatsNewId`, `TemplatePath`, `WhatsNew` (record), `UnreadWhatsNew` (record).
- [ ] **5.2** `notification/dao/WhatsNewDao.java`: `insert(WhatsNew)`, `insertUnread(UnreadWhatsNew)`, `listByUser(UserId)`, `markRead(UnreadWhatsNew id)`.
- [ ] **5.3** `notification/behavior/RecordWhatsNew.java` — registers itself as a subscriber on `KysymysEventBus` at startup. Inserts `whats_news` + `unread_whats_news` rows. Triggered by `SubmittedAnswerEvent` and `OfferedToFollowEvent`. Subscription happens at the start of `KysymysApplicationFactory.create(...)` (or in a dedicated `subscribe` Decision in the middleware setup).
- [ ] **5.4** `notification/behavior/MarkAsRead.java`.
- [ ] **5.5** `notification/resource/WhatsNewsResource.java` (`GET /whats-news`, `PUT /whats-news/:id/read`).
- [ ] **5.6** Lesson `ListFollowerAnswers`: reuse `ConnectionDao.listFolloweesOf(caller)` to get followee userIds, then `AnswerDao.listByAnswerers(List<UserId>)`. Add `listByAnswerers` to `AnswerDao`.
- [ ] **5.7** `lesson/resource/FollowerAnswersResource.java` (`GET /followers/answers`).
- [ ] **5.8** Routes wiring (final Hurl-tested set), Hurl scenarios for user/avatar/notification.
- [ ] **5.9** ADR 001 file refresh: update Notification section to mention the new event bus pattern.
- [ ] **5.10** Final `mvn test`. All tests green.

## Verification

```bash
mvn -pl kysymys-app -am clean compile

# All unit tests
mvn -pl kysymys-app test

# Start dev
cd kysymys-app && mvn exec:java &
sleep 8

# Generate JWT with email/name/permissions claims
TOKEN_TEACHER=$(java /tmp/GenTokenWithRole.java teacher@example.com Tanaka TEACHER)
TOKEN_STUDENT=$(java /tmp/GenTokenWithRole.java student@example.com Sato STUDENT)

# Existing Lesson scenarios still pass with role-aware tokens
hurl --variable host=http://localhost:3000 --variable token="$TOKEN_TEACHER" \
  src/test/hurl/lesson.hurl

# New Sub-C scenarios
hurl --variable host=http://localhost:3000 --variable token="$TOKEN_STUDENT" \
  src/test/hurl/user.hurl
hurl --variable host=http://localhost:3000 --variable token="$TOKEN_STUDENT" \
  src/test/hurl/avatar.hurl
hurl --variable host=http://localhost:3000 --variable token="$TOKEN_STUDENT" \
  src/test/hurl/notification.hurl

# Authorization spot checks
curl -i -H "x-bouncr-credential: $TOKEN_STUDENT" -X POST -H "Content-Type: application/json" \
  http://localhost:3000/problems -d '{"name":"x","repository":{"type":"generic","url":"https://x","branch":"main"}}'
# expect: 403
```

## Self-review

**Spec coverage:** every section of `2026-05-03-sub-c-user-avatar-notification-design.md` maps to a Phase. Lazy signup (1.7+2.3), role auth (2.6+2.7), event bus (1.4-1.6), all 3 contexts (2-5), Lesson `ListFollowerAnswers` (5.6).

**Placeholders:** none. The plan defers token-generation utility creation to phase 2 ("a JWT containing permissions/email/name") because BouncrBackend handles claim extraction generically — the dev `/tmp/GenToken.java` script needs an updated variant to include those claims, which can be done as a one-off helper outside the source tree.

**Type consistency:** `Roles` (record wrapping `Set<Role>`), `Permission` (enum), `Role` (enum carrying permissions). `Connection(UserId followee, UserId follower)`. `Offer(OfferId id, UserId offeringUserId, UserId targetUserId, LocalDateTime offeredAt)` — only UserId fields, not full `User` objects (the legacy version embedded full `User` which violated ADR 001's lazy reference rule).
