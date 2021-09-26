# Problemのライフサイクル

## Context

Problemは生成されてから、アーカイブしたい。

## Decision

Long termイベントパターンを使う。

Problemに対して、ProblemLifecycleのロングタームイベントをOneToOneの関連として作り、ProblemLifecycleがステータスを持つ。

ProblemLifecycleのステータスを変えるイベントを、ProblemEventエンティティとして記録する。ProblemEventはEventごとのエンティティが作られる。
このProblemEventはUseCaseの実行結果として送出されるイベントに対応しており、それを永続化するものである。



