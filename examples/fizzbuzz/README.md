# 演習: FizzBuzz

Kysymys の演習問題プロジェクトのサンプルです。この README がそのまま問題文になります
(kysymys に問題を登録するとき `readmePath` に指定します)。

## 課題

`src/main/java/net/unit8/kysymys/example/fizzbuzz/FizzBuzz.java` の `convert(int n)` を実装してください。
1 以上の整数 `n` を受け取り、次の規則で文字列を返します。

- 3 の倍数なら `Fizz`
- 5 の倍数なら `Buzz`
- 3 と 5 の両方の倍数なら `FizzBuzz`
- それ以外はその数の 10 進表記 (例: `1` → `"1"`)

## 自分でテスト・採点する

```bash
# テストを走らせる
mvn test

# 点数 (通ったテストの割合 0〜100) を表示する
mvn -q test-compile exec:java
```

採点はあなたの手元で走ります。kysymys サーバは採点をしません
(提出は解答リポジトリの URL とコミットを記録するだけです)。

## 提出する

`kysymys.properties` に接続先・問題 ID・自分のトークンを記入し、コミットしてから:

```bash
mvn net.unit8.kysymys:kysymys-maven-plugin:submit
```

提出は「今の git リモートの URL + HEAD コミット」をサーバに送ります。事前に
リモートへ push しておいてください。

## 作業状況の共有 (任意)

`.mvn/extensions.xml` が置いてあると、`mvn` を回すたびにビルドの成否が、ファイルを
編集するたびに heartbeat が講師の kysymys サーバに送られます。詰まったとき・解消した
ときは自分から知らせられます。

```bash
mvn net.unit8.kysymys:kysymys-maven-plugin:stuck      # 「詰まった」
mvn net.unit8.kysymys:kysymys-maven-plugin:resolved   # 「解消した」
```
