# Bounded Context

## Context

将来的なサービス分離を目指して、コンテキストを切り分けておきたい。

## Decision

初期リリース段階では、以下４つに分ける。

- Lesson (Core)
- User (Supported)
- Avatar (Generic)
- Notification (Generic)

テーブルも各コンテキストに分けてもつ。
ユーザは他のすべてのコンテキストでも参照する必要があるが、User以外のコンテキストではUserIdのみを持つ。
