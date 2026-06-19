# CLAUDE.md

このファイルは、Claude Code (claude.ai/code) がこのリポジトリで作業するときの指針です。

## リポジトリの目的

Kysymys はプログラミング学習プラットフォームです。kawasima 氏自身が開発しているフレームワーク群 (Enkan / Kotowari / Raoh / Bouncr) のドッグフーディング実例として再構築されています。

## 構成

Maven マルチモジュール:

| モジュール | 役割 | Java |
|---|---|---|
| `kysymys-app` | バックエンド本体 (Enkan / Kotowari-restful / Raoh / JOOQ / Bouncr) | 25 |
| `kysymys-maven-plugin` | 解答提出用の Maven プラグイン (利用者の手元で動く) | 17 |
| `kysymys-scorer-java` | Java 解答の採点ロジック | 17 |
| `kysymys-activity-agent` | 作業状況テレメトリの Maven 拡張 (Go バイナリをサーバから DL して起動) | 17 |

加えて Maven モジュールではない Go プロジェクト `kysymys-agent/` がある (参加者側の常駐ウォッチャー本体。単一バイナリ、stdlib のみ)。`kysymys-app` の `mvn package` 時に darwin-arm64 / windows-amd64 / linux-amd64 へクロスコンパイルされ、`kysymys-app` の `GET /agent/<os-arch>` から配信される。`kysymys-activity-agent`(拡張)はこれを初回ビルド時にダウンロードしてキャッシュ・起動する。サーバのビルドに Go ツールチェインが要る (`mvn package` 時のみ、`mvn test` には不要)。

ビルド時の JDK は Java 25 一本でよく、外部モジュールは `<release>17</release>` で Java 17 互換 class file を出力します。Maven Toolchains は使いません。

## 技術スタック (kysymys-app)

- **Web**: Enkan 0.15.0 + Kotowari-restful 0.15.0 (Liberator 型 decision graph)
- **入力検証**: Raoh 0.5.0 (`raoh-json`) — `@Decision(MALFORMED)` で `JsonDecoder` を使う
- **永続化**: JOOQ + `enkan-component-jooq` + Flyway。**コード生成は使わず** `field("col_name", Class)` 手書き DSL
- **HTTP**: Undertow (`enkan-component-undertow`)
- **JSON**: Jackson 3 (`enkan-component-jackson` + `jackson-jakarta-rs-json-provider`)
- **エラー応答**: RFC 9457 `application/problem+json`
- **認証**: Bouncr (`enkan-bouncr` の `BouncrBackend` で `x-bouncr-credential` JWT 検証)
- **DB**: 開発・テスト H2 / 本番 PostgreSQL

## ディレクトリ構造 (kysymys-app)

```
kysymys-app/src/main/java/net/unit8/kysymys/
├── KysymysDevMain.java               -- 開発用 entry point
├── KysymysMain.java                  -- 本番 entry point (環境変数読み込み)
├── KysymysDevSystemFactory.java      -- 開発用 component wiring (H2 in-mem)
├── KysymysSystemFactory.java         -- 本番 component wiring
├── KysymysApplicationFactory.java    -- middleware stack (Dev/本番共通)
├── inject/
│   └── DSLContextInjector.java       -- DSLContext を @Decision に注入
├── health/
│   ├── HealthResource.java           -- /health
│   └── MeResource.java               -- /me (Bouncr 認証疎通用)
├── activity/                         -- 作業状況テレメトリ (data/behavior/dao/resource)
└── (lesson/ user/ avatar/ notification/ を data/behavior/dao/resource 構成で配置)
```

## 作業状況テレメトリ (activity)

研修で submit を待たずに各参加者の作業状況を集め、手助けが要る人を講師が見つけるための仕組み。詳細は `doc/training-activity-setup.md`。

- 参加者側: `kysymys-activity-agent` (Maven 拡張で `mvn` のたびにビルド成否を HTTP 送信し、ホストに合う Go バイナリ `kysymys-agent` をサーバから DL してウォッチャーを起動。ファイル編集の heartbeat はその Go バイナリが送信) と `mvn kysymys:stuck` / `kysymys:resolved`
- サーバ側: `POST /activity` (テレメトリ受信、Bouncr JWT 認証、participantId = principal)、`GET /activity/status` (講師専用、TEACHER 権限。全参加者の現在状態を導出)、`GET /agent/<os-arch>` (Go バイナリ配信、認証不要のローエンドポイント)。蓄積は `activity_events` テーブル (Flyway V5)、状態は読み取り時に導出
- 講師側: React の `/dashboard` (teacher 専用、3 秒 polling)

各 Bounded Context (Sub-B 以降で追加) の配下には:

- `data/` — record / sealed interface のみ。バリデーション知識を持たない
- `behavior/` — `Function<Input, Result<Output>>` の純関数
- `dao/` — JOOQ DSL を手書きする Repository。`DSLContext` コンストラクタ注入
- `resource/` — Kotowari の `@Decision` メソッド + `*JsonDecoders` (Raoh) + `*JsonEncoders`

## ビルドと起動

```bash
# 全モジュールビルド
mvn clean compile

# Dev 起動 (H2 in-mem、ポート 3000)
cd kysymys-app && mvn exec:java

# 本番起動 (PostgreSQL を docker-compose で立てる)
cd kysymys-app && docker compose up -d
KYSYMYS_DB_URL=jdbc:postgresql://localhost:5432/kysymys \
KYSYMYS_DB_USER=kysymys \
KYSYMYS_DB_PASSWORD=kysymys \
KYSYMYS_JWT_SECRET=devsecret \
mvn exec:java -Dexec.mainClass=net.unit8.kysymys.KysymysMain
```

環境変数 (本番):

- `KYSYMYS_DB_URL`, `KYSYMYS_DB_USER`, `KYSYMYS_DB_PASSWORD` — DB 接続
- `KYSYMYS_JWT_SECRET` — Bouncr 用 HMAC 秘密鍵

## 認証

Bouncr の `BouncrBackend` が `x-bouncr-credential` ヘッダの JWT を検証し、`Principal` を立てます。

- Sub-A 時点では HMAC 固定キーで自前署名した JWT で疎通確認します
- Sub-C で本物の Bouncr スタック (Envoy + bouncr-proxy + bouncr-api-server) に接続します

`@Decision(AUTHORIZED)` で `Principal` の有無を確認し、なければ kotowari-restful が 401 を返します。

## 参照モデル

実装の見本は kawasima 氏の以下のリポジトリ:

- `kotowari-restful` の `example/` — `data/behavior/dao/resource/inject` の構成
- `validation-modeling` の `raoh/` — Raoh の使い方 (`combine` / `field` / `discriminate`)
- `enkan-bouncr` — `BouncrBackend` (HMAC / RSA / ECDSA 対応)

## 移行計画 (Sub-A〜E)

`doc/adr/004_replatform_to_enkan.md` と `doc/specs/` 配下の spec を参照。

- Sub-A: 新スタック土台 (完了)
- Sub-B: Lesson コンテキスト移行
- Sub-C: User / Avatar / Notification 移行 (Bouncr 本格接続を含む)
- Sub-D: React/TS フロントエンド構築
- Sub-E: kysymys-maven-plugin / kysymys-scorer-java の API 追随

## ADR

設計判断は `doc/adr/` 配下に番号付きで記録します。

- 001: Bounded Context の分割
- 002: レイヤー定義 (Sub-A で Enkan 構成に合わせ更新)
- 003: Problem のライフサイクル (Long-term イベントパターン)
- 004: Spring Boot から Enkan への全面置き換え

## PR ワークフロー

- `feature/<topic>` ブランチから `develop` へ PR
- `develop` への直接 commit 禁止
- `master` へは release PR のみ
