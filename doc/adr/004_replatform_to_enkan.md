# Spring Boot から Enkan / Kotowari / Raoh / JOOQ / Bouncr への全面置き換え

## Context

`kysymys-app` は Spring Boot 3.0 + JPA + Thymeleaf + Spring Security OAuth2 で構築されていた。技術選定の方針として、kawasima 氏自身が開発しているフレームワーク群 (Enkan / Kotowari / Raoh / Bouncr) のドッグフーディング実例として再構築したい。フロントエンドも Thymeleaf SSR から React/TS SPA に切り替え、API とフロントを完全分離する。

## Decision

`kysymys-app` を以下の構成に **完全書き換え** する。Spring Boot は完全撤去。

| 観点 | 採用技術 |
|---|---|
| Web スタック | Enkan 0.15.0 + Kotowari-restful 0.15.0 (Liberator 型 decision graph) |
| 入力検証 | Raoh 0.5.0 (`raoh-json`) — `MALFORMED` で `JsonDecoder` |
| 永続化 | JOOQ + `enkan-component-jooq` + Flyway。**コード生成は使わず** `field("name", Class)` 手書き DSL |
| HTTP server | Undertow (`enkan-component-undertow`) |
| JSON | Jackson 3 (`enkan-component-jackson` + `jackson-jakarta-rs-json-provider`) |
| エラー応答 | RFC 9457 `application/problem+json` |
| 認証 | Bouncr (`enkan-bouncr` の `BouncrBackend` で `x-bouncr-credential` JWT 検証) |
| フロントエンド | React/TS SPA (API完全分離、Thymeleaf 全廃) |
| パッケージ構成 | 各 Bounded Context 配下に `data/` `behavior/` `dao/` `resource/` |
| Java バージョン | kysymys-app: **Java 25**、kysymys-maven-plugin / kysymys-scorer-java: **Java 17 のまま** |
| DB | 開発・テスト: H2 / 本番: PostgreSQL |
| Dev/本番分離 | Spring profile は持ち込まず、`KysymysDevMain` / `KysymysMain`、`KysymysDevSystemFactory` / `KysymysSystemFactory` をクラスとして分割 |

参照モデルは [kotowari-restful の example](https://github.com/enkan/kotowari-restful) と [validation-modeling](https://github.com/kawasima/validation-modeling) の Raoh 流。

## Consequences

### 維持される設計意図

- ADR 001 の Bounded Context 4分割 (Lesson / User / Avatar / Notification) はそのまま継承する。
- ADR 002 のレイヤー独立方針はむしろ強化される。Spring DI への依存も外れ、ドメインとアプリケーション層から Spring 起源のアノテーション (`@Service` `@Component` `@Autowired` 等) が完全に消える。
- ADR 003 の Long-term イベントパターンは Sub-B 段階で sealed record + JOOQ + 手書き DSL で再表現する。

### 失われる機能と代替

- **Spring Security OAuth2 (GitHub ログイン)**: Bouncr に置き換える。Kysymys 自体は OIDC RP の機能を持たず、`x-bouncr-credential` JWT を検証するだけ。GitHub ログインは Bouncr 側の OIDC IdP に外部認証として連ねる構成。
- **Spring Data JPA の自動実装**: 各 dao は `DSLContext` を使って手書き。
- **`@Transactional`**: `dsl.transaction(cfg -> { ... })` の明示呼び出しに置き換える。
- **Spring の `ApplicationEventPublisher`**: アプリ内イベントは別途設計 (Sub-C で Notification 移行時に確定)。
- **Thymeleaf テンプレート**: 撤去し、フロントは React/TS SPA に分離 (Sub-D)。

### 段階移行

全体は以下の sub-project に分けて進める:

- **Sub-A**: 新スタック土台 + Flyway + Bouncr スタブ + Dev/本番 Main 完成 (本 ADR 作成時点で完了)
- **Sub-B**: Lesson コンテキスト移行
- **Sub-C**: User / Avatar / Notification 移行 (Bouncr 本格接続、SMTP 通知、アバターアップロードを含む)
- **Sub-D**: React/TS フロントエンド新規構築
- **Sub-E**: kysymys-maven-plugin / kysymys-scorer-java の API 追随

Sub-A 時点では Lesson/User/Avatar/Notification の resource / behavior / dao は未実装。Sub-A の dummy resource (`/health` `/me`) のみが動作する。

### Sub-A 完了条件と検証

`doc/specs/2026-05-03-sub-a-enkan-foundation.md` を参照。
