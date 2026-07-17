# examples — 動く演習と検証

Kysymys の学びのループを、実際に動く演習リポジトリで通すためのサンプルです。二つの役割を
兼ねています。

- **見本**: 研修の講師・学習者が「演習リポジトリはこう作る」とコピーして使える参照実装
- **検証**: `verify.sh` が submit → コメント → 通知 → 採点 → テレメトリ → ダッシュボードまでを
  本物のコマンドで一気通貫に通し、README のコンセプトが現状のコードで回ることを確かめる

## 構成

```
examples/
├── fizzbuzz/            演習問題プロジェクト (研修参加者が clone するもの)
├── bin/mint-jwt         dev 用の HS256 JWT 発行ヘルパ
└── verify.sh            ループ全体を実コマンドで駆動する検証スクリプト
```

`fizzbuzz/` 単体の使い方 (実装・自己採点・提出) は [`fizzbuzz/README.md`](fizzbuzz/README.md) にあります。
これがそのまま問題文で、kysymys に問題を登録するときの `readmePath` になります。

## 検証を回す

```bash
# 依存する kysymys モジュールを ~/.m2 に入れておく (初回のみ)
mvn -N install -DskipTests
mvn -pl kysymys-scorer-java,kysymys-maven-plugin,kysymys-activity-agent install -DskipTests

# ループを通す (dev サーバが未起動なら verify.sh が mvn exec:java で起動・停止する)
./examples/verify.sh
```

`verify.sh` が通す流れ:

1. dev サーバの応答を待つ (`GET /health`)
2. dev JWT を発行 (teacher / student)
3. 遅延サインアップ (`GET /users`)
4. teacher が問題を登録 (`POST /problems`)
5. student が `mvn kysymys:submit` で解答を提出 (git リモート URL + commit を送る)
6. teacher がレビューコメントを投稿 (`POST /answers/:id/comments`)
7. 通知機構を確認 (フォロー申請 → 対象者へ通知)
8. 手元で自己採点 (`mvn exec:java` → `100`)
9. `mvn kysymys:stuck` / `:resolved` とビルドイベントを送信
10. 講師ダッシュボードへの反映を確認 (`GET /activity/status`)

## スコープ境界と、検証で分かった現状との差

README のコンセプトと今のコードには、いくつか差があります。verify.sh はこれを踏まえて、
実装済みの範囲をグリーンにしつつ差分を出力します。

- **コメントは通知を生成しない (README との差)**。README は「コメントは学習者に通知として
  届きます」と述べますが、`PostComment` はイベントを発火せず、`CommentPostedEvent` 自体が
  ありません。通知を作る `RecordWhatsNew` は `SubmittedAnswerEvent` (フォロワー宛て) と
  `OfferedToFollowEvent` (対象者宛て) にしか反応しません。verify.sh はこの差分を警告として
  出し、通知機構そのものはフォロー申請の経路で動作確認します。README どおりにするには、
  コメント投稿時にイベントを発火し、`RecordWhatsNew` にコメント通知の分岐を加える必要が
  あります。
- **サーバ採点は存在しない**。採点は `kysymys-scorer-java` が学習者の手元で走り、点数を
  stdout に出すだけです。submit は解答リポジトリの URL とコミットを記録するだけで、採点は
  返しません (`KysymysTestLauncher` の javadoc に明記)。
- **本物の Bouncr スタックは使わない**。`mint-jwt` が dev の HMAC 固定鍵で自前署名します。
  本番の Envoy + bouncr-proxy 接続 (移行計画の Sub-C 後半) は未達です。
- **Go 常駐ウォッチャーの heartbeat は best-effort**。verify.sh は Maven 拡張のビルドイベントと
  `stuck`/`resolved` の到達までを必須検証とし、Go バイナリ配信 (`GET /agent/<os-arch>`、
  サーバの `mvn package` 時にクロスコンパイル) は任意扱いです。
