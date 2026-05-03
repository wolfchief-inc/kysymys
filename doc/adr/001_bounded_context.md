# Bounded Context

## Context

将来的なサービス分離を目指して、コンテキストを切り分けておきたい。

## Decision

初期リリース段階では、以下４つに分ける。

- Lesson (Core): 問題を作成したり、それに回答したり、コメントをつけたりする。
- User (Supported): Kysymysを使うユーザの登録やプロフィール閲覧・変更、フォロー申請など。
- Avatar (Generic): ユーザのアバターを生成・表示する。
- Notification (Generic): ユーザへの更新メッセージなどの通知を行う。

テーブルも各コンテキストに分けてもつ。
ユーザは他のすべてのコンテキストでも参照する必要があるが、User以外のコンテキストではUserIdのみを持つ。

## Consequences

ユーザ以外のContextからユーザのデータを参照したいケース。

### Lessonコンテキストで、フォロー関係にあるユーザの解答が見える

他人の解答が見えるかどうか判定したいが、この判定にはユーザコンテキストが必要になる。

### Notificationでユーザの名前やEメールアドレスが必要になる

メール通知する場合、メールアドレスが必要になるが、NotificationからはUserコンテキスを参照したくない。
したがって、Notificationが受け取るEventに、必要なUserの名前やEメールアドレスを含めて受け渡すようにしている。

### Sub-C のクロスコンテキストイベント実装

Sub-C で Spring Boot から Enkan/Kotowari に移行した際、クロスコンテキストイベントは `net.unit8.kysymys.events` 共通パッケージの sealed interface `KysymysEvent` (permits `SubmittedAnswerEvent` / `OfferedToFollowEvent` / `UserCreatedEvent`) として定義する。各 Event は record で、必要な User 情報 (UserId, UserName, EmailAddress 等) を含めて発火元の Bounded Context が publish する。

- 配信は in-memory の `KysymysEventBus` (system component)。同期 dispatch、handler 例外は隔離。
- Notification の `RecordWhatsNewSubscriber` が起動時に subscribe し、受信した Event を `whats_news` / `unread_whats_news` に永続化する。
- メール送信は将来 Sub に分離 (Sub-C ではスコープ外)。
