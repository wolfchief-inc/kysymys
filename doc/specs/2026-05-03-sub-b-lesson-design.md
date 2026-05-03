# Sub-B: Lesson コンテキスト移行

**作成日**: 2026-05-03
**対象ブランチ**: `feature/sub-b-lesson`
**ベース**: `develop` (Sub-A 完了済み)

## Context

Sub-A (Enkan土台) 完了後、最初の Bounded Context として **Lesson** を新スタックで完全実装する。Lesson は Kysymys の Core コンテキスト (ADR 001) で、Problem (問題) を中心に Answer (解答) と ReviewComment (コメント) が連なる。

参照モデル:

- `kotowari-restful` の `example/` (`data/behavior/dao/resource` 構成、JOOQ手書き DSL)
- `validation-modeling` の `raoh/joboffer` (sealed interface + Raoh `discriminate`)
- ADR 003 (Long-term イベントパターン) を完全実装する

「仕様を見直しつつ移植」の方針で、旧 Lesson コードの設計の歪み (ordinal 永続化、`latest_submissions` の PK ミス、`Answer` への commitHash 二重保持など) は是正しつつ、ビジネスルールは継承する。

## ゴール

新スタックで Lesson コンテキストの主要ユースケース10個 (CreateProblem / UpdateProblem / ArchiveProblem / GetProblem / ListProblems / SubmitAnswer / ShowAnswer / ListMyAnswers / PostComment + ListComments同梱) が動作する。`/problems`, `/answers`, `/problems/:id/answers`, `/answers/:id/comments` などのエンドポイントが Bouncr 認証付きで JSON API として動く。各 dao / behavior には JUnit テストを書く。

## 全体方針 (確定済み決定事項)

| 観点 | 決定 |
|---|---|
| Sub-B スコープ | Lesson 全体を1 spec、PR は機能単位で分割可 |
| ProblemRepository / AnswerRepository | sealed interface (`GitHub` / `BitBucket` / `Generic`) + Raoh `discriminate` |
| RepositoryUrlBuilder | sealed の各 record の振る舞いとして実装 (旧版の interface + 3 subclass を sealed record + メソッドに統合) |
| ProblemLifecycle / ProblemEvent | ADR 003 完全実装。V3 マイグレーションで `problem_updated_events`, `problem_archived_events` を追加し、3 イベント全部を永続化 |
| ProblemStatus | enum `ACTIVE` / `ARCHIVED` (過去形に統一)。永続化を `INTEGER` から `VARCHAR(20)` に変更 (V1 編集) |
| Answer / Submission | 最新 commitHash は Submission (`latest_submissions` 経由) に集約。`Answer` 値オブジェクトから commitHash を分離。`latest_submissions` PK を `(answer_id)` に修正 (V1 編集) |
| ReviewComment | `description` を `VARCHAR(4000)` に拡張 (V1 編集、ドメインの `Description` 値オブジェクト制約に合わせる) |
| ID 列の型 | nanoid 21 桁で統一。V1 の `VARCHAR(255)` を `VARCHAR(21)` に揃える (V1 編集) |
| UserId | `net.unit8.kysymys.user.data.UserId` を Sub-B で先取り定義 (record `UserId(String value)` のみ。User context 全体は Sub-C) |
| ListFollowerAnswers ユースケース | Sub-C まで延期 (フォロー関係参照が必要なため) |
| Principal → UserId | `UserIdInjector` を `KysymysApplicationFactory` の `parameterInjectors` に追加 |
| エラー応答 | RFC 9457 `Problem` で統一。旧 `*NotFoundException` は新スタックでは `Result.fail(...)` または `@Decision(EXISTS) return false` (404) で表現 |
| Authorization | `@Decision(AUTHORIZED)` で principal の有無のみ確認。role 認可 (`先生`/`受講者`) は Sub-C で `permissions` クレーム経由 |
| ID 生成 | jnanoid (21 桁) で統一。旧版の `FlakeAdapter` は撤去 |

## 完了条件

1. Lesson 関連の全テーブルがアプリ起動時に Flyway で適用され、JOOQ DSL でアクセスできる。
2. **Problem CRUD**: `POST /problems`, `GET /problems`, `GET /problems/:id`, `PUT /problems/:id`, `DELETE /problems/:id` が動作。それぞれ `Problem*Event` を `problem_*_events` に永続化する。
3. **Answer**: `POST /problems/:id/answers` で submission 履歴を積み (`submissions` + `latest_submissions` を transaction で更新)、`GET /answers`, `GET /answers/:id` が動作。
4. **Comment**: `POST /answers/:id/comments` でコメント投稿、`GET /answers/:id` で answer + comments を JSON で返す。
5. すべての書き込みエンドポイントは Bouncr 認証必須。`x-bouncr-credential` の `sub` クレームから `UserId` を取り出し、`answerer_id` / `commenter_id` / Event の `creator_id` 等に使う。
6. dao 層と behavior 層に JUnit テスト (H2 in-mem、Mockito 不要、record の純関数テスト) を書く。
7. E2E は `kysymys-app/src/test/hurl/lesson.hurl` で 1 シナリオ (Problem 作成 → Answer 提出 → Comment 投稿 → 取得) を通す。
8. ADR 003 のリンク切れ修正 (旧 `ProblemJpaEntity` 等への参照を新クラス名に差し替え)。

## 含めない (後続 Sub に回す)

- ListFollowerAnswers (Sub-C: User context のフォロー関係が必要)
- Role 認可 (`先生`/`受講者` の権限分離) — Sub-C
- Avatar / Notification / SMTP — Sub-C
- React フロント — Sub-D
- kysymys-maven-plugin の API 追随 (旧 `SubmitMojo` は旧 API 互換) — Sub-E

## ディレクトリ構造

Sub-A の `health/` `inject/` の隣に Lesson コンテキストを追加:

```
kysymys-app/src/main/java/net/unit8/kysymys/
├── (Sub-A 既存) Kysymys*Main, *SystemFactory, KysymysApplicationFactory, inject/, health/
├── lesson/
│   ├── data/                          -- record / sealed interface
│   │   ├── ProblemId.java             -- record (nanoid 21桁)
│   │   ├── ProblemName.java           -- record (1〜100 文字)
│   │   ├── ProblemRepository.java     -- sealed interface, permits GitHub/BitBucket/Generic
│   │   ├── GitHubProblemRepository.java   -- record (url, branch, readmePath)
│   │   ├── BitBucketProblemRepository.java
│   │   ├── GenericProblemRepository.java  -- (url, branch) のみ、readmePath なし
│   │   ├── Problem.java               -- record (id, name, repository, lifecycleId)
│   │   ├── ProblemStatus.java         -- enum ACTIVE / ARCHIVED
│   │   ├── ProblemLifecycle.java      -- record (id, problemId, status)
│   │   ├── ProblemEvent.java          -- sealed interface, permits Created/Updated/Archived
│   │   ├── ProblemCreatedEvent.java   -- record (id, lifecycleId, occurredAt, creatorId)
│   │   ├── ProblemUpdatedEvent.java   -- record (id, lifecycleId, occurredAt, updaterId)
│   │   ├── ProblemArchivedEvent.java  -- record (id, lifecycleId, occurredAt, archiverId)
│   │   ├── AnswerId.java              -- record
│   │   ├── AnswerRepository.java      -- sealed interface
│   │   ├── GitHubAnswerRepository.java        -- record (url)
│   │   ├── BitBucketAnswerRepository.java
│   │   ├── GenericAnswerRepository.java
│   │   ├── Answer.java                -- record (id, problemId, answererId, repository, lastAnsweredAt)
│   │   ├── SubmissionId.java
│   │   ├── Submission.java            -- record (id, answerId, commitHash, submittedAt)
│   │   ├── CommitHash.java            -- record (40 桁 HEX)
│   │   ├── CommentId.java
│   │   ├── Description.java           -- record (1〜4000 文字)
│   │   └── ReviewComment.java         -- record (id, answerId, commenterId, description, postedAt)
│   ├── behavior/                      -- ユースケース。DB 副作用あるものは Function<In, Result<Out>> + dao コンストラクタ注入、参照系は dao 直呼びで resource から
│   │   ├── CreateProblem.java         -- 入力検証 + Problem/Lifecycle/CreatedEvent の3点 insert を transaction で
│   │   ├── UpdateProblem.java         -- Problem load → 検証 → update + UpdatedEvent insert
│   │   ├── ArchiveProblem.java        -- 旧 DeleteProblem 相当。Lifecycle.status を ARCHIVED に + ArchivedEvent insert
│   │   ├── SubmitAnswer.java          -- Problem 検証 + Answer upsert + Submission insert + latest_submissions upsert
│   │   └── PostComment.java           -- Answer 存在確認 + ReviewComment insert
│   --  GetProblem / ListProblems / ShowAnswer / ListMyAnswers は behavior を切らず、resource から dao 直呼びで十分 (純参照のため)
│   ├── dao/                           -- すべて concrete class (interface は切らない)
│   │   ├── ProblemDao.java            -- DSLContext をコンストラクタ注入。behavior から `new ProblemDao(dsl)` で使う
│   │   ├── ProblemMapper.java         -- jOOQ Record -> Problem record (raoh-jooq の JooqRecordDecoder)
│   │   ├── ProblemEventDao.java
│   │   ├── AnswerDao.java
│   │   ├── SubmissionDao.java
│   │   └── ReviewCommentDao.java
│   └── resource/
│       ├── ProblemsResource.java        -- POST /problems, GET /problems
│       ├── ProblemResource.java         -- GET/PUT/DELETE /problems/:id
│       ├── AnswersResource.java         -- POST /problems/:id/answers
│       ├── AnswerResource.java          -- GET /answers/:id
│       ├── MyAnswersResource.java       -- GET /answers (caller's own)
│       ├── CommentsResource.java        -- POST /answers/:id/comments
│       ├── ProblemJsonDecoders.java     -- Raoh
│       ├── ProblemJsonEncoders.java
│       ├── AnswerJsonDecoders.java
│       ├── AnswerJsonEncoders.java      -- Answer + comments 同梱
│       └── CommentJsonDecoders.java
└── (Sub-B で先取り) user/data/UserId.java
```

`dao` 層のクラス名 `ProblemRepository` は `data/ProblemRepository` (sealed interface) と紛らわしいので、dao 側は `ProblemDao` / `AnswerDao` / `ReviewCommentDao` に統一する (kotowari-restful example も `CustomerRepository` だが、Kysymys では値オブジェクトに `Repository` を使っているので `Dao` に寄せる)。

## sealed interface の入力例

リクエスト JSON (POST /problems):

```json
{
  "name": "FizzBuzz",
  "repository": {
    "type": "github",
    "url": "https://github.com/foo/fizzbuzz",
    "branch": "main",
    "readmePath": "/README.md"
  }
}
```

```json
{
  "name": "Apache Kafka 入門",
  "repository": {
    "type": "generic",
    "url": "https://example.com/lessons/kafka",
    "branch": "main"
  }
}
```

Raoh `JsonDecoders.ProblemJsonDecoders`:

```java
private static final JsonDecoder<ProblemRepository> REPOSITORY_DECODER = discriminate("type", Map.of(
    "github", combine(
        field("url", string().url().maxLength(255)),
        field("branch", string().pattern(BRANCH_PATTERN).maxLength(100)),
        field("readmePath", optional(string().maxLength(100)).map(o -> o.orElse("/README.md")))
    ).map(GitHubProblemRepository::new),
    "bitbucket", ...,
    "generic", combine(
        field("url", string().url().maxLength(255)),
        field("branch", string().pattern(BRANCH_PATTERN).maxLength(100))
    ).map(GenericProblemRepository::new)
));

public static final JsonDecoder<CreateProblemInput> CREATE_DECODER = combine(
    field("name", string().minLength(1).maxLength(100).map(ProblemName::new)),
    field("repository", REPOSITORY_DECODER)
).map(CreateProblemInput::new);
```

Submission の入力 (POST /problems/:id/answers):

```json
{
  "repository": { "type": "github", "url": "https://github.com/foo/fizzbuzz-mine" },
  "commitHash": "0123456789012345678901234567890123456789"
}
```

## DDL 変更 (V1 編集 + V3 追加)

### V1 編集 (Sub-A の `BLOB→BYTEA` と同じ温度感)

具体的な編集箇所は以下の通り。Sub-B-1 PR で V1 を直接編集する (V1 は Sub-A 段階で本番運用に乗っていない)。

| テーブル/カラム | 旧 | 新 | 理由 |
|---|---|---|---|
| `answers.id` `answers.problem_id` | `VARCHAR(21)` | (変更なし) | nanoid 21桁 |
| `answers.answerer_id` | `VARCHAR(255)` | `VARCHAR(21)` | UserId も nanoid 21桁に統一 |
| `problems.id` | `VARCHAR(21)` | (変更なし) | |
| `problems.problem_lifecycle_id` | `VARCHAR(255)` | `VARCHAR(21)` | nanoid 統一 |
| `problem_lifecycles.id` `problem_id` | `VARCHAR(255)` | `VARCHAR(21)` | nanoid 統一 |
| `problem_lifecycles.status` | `INTEGER NOT NULL` | `VARCHAR(20) NOT NULL` | enum 文字列永続化 |
| `problem_created_events.id` `problem_lifecycle_id` `creator_id` | `VARCHAR(255)` | `VARCHAR(21)` | nanoid 統一 |
| `submissions.id` | `VARCHAR(255)` | `VARCHAR(21)` | nanoid 統一 |
| `submissions.answer_id` `commit_hash` | `VARCHAR(21)` / `VARCHAR(255)` | `VARCHAR(21)` / `VARCHAR(40)` | commit hash は 40 桁固定 |
| `latest_submissions` PK | `(answer_id, submission_id)` | `(answer_id)` | 「最新 1 件」の一意性表明 |
| `latest_submissions.submission_id` | `VARCHAR(255)` | `VARCHAR(21) NOT NULL` | nanoid 統一 + NOT NULL |
| `review_comments.id` `commenter_id` | `VARCHAR(255)` | `VARCHAR(21)` | nanoid 統一 |
| `review_comments.description` | `VARCHAR(255) NOT NULL` | `VARCHAR(4000) NOT NULL` | ドメインの `Description` 制約に合わせる |

User / Avatar / Notification 関連テーブルの id 列は Sub-C で揃える (Sub-B のスコープ外)。

### V3 マイグレーション (新規)

`V3__add_problem_event_tables.sql`:

```sql
CREATE TABLE problem_updated_events (
  id VARCHAR(21) NOT NULL,
  problem_lifecycle_id VARCHAR(21),
  occurred_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updater_id VARCHAR(21) NOT NULL,
  CONSTRAINT pk_problem_updated_events PRIMARY KEY (id)
);

CREATE TABLE problem_archived_events (
  id VARCHAR(21) NOT NULL,
  problem_lifecycle_id VARCHAR(21),
  occurred_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  archiver_id VARCHAR(21) NOT NULL,
  CONSTRAINT pk_problem_archived_events PRIMARY KEY (id)
);

ALTER TABLE problem_updated_events ADD CONSTRAINT FK_PROBLEM_UPDATED_EVENTS_ON_PROBLEM_LIFECYCLE
  FOREIGN KEY (problem_lifecycle_id) REFERENCES problem_lifecycles (id);

ALTER TABLE problem_archived_events ADD CONSTRAINT FK_PROBLEM_ARCHIVED_EVENTS_ON_PROBLEM_LIFECYCLE
  FOREIGN KEY (problem_lifecycle_id) REFERENCES problem_lifecycles (id);
```

## ルーティング

```
GET    /problems                  ListProblemsResource
POST   /problems                  ProblemsResource (CreateProblem)
GET    /problems/:id              ProblemResource (GetProblem)
PUT    /problems/:id              ProblemResource (UpdateProblem)
DELETE /problems/:id              ProblemResource (Archive、200 OK + 状態返却)
POST   /problems/:id/answers      AnswersResource (SubmitAnswer)
GET    /answers                   MyAnswersResource (caller の解答一覧)
GET    /answers/:id               AnswerResource (Answer + Comments)
POST   /answers/:id/comments      CommentsResource (PostComment)
```

## 認可

Sub-B では `@Decision(AUTHORIZED)` で **principal 有無のみ確認**。role 別認可は Sub-C で:

- 「先生」のみ Problem CRUD 可能 → Sub-C で `permissions` クレームから判定
- Answer / Comment は認証済みユーザなら誰でも投稿可

## 検証

```bash
# 1. ビルド + 起動
mvn -pl kysymys-app -am clean compile
cd kysymys-app && mvn exec:java &

# 2. JWT 生成 (Sub-A の手順を再利用)
TOKEN=$(java /tmp/GenToken.java)

# 3. Hurl で E2E
hurl --variable host=http://localhost:3000 --variable token="$TOKEN" \
  src/test/hurl/lesson.hurl

# 4. 単体テスト
mvn -pl kysymys-app test
```

`lesson.hurl` のシナリオ:

1. POST /problems (FizzBuzz, github type)
2. GET /problems → 1 件返る
3. GET /problems/:id → 200 + 詳細
4. POST /problems/:id/answers (commit hash A)
5. POST /problems/:id/answers (同じ answerer、commit hash B)
6. GET /answers → 1 件 (latest = B)
7. GET /answers/:id → answer + 0 comments
8. POST /answers/:id/comments (description "Looks good")
9. GET /answers/:id → answer + 1 comment
10. PUT /problems/:id (name 更新) → 200, problem_updated_events に1行
11. DELETE /problems/:id → 200, status=ARCHIVED, problem_archived_events に1行

## 含めるテストの仕様

| テスト | 内容 |
|---|---|
| `ProblemDaoTest` | Problem の save/find/list、Lifecycle と Event の同時 insert (transaction) |
| `AnswerDaoTest` | 同一 (problemId, answererId) で複数 submit → answers 1件 + submissions 2件 + latest_submissions 1件 (旧 `AnswerPersistenceAdapterTest` の意図を踏襲) |
| `ReviewCommentDaoTest` | answerId 単位の comment 一覧取得 |
| `RepositoryUrlBuilderTest` (record メソッド版) | GitHub: branch 単独 / commitHash 単独 / 両方指定エラー / branch名の git 規則違反 (旧 `RepositoryUrlBuilderTest` の意図を踏襲) |
| `CreateProblemTest` | behavior の純関数テスト (旧 `CreateProblemUseCaseImplTest` の意図) |
| `SubmitAnswerTest` | behavior の純関数テスト (旧 `SubmitAnswerUseCaseImplTest` の意図) |

## PR 戦略

Sub-B は Lesson 全体で 30〜50 ファイル規模になる見込み。レビュー負荷を下げるため、内部 PR を機能単位に分割:

- **Sub-B-1**: V1 編集 + V3 追加 + UserId 先取り + Lesson `data/` 一式 (record + sealed のみ、ロジックなし)
- **Sub-B-2**: Lesson `dao/` (ProblemDao / ProblemEventDao を含む) + dao テスト
- **Sub-B-3**: Problem の behavior + resource (Create/Update/Archive/Get/List) + Hurl シナリオ部分
- **Sub-B-4**: Answer / Submission の behavior + resource + Hurl 拡張
- **Sub-B-5**: ReviewComment + AnswerResource の comments 同梱 + Hurl 完成

各 PR は単独でビルド可能・テスト可能であること。前段 PR がマージされてから次段に着手する。

## 参照する外部リソース

- `kotowari-restful` の `example/` (`/Users/kawasima/workspace/kotowari-restful/`)
- `validation-modeling` の `raoh/joboffer` (sealed + discriminate、`/Users/kawasima/workspace/validation-modeling/raoh/`)
- 既存 ADR: `doc/adr/001..004`
- 旧 Lesson コード: `git show c4c0093:kysymys-app/src/main/java/net/unit8/kysymys/lesson/...` (Sub-A merge 直前の develop)
- 旧 Lesson テスト仕様: `doc/specs/2026-05-03-sub-a-enkan-foundation.md` の「削除前に記録するもの」表
