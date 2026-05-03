# Sub-A: Enkan土台構築

**作成日**: 2026-05-03
**対象ブランチ**: `feature/sub-a-enkan-foundation`
**ベース**: `develop`

## Context

Kysymys (`kysymys-app` モジュール) を **Spring Boot 3.0 + JPA + Thymeleaf + Spring Security OAuth2** から **Enkan 0.15.0 + Kotowari-restful 0.15.0 + Raoh 0.5.0 + JOOQ + React/TS + Bouncr** に完全書き換えする。本 spec はその土台構築 (**Sub-A**) を扱う。後続 Sub-B〜E は Sub-A 完了後に別 spec として立ち上げる。

参照モデル:

- [kotowari-restful の example](https://github.com/enkan/kotowari-restful) — `data/behavior/dao/resource` 構成、Raoh ベースの MALFORMED 検証、JOOQ 手書き DSL
- [validation-modeling](https://github.com/kawasima/validation-modeling) — Raoh の sealed interface 階層対応 (`discriminate`)

設計意図 (ADR 維持):

- ADR 001: Bounded Context 4分割 (Lesson / User / Avatar / Notification) は維持
- ADR 002: アプリ層を Web/DB ライブラリから非依存にする方針は維持 (Spring DI も同時に外す)
- ADR 003: Long-term イベントパターンは Sub-B での Lesson 移植時に新スタック (sealed record + JOOQ + 手書き DSL) で再表現

## ゴール

新スタックで `kysymys-app` が起動し、`/health` 相当の dummy resource が JSON を返す。`x-bouncr-credential` ヘッダ (HMAC固定キーで自前署名した JWT) を渡すと認証通過、なければ 401。アプリ起動時に Flyway が既存 `V1__CREATE_INITIAL.sql` を適用し、JOOQ DSL (`field("name", Class)` 手書き) で全テーブルにアクセスできる土台が整う。**Dev / 本番両方の Main が動作確認可能**。Sub-B 以降の resource/behavior/dao 実装が即座に着手できる状態にする。

## 全体方針 (決定済み)

| 観点 | 決定 |
|---|---|
| Web スタック | Enkan 0.15.0 + Kotowari-restful 0.15.0 (Liberator 型 decision graph) |
| 入力検証 | Raoh 0.5.0 (`raoh-json`) — `MALFORMED` で `JsonDecoder` |
| 永続化 | JOOQ + `enkan-component-jooq` + Flyway (`enkan-component-flyway`)。**コード生成は使わず** `field("name", Class)` 手書き DSL |
| HTTP server | Undertow (`enkan-component-undertow`) |
| JSON | Jackson 3 (`enkan-component-jackson`, `tools.jackson`) |
| エラー応答 | RFC 9457 `application/problem+json` |
| 認証 | Bouncr。Sub-A は `enkan-bouncr` の `BouncrBackend` を **HMAC固定キー** で組み込み、自前署名 JWT で疎通確認。本物 Bouncr スタック接続は Sub-C |
| フロントエンド | React/TS SPA。Sub-A では **作らない** (Thymeleaf 削除のみ)。Sub-D で別管理 |
| パッケージ構成 | `data/` `behavior/` `dao/` `resource/` + `inject/` `*Main` `*ApplicationFactory` `*SystemFactory` |
| Java バージョン | kysymys-app: **Java 25**、kysymys-maven-plugin / kysymys-scorer-java: **Java 17 のまま** (JDK 25 一本、`--release 17` で外部互換) |
| モジュール配置 | 既存 `kysymys-app/` を **完全書き換え**。Spring Boot 版は残さない |
| DB | 開発・テスト: H2 / 本番: PostgreSQL |
| Flyway 起点 | 既存 `V1__CREATE_INITIAL.sql` をそのまま継承 |
| Spring profile の代替 | profile 機構は持ち込まず、**Dev 用と本番用で Main / SystemFactory クラスを分割** |

## 完了条件

1. `kysymys-app/pom.xml` から Spring Boot 系依存をすべて削除し、Enkan 0.15.0 + Kotowari-restful + Raoh + JOOQ + Flyway + Bouncr 依存に置き換わる。
2. **Dev 起動**: `cd kysymys-app && mvn compile exec:java -Dexec.mainClass=net.unit8.kysymys.KysymysDevMain` で Undertow が起動し、`http://localhost:3000/health` が `200 OK` で `{"status":"ok","userCount":0}` を返す (`userCount` は users テーブルの row count、Flyway 適用と JOOQ 接続を同時に検証)。
3. **認証疎通**: `x-bouncr-credential` ヘッダなしで `/me` が `401`、固定HMACキーで自前署名した JWT を付けると `200` で principal の `sub` クレームを JSON で返す。
4. **旧コード削除**: `kysymys-app/src/main/java/net/unit8/kysymys/` 配下の旧 Spring Boot コード (`avatar/` `lesson/` `notification/` `user/` `web/` `config/` `share/` `stereotype/` および `App.java`) と `src/test/java/` 配下の既存7テストはすべて削除されている。
5. **設定ファイル整理**: `application.yml` 系の Spring 設定ファイルは削除されている。
6. **テンプレート削除**: `kysymys-app/src/main/resources/templates/` (Thymeleaf) と `static/` は削除されている。
7. **本番 Main 動作確認**: `kysymys-app/docker-compose.yml` で PostgreSQL 16 を起動し、`KYSYMYS_DB_URL=jdbc:postgresql://localhost:5432/kysymys` 等の環境変数を渡して `KysymysMain` を起動すると、Dev と同じ `/health` `/me` が動作する。
8. **外部モジュール無変更**: `kysymys-maven-plugin` と `kysymys-scorer-java` は触らず、既存の Java 17 ビルドが通る (`mvn -pl kysymys-maven-plugin,kysymys-scorer-java -am test` 成功)。
9. **ADR 更新**: `doc/adr/004_replatform_to_enkan.md` を新規追加し、`doc/adr/002_layer.md` を新スタック方針に合わせて更新。
10. **CLAUDE.md 新規**: リポジトリルートに CLAUDE.md を新規作成。Java 25 / Enkan / Kotowari / Raoh / JOOQ / Bouncr の構成、ビルドコマンド、`data/behavior/dao/resource` 構成、PR ワークフローを記載。

## 含めない (後続 Sub に回す)

- Lesson/User/Avatar/Notification の resource / behavior / dao 実装 (Sub-B, Sub-C)
- 本物の Bouncr スタック (bouncr-api-server + bouncr-proxy + Envoy) 接続 (Sub-C)
- React/TS フロント (Sub-D)
- メール通知 (Notification) の SMTP 置き換え (Sub-C)
- マルチパート / アバター画像アップロード (Sub-C)
- kysymys-maven-plugin / kysymys-scorer-java の API 追随 (Sub-E)
- 自動テスト整備 (Sub-B 以降の各機能移植時に dao / behavior 単位で書く)

## 主要決定事項

### モジュール命名

- `kysymys-app/` のまま (リネームなし)
- 中身は完全書き換え (旧 Spring Boot 版は残さない)

### 新ディレクトリ構造

```
kysymys-app/src/main/java/net/unit8/kysymys/
├── KysymysDevMain.java               -- 開発用 entry point (H2 in-mem 固定)
├── KysymysMain.java                  -- 本番 entry point (環境変数読み込み)
├── KysymysDevSystemFactory.java      -- 開発用 component wiring
├── KysymysSystemFactory.java         -- 本番 component wiring
├── KysymysApplicationFactory.java    -- middleware stack (Dev/本番共通)
├── inject/
│   └── DSLContextInjector.java       -- DSLContext を @Decision に注入
└── health/
    ├── HealthResource.java           -- `/health`
    └── MeResource.java               -- `/me` (Bouncr 認証疎通用)
```

### Maven / 依存

`kysymys-app/pom.xml` の依存 (主要):

```xml
<properties>
  <java.version>25</java.version>
  <enkan.version>0.15.0</enkan.version>
  <jackson.version>3.1.0</jackson.version>
  <raoh.version>0.5.0</raoh.version>
</properties>

<dependencies>
  <!-- Enkan / Kotowari -->
  <dependency><groupId>net.unit8.enkan</groupId><artifactId>kotowari-restful</artifactId><version>0.15.0</version></dependency>
  <dependency><groupId>net.unit8.enkan</groupId><artifactId>kotowari-restful-devel</artifactId><version>0.15.0</version></dependency>
  <dependency><groupId>net.unit8.enkan</groupId><artifactId>enkan-devel</artifactId><version>${enkan.version}</version></dependency>
  <dependency><groupId>net.unit8.enkan</groupId><artifactId>enkan-component-flyway</artifactId><version>${enkan.version}</version></dependency>
  <dependency><groupId>net.unit8.enkan</groupId><artifactId>enkan-component-jackson</artifactId><version>${enkan.version}</version></dependency>
  <dependency><groupId>net.unit8.enkan</groupId><artifactId>enkan-component-HikariCP</artifactId><version>${enkan.version}</version></dependency>
  <dependency><groupId>net.unit8.enkan</groupId><artifactId>enkan-component-undertow</artifactId><version>${enkan.version}</version></dependency>
  <dependency><groupId>net.unit8.enkan</groupId><artifactId>enkan-component-jooq</artifactId><version>${enkan.version}</version></dependency>
  <!-- Raoh -->
  <dependency><groupId>net.unit8.raoh</groupId><artifactId>raoh-json</artifactId><version>${raoh.version}</version></dependency>
  <dependency><groupId>net.unit8.raoh</groupId><artifactId>raoh-jooq</artifactId><version>${raoh.version}</version></dependency>
  <!-- Bouncr (groupId/version は実装時に /Users/kawasima/workspace/enkan-bouncr/pom.xml から取得) -->
  <dependency><groupId>TBD</groupId><artifactId>enkan-bouncr</artifactId><version>TBD</version></dependency>
  <!-- DB drivers -->
  <dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId><scope>runtime</scope></dependency>
  <dependency><groupId>org.postgresql</groupId><artifactId>postgresql</artifactId><scope>runtime</scope></dependency>
  <!-- Logging -->
  <dependency><groupId>org.slf4j</groupId><artifactId>slf4j-simple</artifactId><scope>runtime</scope></dependency>
  <!-- Test -->
  <dependency><groupId>org.junit.jupiter</groupId><artifactId>junit-jupiter-api</artifactId><scope>test</scope></dependency>
  <dependency><groupId>org.assertj</groupId><artifactId>assertj-core</artifactId><scope>test</scope></dependency>
</dependencies>
```

`enkan-bouncr` の正確な `groupId` / `version` は実装時に `/Users/kawasima/workspace/enkan-bouncr/pom.xml` から取得する。

parent `pom.xml` は Spring Boot BOM import を撤去し、`<release>` プロパティ指定は廃止 (各子モジュールで個別指定)。

### Flyway 配置

- 既存と同じ `kysymys-app/src/main/resources/db/migration/V1__CREATE_INITIAL.sql` (移動しない)
- アプリ起動時に `enkan-component-flyway` が自動適用

### ポート / 環境変数

- HTTP: `3000` (kotowari-restful example と同じ)
- JWT 検証: `BouncrBackend` を `HS256` HMAC で構成。鍵は `KYSYMYS_JWT_SECRET` 環境変数 (dev デフォルトは固定文字列、本番は必須)
- 環境変数 (本番): `KYSYMYS_DB_URL`, `KYSYMYS_DB_USER`, `KYSYMYS_DB_PASSWORD`, `KYSYMYS_JWT_SECRET`

### docker-compose (本番動作確認用)

`kysymys-app/docker-compose.yml`:

- `postgres:16` (port 5432, db: kysymys, user/password 固定)
- ボリュームは無名でよい (検証目的のみ)

### PR

単一 PR `feature/sub-a-enkan-foundation` を `develop` に向けて作成。

## 検証

```bash
cd /Users/kawasima/workspace/kysymys

# 1. Maven ビルド
mvn -pl kysymys-app -am clean compile

# 2. Dev 起動
cd kysymys-app && mvn exec:java -Dexec.mainClass=net.unit8.kysymys.KysymysDevMain &

# 3. Health
curl -s http://localhost:3000/health
# expect: {"status":"ok","userCount":0}

# 4. 認証なしで 401
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:3000/me
# expect: 401

# 5. 認証付きで 200
TOKEN=...   # KYSYMYS_JWT_SECRET 既定値で HS256 署名した JWT (sub=alice)
curl -s -H "x-bouncr-credential: $TOKEN" http://localhost:3000/me
# expect: {"sub":"alice"}

# 6. 旧 Spring Boot コードが消えている
test -z "$(rg -l '@SpringBootApplication|@RestController|@Service|@Autowired' kysymys-app/src/)"

# 7. 外部モジュールは触っていない
mvn -pl kysymys-maven-plugin,kysymys-scorer-java -am test

# 8. 本番 Main を Docker Compose の PostgreSQL に対して起動
cd kysymys-app && docker compose up -d
KYSYMYS_DB_URL=jdbc:postgresql://localhost:5432/kysymys \
KYSYMYS_DB_USER=kysymys \
KYSYMYS_DB_PASSWORD=kysymys \
KYSYMYS_JWT_SECRET=devsecret \
mvn exec:java -Dexec.mainClass=net.unit8.kysymys.KysymysMain
# Dev と同じ /health /me が動作する
```

## 削除前に記録するもの (Sub-B/C で再実装するテストの仕様メモ)

旧コードと旧テストは git 履歴で参照可能だが、再実装時に意図を見失わないよう、削除前に**テスト名と検証意図のメモ**を以下に残す。

| 旧テスト | 検証意図 | 移植先 (予定) |
|---|---|---|
| `user/domain/UserTest` | 空 (実装なし) | 不要 |
| `lesson/domain/RepositoryUrlBuilderTest` | GitHub URL builder の不正系 (branch / commit hash 同時指定不可、branch名の `..` `/` 末尾エラー) と正常系 (branch のみ → `/blob/<branch>`、commit hash のみ → `/tree/<hash>`、path 連結) | Sub-B Lesson 移植時に同等のテストを Raoh デコーダ + record で書き直し |
| `user/adapter/persistence/UserPersistenceAdapterTest` | `UserPersistenceAdapter#list(query, page)` と `list(query, roles, page)` のクエリ・ロール絞り込み挙動 (DataJpaTest) | Sub-C User 移植時に dao 層テスト (Testcontainers or H2) で書き直し |
| `lesson/adapter/persistence/AnswerPersistenceAdapterTest` | Answer 保存時に同一 answerId への複数 submission が正しく蓄積される (`answers` 1件 + `submissions` 2件) | Sub-B Lesson 移植時に dao 層テストで書き直し |
| `lesson/application/impl/SubmitAnswerUseCaseImplTest` | `SubmitAnswerUseCaseImpl#handle` で `saveAnswerPort.save` が呼ばれる (Mockito) | Sub-B `behavior/SubmitAnswer` の純関数テストとして書き直し |
| `lesson/application/impl/CreateProblemUseCaseImplTest` | `CreateProblemUseCaseImpl#handle` で `saveProblemPort.save` が呼ばれる (Mockito) | Sub-B `behavior/CreateProblem` の純関数テストとして書き直し |
| `share/adapter/system/FlakeAdapterTest` | `FlakeAdapter#generateId` で 10000 個生成し、ソート済み (時間単調増加) であること | Sub-B (Lesson移植時) で ID 生成戦略を決めるとき再評価。`FlakeAdapter` を移植するなら同等テスト要、jnanoid に統一するなら不要 |

## 参照する外部リソース (ローカル clone)

- [kotowari-restful README + example](https://github.com/enkan/kotowari-restful) — `/Users/kawasima/workspace/kotowari-restful/`
- [validation-modeling raoh/](https://github.com/kawasima/validation-modeling) — `/Users/kawasima/workspace/validation-modeling/raoh/`
- [enkan-bouncr README](https://github.com/kawasima/enkan-bouncr) — `/Users/kawasima/workspace/enkan-bouncr/`
- [Bouncr README + SPECIFICATION](https://github.com/kawasima/bouncr) — `/Users/kawasima/workspace/bouncr/`
- 既存 ADR: `kysymys/doc/adr/001..003`
- 既存 DDL: `kysymys/kysymys-app/src/main/resources/db/migration/V1__CREATE_INITIAL.sql`
