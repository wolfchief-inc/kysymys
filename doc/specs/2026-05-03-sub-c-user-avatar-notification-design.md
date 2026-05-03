# Sub-C: User / Avatar / Notification context migration

**作成日**: 2026-05-03
**対象ブランチ**: `feature/sub-c-user-avatar-notification`
**ベース**: `develop` (Sub-A, Sub-B 完了済み)

## Context

Sub-A (土台) → Sub-B (Lesson) に続いて、残りの 3 Bounded Context (User / Avatar / Notification) を新スタックで完全実装する。これで kysymys-app のバックエンド機能はすべて新スタックに移行する。

参照モデルは引き続き `kotowari-restful` の `example/` と `validation-modeling` の `raoh/`。Sub-B で Sub-A の `UserId` を先取り定義し、Lesson 側で使えるようにしてあるが、本 spec で User context 全体を完成させる。

## ゴール

User / Avatar / Notification の主要ユースケースが新スタックで動作する:

- **User**: lazy signup (JWT 初出現時に自動登録)、UpdateProfile、ShowProfile、SearchUsers、ListUsers、ListTeachers、GrantTeacherRole、OfferToFollow、AcceptFollow、ListFollowers、ListOffers
- **Avatar**: GenerateAvatar (8bit-avatar 自動生成)、GetAvatarImage
- **Notification**: WhatsNew 永続化 (Lesson SubmittedAnswer / User OfferedToFollow に反応)、GetWhatsNews、MarkAsRead

加えて Lesson 側の Sub-B で延期した `ListFollowerAnswers` を実装する (フォロー関係参照が可能になるため)。

役割認可: `permissions` クレームを `BouncrBackend` の `UserPermissionPrincipal` 経由で取り出し、各 resource の `@Decision(ALLOWED)` で「先生のみ Problem CRUD」「自分の profile のみ更新可能」等を判定する。

## 全体方針 (確定済み決定事項)

| 観点 | 決定 |
|---|---|
| Sub-C スコープ | User / Avatar / Notification を1 spec、内部 PR は機能単位で分割可 |
| 認証 | Sub-A の HMAC 固定キー JWT 維持。Bouncr 本物スタック (Envoy + bouncr-proxy + bouncr-api-server) 接続は Sub-F に分離 |
| 役割認可 | JWT の `permissions` クレームから `UserPermissionPrincipal.permissions` (Set<String>) を取得し、`@Decision(ALLOWED)` で判定 |
| Signup | **Lazy signup**: JWT principal が初出現したときに kysymys-app 側で `users` 行を自動 upsert。`email` / `name` クレームから取得。`EnsureUserRegisteredMiddleware` を新設 |
| GitHub OAuth2 | Bouncr 本物接続 (Sub-F) で OIDC IdP の external IdP として連ねる。Sub-C のスコープでは扱わない |
| Avatar アップロード | 自動生成 only (旧版同様)。multipart upload は Sub-D で React 経由に |
| メール送信 | **WhatsNew 永続化のみ実装、SMTP は将来 Sub に分離**。`SendMailEvent` は publish するが listener は noop または削除 |
| イベント駆動 | 自前 `KysymysEventBus` (in-memory) を `SystemComponent` として作る。Lesson の `SubmitAnswer` を改修して publish。Notification 側で subscribe |
| Cross-context Event 配置 | `net.unit8.kysymys.events.*` 共通 package |
| ID 列の型 | nanoid 21桁で統一。V1 編集で users / connections / offers / user_avatars / user_roles / whats_news / unread_whats_news の id 系列を `VARCHAR(21)` に揃える |
| password 列 | password 認証は Bouncr 責務に委ねる方針 (Sub-F)。V1 の `users.password` 列は **削除しない** が新スタックでは未使用。データは null のまま |
| Connection 表現 | 旧 `connections(followee_id, follower_id)` 構造を踏襲、record `Connection(UserId followee, UserId follower)` |
| ドメイン認可ロジック | role 判定は resource 層 (`@Decision(ALLOWED)`) に集約。domain layer に Spring Security 由来の `UserDetails` / `OAuth2User` 実装は持ち込まない |

## 完了条件

1. Lesson 関連 + User/Avatar/Notification 関連の全テーブルがアプリ起動時に Flyway で適用される (V1 編集 + V4 新規マイグレーション)。
2. **Lazy signup**: 認証済み JWT (claims `sub`, `email`, `name`, `permissions: ["STUDENT"]` 等) を持って `/me` を叩くと、初回呼び出し時に `users` テーブルに自動的に行が作られる。2回目以降は既存行を返す。
3. **User**: `GET /users` (検索)、`GET /users/:id` (profile)、`PUT /users/:id` (自分のみ更新可)、`POST /grant-teacher-role` (先生のみ)、`POST /offers` (フォロー申請)、`PUT /offers/:id/accept` (申請受領)、`GET /offers` (受信した申請一覧)、`GET /users/:id/followers` (フォロワー一覧) が動作。
4. **Avatar**: `GET /users/:id/avatar` で 8bit avatar PNG を返す。初回は自動生成して保存、2回目以降はキャッシュ済みを返す。
5. **Notification**: Lesson `SubmitAnswer` 完了時、 User `OfferToFollow` 完了時にイベントが publish され、`whats_news` / `unread_whats_news` に行が積まれる。`GET /whats-news` (自分宛て一覧)、`PUT /whats-news/:id/read` (既読化) が動作。
6. **Lesson Sub-B 拡張**: `ListFollowerAnswers` を実装、`GET /followers/answers` が動作 (caller のフォロー先の解答一覧)。
7. **役割認可**: `先生 (TEACHER)` 権限なしで `POST /problems` を叩くと `403`。 `自分以外の userId` で `PUT /users/:id` を叩くと `403`。
8. dao 層 + behavior 層 + EventBus に JUnit テストを書く。
9. E2E は `kysymys-app/src/test/hurl/user.hurl`, `avatar.hurl`, `notification.hurl` を追加。

## 含めない (後続 Sub に回す)

- Bouncr 本物スタック (Envoy + bouncr-proxy + bouncr-api-server) との接続 — **Sub-F**
- パスワード認証フロー (signup / login API + password ハッシュ) — Bouncr 責務、Sub-F
- GitHub OAuth2 ログイン — Bouncr 側で external IdP として連ねる、Sub-F
- SMTP メール送信 — **Sub-G** (template engine 選定込み)
- Avatar の multipart upload (画像差し替え) — **Sub-D** (React 経由で実装)
- React フロント — **Sub-D**

## ディレクトリ構造

Sub-A の `health/` `inject/` + Sub-B の `lesson/` の隣に追加:

```
kysymys-app/src/main/java/net/unit8/kysymys/
├── (Sub-A 既存)
├── (Sub-B 既存) lesson/
├── events/                            -- cross-context Event 共通 package (新規)
│   ├── SubmittedAnswerEvent.java
│   ├── OfferedToFollowEvent.java
│   ├── UserCreatedEvent.java
│   └── KysymysEvent.java              -- sealed marker interface
├── system/                            -- インフラレベル component (新規)
│   ├── KysymysEventBus.java           -- in-memory pub/sub component
│   └── EnsureUserRegisteredMiddleware.java  -- lazy signup
├── inject/
│   └── (Sub-A 既存)
├── user/
│   ├── data/
│   │   ├── (Sub-B 既存) UserId.java
│   │   ├── User.java                  -- record (id, email, name, roles)
│   │   ├── EmailAddress.java          -- record (1..100, "@" 含む)
│   │   ├── UserName.java              -- record (1..100)
│   │   ├── Role.java                  -- enum STUDENT / TEACHER (旧版踏襲)
│   │   ├── Roles.java                 -- record (Set<Role>) + permissions 計算
│   │   ├── Permission.java            -- enum SUBMIT_ANSWER / POST_COMMENT / CREATE_PROBLEM / GRANT_TEACHER_ROLE
│   │   ├── Connection.java            -- record (followee, follower)
│   │   ├── OfferId.java
│   │   └── Offer.java                 -- record (id, offeringUserId, targetUserId, offeredAt)
│   ├── behavior/
│   │   ├── EnsureUserRegistered.java  -- lazy signup (upsert)
│   │   ├── UpdateProfile.java
│   │   ├── GrantTeacherRole.java
│   │   ├── OfferToFollow.java
│   │   └── AcceptFollow.java
│   ├── dao/
│   │   ├── UserDao.java
│   │   ├── ConnectionDao.java
│   │   └── OfferDao.java
│   └── resource/
│       ├── UsersResource.java         -- GET /users (search), POST not used (lazy signup)
│       ├── UserResource.java          -- GET/PUT /users/:id
│       ├── TeachersResource.java      -- GET /teachers
│       ├── GrantTeacherRoleResource.java  -- POST /grant-teacher-role
│       ├── OffersResource.java        -- POST /offers, GET /offers
│       ├── AcceptOfferResource.java   -- PUT /offers/:id/accept
│       ├── FollowersResource.java     -- GET /users/:id/followers
│       ├── UserJsonDecoders.java
│       └── UserJsonEncoders.java
├── avatar/
│   ├── data/
│   │   └── UserAvatar.java            -- record (UserId, byte[])
│   ├── behavior/
│   │   └── EnsureAvatar.java          -- find or generate
│   ├── dao/
│   │   └── UserAvatarDao.java
│   ├── image/
│   │   └── EightBitAvatarGenerator.java  -- com.talanlabs avatar-generator wrapper
│   └── resource/
│       └── AvatarResource.java        -- GET /users/:id/avatar (image/png)
└── notification/
    ├── data/
    │   ├── WhatsNewId.java
    │   ├── WhatsNew.java              -- record (id, userId, templatePath, params, postedAt)
    │   ├── TemplatePath.java          -- record (1..100)
    │   └── UnreadWhatsNew.java        -- record (id, whatsNewId)
    ├── behavior/
    │   ├── RecordWhatsNew.java        -- subscribe to events, persist WhatsNew + UnreadWhatsNew
    │   └── MarkAsRead.java
    ├── dao/
    │   └── WhatsNewDao.java
    └── resource/
        ├── WhatsNewsResource.java     -- GET /whats-news, PUT /whats-news/:id/read
        └── WhatsNewJsonEncoders.java
```

加えて Lesson 側に `behavior/ListFollowerAnswers.java` + `resource/FollowerAnswersResource.java` を追加 (Sub-B 完了条件で Sub-C 延期と書いた分)。Lesson の `SubmitAnswer` には `KysymysEventBus.publish(...)` の呼び出しを追加する。

## DDL 変更

### V1 編集 (Sub-B と同じ温度感、再修正)

| テーブル/カラム | 旧 | 新 | 理由 |
|---|---|---|---|
| `users.id` | `VARCHAR(255)` | `VARCHAR(21)` | nanoid 統一 |
| `users.password` | `VARCHAR(255)` | (変更なし、未使用) | Bouncr 責務 |
| `connections.followee_id` `follower_id` | `VARCHAR(255)` | `VARCHAR(21)` | nanoid 統一 |
| `offers.id` `offering_user_id` `target_user_id` | `VARCHAR(255)` | `VARCHAR(21)` | nanoid 統一 |
| `user_avatars.user_id` | `VARCHAR(255)` | `VARCHAR(21)` | nanoid 統一 |
| `user_roles.user_id` | `VARCHAR(255)` | `VARCHAR(21)` | nanoid 統一 |
| `whats_news.id` `user_id` | `VARCHAR(255)` | `VARCHAR(21)` | nanoid 統一 |

V2 (`unread_whats_news`) も:

| テーブル/カラム | 旧 | 新 |
|---|---|---|
| `unread_whats_news.id` `whats_new_id` | `VARCHAR(255)` | `VARCHAR(21)` |

V2 を編集してよいかは要判断 — Sub-A と同じく Sub-C 段階で本番運用ない前提で V1/V2 を直接編集する。

### V4 マイグレーション (Notification 強化用)

旧 `unread_whats_news` には `user_id` 列が無い (見落とし)。V4 で追加:

```sql
-- unread_whats_news に user_id 列を追加 (旧版にはなく、検索が困難だった)
ALTER TABLE unread_whats_news ADD COLUMN user_id VARCHAR(21);
ALTER TABLE unread_whats_news ADD CONSTRAINT FK_UNREAD_WHATS_NEWS_ON_USER
  FOREIGN KEY (user_id) REFERENCES users (id);
```

## 認可モデル

`Role` enum:

```java
public enum Role {
    STUDENT(Permission.SUBMIT_ANSWER, Permission.POST_COMMENT),
    TEACHER(Permission.CREATE_PROBLEM, Permission.GRANT_TEACHER_ROLE);
}

public enum Permission {
    SUBMIT_ANSWER, POST_COMMENT, CREATE_PROBLEM, GRANT_TEACHER_ROLE
}
```

JWT の `permissions` claim には Role の name 配列 (`["STUDENT", "TEACHER"]`) が入り、`UserPermissionPrincipal.permissions` (Set<String>) として展開される。

`@Decision(ALLOWED)` の判定:

- `POST/PUT/DELETE /problems(:id)`: `permissions.contains("TEACHER")`
- `POST /grant-teacher-role`: `permissions.contains("TEACHER")`
- `PUT /users/:id`: `params.id == caller.userId` (path id と principal が一致)
- `GET/POST /offers`, `GET /problems`, `GET /answers` 等: 認証のみで OK

Sub-B の `ProblemsResource` / `ProblemResource` / `CommentsResource` の `@Decision(ALLOWED)` を更新する必要があり、これは Sub-C のスコープに含める。

## イベント駆動

`KysymysEventBus` インタフェース:

```java
public class KysymysEventBus extends SystemComponent<KysymysEventBus> {
    private final Map<Class<? extends KysymysEvent>, List<Consumer<? extends KysymysEvent>>> subscribers
            = new ConcurrentHashMap<>();

    public <T extends KysymysEvent> void subscribe(Class<T> type, Consumer<T> handler) { ... }
    public <T extends KysymysEvent> void publish(T event) {
        // 同期 dispatch (in-memory 用、別 thread不要)
    }
}
```

cross-context events:

```java
public sealed interface KysymysEvent permits SubmittedAnswerEvent, OfferedToFollowEvent, UserCreatedEvent {
    LocalDateTime occurredAt();
}

public record SubmittedAnswerEvent(
        AnswerId answerId, ProblemId problemId, ProblemName problemName,
        UserId answererId, UserName answererName,
        List<UserId> followers, LocalDateTime occurredAt) implements KysymysEvent {}

public record OfferedToFollowEvent(
        OfferId offerId, UserId offeringUserId, UserName offeringUserName,
        UserId targetUserId, UserName targetUserName, EmailAddress targetEmail,
        LocalDateTime occurredAt) implements KysymysEvent {}

public record UserCreatedEvent(UserId userId, LocalDateTime occurredAt) implements KysymysEvent {}
```

Lesson `SubmitAnswer` を改修: `dsl.transaction(...)` 後に `eventBus.publish(new SubmittedAnswerEvent(...))` する。followers 一覧は behavior 入力に含めず、`SubmitAnswer` から `ConnectionDao.listFollowersOf(answererId)` で問い合わせる (Sub-B の延期事項を Sub-C で解消)。`ConnectionDao` を Lesson 側でも利用する形になるが、ADR 001 の「User以外は UserId のみ参照」と整合させるため `ConnectionDao` の戻り値は `List<UserId>` に限定。

Notification 側は `RecordWhatsNew` という component が起動時に `eventBus.subscribe(SubmittedAnswerEvent.class, this::onSubmitted)` 等を登録する。

## ルーティング

```
# User
GET    /users                          UsersResource (検索: ?q=)
GET    /users/:id                      UserResource (profile)
PUT    /users/:id                      UserResource (自分のみ更新)
GET    /teachers                       TeachersResource
POST   /grant-teacher-role             GrantTeacherRoleResource (TEACHER のみ)
POST   /offers                         OffersResource
GET    /offers                         OffersResource (受信申請一覧)
PUT    /offers/:id/accept              AcceptOfferResource
GET    /users/:id/followers            FollowersResource

# Avatar
GET    /users/:id/avatar               AvatarResource (image/png)

# Notification
GET    /whats-news                     WhatsNewsResource
PUT    /whats-news/:id/read            WhatsNewsResource (mark as read)

# Lesson 拡張
GET    /followers/answers              FollowerAnswersResource (Sub-B 延期分)
```

`/me` (Sub-A) は EnsureUserRegisteredMiddleware を通った後 user 行が存在することを保証する。

## 検証

```bash
# ビルド + 起動
mvn -pl kysymys-app -am clean compile
cd kysymys-app && mvn exec:java &

# JWT 生成 (permissions に TEACHER を含めたバージョン、email/name claim も付与)
TOKEN=$(java /tmp/GenTokenWithRole.java teacher)

# E2E
hurl --variable host=http://localhost:3000 --variable token="$TOKEN" \
  src/test/hurl/{lesson,user,avatar,notification}.hurl

# 単体テスト
mvn -pl kysymys-app test
```

Hurl scenarios (新規):

- `user.hurl`: lazy signup → profile 取得 → profile 更新 → ListUsers 検索 → OfferToFollow → AcceptFollow → ListFollowers
- `avatar.hurl`: avatar 取得 (1回目=生成、2回目=キャッシュ)
- `notification.hurl`: SubmitAnswer 後に WhatsNew が積まれる、MarkAsRead → 既読

## PR 戦略

Sub-C は規模が大きい。レビュー負荷を下げるため、内部 PR を機能単位に分割:

- **Sub-C-1**: V1/V2 編集 + V4 追加 + `events/` package + `KysymysEventBus` + `system/EnsureUserRegisteredMiddleware` + Lesson `SubmitAnswer` の event publish 改修
- **Sub-C-2**: User の `data/` `dao/` `behavior/` (lazy signup, UpdateProfile) + 認可ロジック (`@Decision(ALLOWED)` を Lesson resource にも適用)
- **Sub-C-3**: User の Offer / Follow / GrantTeacherRole + resource + Hurl
- **Sub-C-4**: Avatar (data/behavior/dao/image/resource) + Hurl
- **Sub-C-5**: Notification (data/behavior/dao/resource) + Lesson `ListFollowerAnswers` + Hurl 完成 + ADR 001 の Notification 関係を更新

各 PR は単独でビルド可能・テスト可能 (Sub-A/B 同様、ローカルで連続実装し、最後に1つの大きな PR にまとめるのが現実的かもしれない)。

## 削除前に記録するもの (Sub-C で再実装するテストの仕様メモ)

Sub-A spec で「Sub-C で再実装」と記録した旧テスト:

- `user/domain/UserTest` (空、不要)
- `user/adapter/persistence/UserPersistenceAdapterTest` (`UserPersistenceAdapter#list(query, page)` と `list(query, roles, page)` のクエリ・ロール絞り込み挙動) → Sub-C-2 の `UserDaoTest` で再実装

新規:

- `KysymysEventBusTest`: subscribe → publish → handler 呼び出し、複数 handler の独立性
- `EnsureUserRegisteredMiddlewareTest`: 初出現で insert、2回目で update なし
- `EightBitAvatarGeneratorTest`: 64x64 PNG が生成される
- `UserDaoTest` / `OfferDaoTest` / `ConnectionDao Test` / `WhatsNewDaoTest`: それぞれ典型的な insert/find/list

## 参照する外部リソース (ローカル clone)

- `kotowari-restful` の `example/` (`/Users/kawasima/workspace/kotowari-restful/`)
- `validation-modeling` の `raoh/` (`/Users/kawasima/workspace/validation-modeling/raoh/`)
- `enkan-bouncr` (`/Users/kawasima/workspace/enkan-bouncr/`) — `UserPermissionPrincipal.permissions` の使い方
- 旧 User/Avatar/Notification コード: `git show c4c0093:kysymys-app/src/main/java/net/unit8/kysymys/{user,avatar,notification}/...`
- 既存 ADR: `doc/adr/001..004`
- 旧テスト仕様: `doc/specs/2026-05-03-sub-a-enkan-foundation.md` の付録表
