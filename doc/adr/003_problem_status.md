# Problemのライフサイクル

## Context

Problemは生成されてから、アーカイブされる。そのライフサイクルを管理したい。

## Decision

[Long termイベントパターン](https://scrapbox.io/kawasima/%E3%82%A4%E3%83%9F%E3%83%A5%E3%83%BC%E3%82%BF%E3%83%96%E3%83%AB%E3%83%87%E3%83%BC%E3%82%BF%E3%83%A2%E3%83%87%E3%83%AB#5e3a5f1da8e5b200009c04ec) を使う。


[Problem](../../kysymys-app/src/main/java/net/unit8/kysymys/lesson/adapter/persistence/ProblemJpaEntity.java) に対して、[ProblemLifecycle](../../kysymys-app/src/main/java/net/unit8/kysymys/lesson/adapter/persistence/ProblemJpaEntity.java) のロングタームイベントをOneToOneの関連として作り、ProblemLifecycleがステータスを持つ。

```


   problems             problem_lifecycles      problem_events
  ┌──────────────┐1     ┌────────────────┐ 1    ┌────────────────────┐
  │id            ├──┐   │id              ├───┐  │id                  │
  ├──────────────┤  │  1├────────────────┤   │  ├────────────────────┤
  │name          │  └───┤problem_id      │   └──┤problem_lifecycle_id│
  │repository_url│      │problem_status  │   1…*│occurred_at         │
  │branch        │      └────────────────┘      └────────────────────┘
  │readme_path   │                                        △
  └──────────────┘                               ┌────────┴────────┐
                                                 │                 │
                                         problem_created    problem_archived
                                         ┌─────────────┐    ┌─────────────┐
                                         │id           │    │id           │
                                         ├─────────────┤    ├─────────────┤
                                         │creator_id   │    │archiver_id  │
                                         └─────────────┘    └─────────────┘


```

ProblemLifecycleのステータスを変えるイベントを、[ProblemEvent](../../kysymys-app/src/main/java/net/unit8/kysymys/lesson/adapter/persistence/ProblemJpaEntity.java)エンティティとして記録する。ProblemEventはEventごとのエンティティが作られる。
このProblemEventはUseCaseの実行結果として送出されるイベントに対応しており、それを永続化するものである。

イベントは日時属性をただ一つ持つ。そのふるまいは共通しているので、ProblemEventを継承して、ProblemCreatedやProblemArchivedなどの実際のイベントを作る。

UseCaseから送出される過程で、永続化層には以下の操作がされる。

- Problemを新規登録および変更する。
- ProblemLifecycleのステータスを変更する。
- イベントに応じてProblemCreatedやProblemArchivedを登録する。

本アプリケーションでは、EventSourcingの形はとらないが、もしEventSourcingだとすれば、このProblemEventsを時系列に適用してProblemの状態を復元すればよい。

というように、Long-termイベントパターンは、Command-Eventの設計やEvent Sourcingとよくマッチする。



