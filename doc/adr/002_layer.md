# レイヤー定義

## Context

このアプリケーション自体を、レイヤー間の依存が整理された姿の見本として位置づけたい。

ADR 004 で Spring Boot から Enkan / Kotowari / Raoh / JOOQ / Bouncr への置き換えを決めた。レイヤーの基本方針 (ドメインを独立に保ち、副作用はアダプタに隔離する) は維持しつつ、用語と実装手段を新スタックに合わせて改訂する。

## Decision

各 Bounded Context (Lesson / User / Avatar / Notification) の配下に以下のサブパッケージを置く。kotowari-restful の example の構成を踏襲する。

```
<context>/
├── data/        ドメインモデル。record / sealed interface のみ。
│                バリデーション知識やフレームワーク注釈を持たない。
├── behavior/    ユースケース。Function<Input, Result<Output>> または
│                BiFunction<...> の純関数。Spring の @Service 相当だが
│                注釈もコンテナへの依存も持たない。
├── dao/         JOOQ DSL を使う Repository。`field("col_name", Class)`
│                手書き DSL。`DSLContext` をコンストラクタ注入で受ける。
└── resource/    Kotowari の @Decision メソッドを持つ resource クラス +
                 *JsonDecoders (Raoh) + *JsonEncoders。
                 Liberator 型 decision graph に従う。
```

レイヤー間依存の方針:

- **ドメインレイヤー (`data/`)** は他のレイヤーに依存しない。フレームワーク注釈も持たない。
- **アプリケーションレイヤー (`behavior/`)** は Web や DB のライブラリに直接依存しない。dao の interface には依存できる。Bouncr/Kotowari の注釈や Web 由来の型を持ち込まない。
  - Spring Boot 時代は「DI コンテナとしての Spring には依存して OK」としていたが、Enkan の `EnkanSystem` はクラス名を文字列で参照する `ApplicationComponent` で wiring するため、behavior 側にコンテナ依存の注釈を書く必要がない。アプリケーション層は完全にプレーン Java になる。
- **アダプタレイヤー (`dao/` `resource/`)** は副作用を持つ操作をすべて担う。
  - データベース操作 (JOOQ DSL を `dao/` で手書き)
  - HTTP 応答の組み立て (`resource/` の `@Decision` メソッド)
  - 入出力の検証と型変換 (Raoh の `JsonDecoder` / `JooqRecordDecoder`)
- Web のレイヤは Bounded Context をまたぐが、kotowari-restful 流ではコンテキストごとの `resource/` 配下に置けば Routes 側で集約できる。

JOOQ コード生成は使わない。`field("col_name", Class)` でカラム参照を手書きする。これは参照モデル (kotowari-restful の example、validation-modeling の Raoh) と揃える方針判断。テーブル/カラム名の typo は dao 単位のテストで吸収する。

## Consequences

- アプリケーション層から Spring 起源の注釈 (`@Service` `@Component` `@Autowired` `@Transactional` 等) がすべて消え、ドメインとアプリケーション層は純粋な Java になる。
- 永続化は JOOQ + `dsl.transaction(cfg -> { ... })` の明示呼び出しに統一される。`@Transactional` は使わない。
- 入出力境界は Raoh の `JsonDecoder` / `JooqRecordDecoder` で型付けされ、ドメインモデル (`data/`) は record と sealed interface だけで構成される。
- Sub-B 以降の各 Bounded Context 移植時には、まず `data/` の record 定義を確定させ、続いて `dao/` で永続化、`behavior/` で純関数のユースケース、最後に `resource/` で HTTP 露出、という順序で組み立てる。
