# 研修での作業状況リアルタイム収集

解答の submit を待たずに、各参加者の作業状況を自動で集めて、手助けが要る人を講師が見つけられるようにする仕組みです。参加者のマシンで Maven 拡張と常駐ウォッチャーが動き、kysymys サーバの `POST /activity` にイベントを送ります。講師は React の `/dashboard` でそれをリアルタイムに見ます。

常駐ウォッチャーは Go の単一バイナリ (`kysymys-agent`) です。Kysymys サーバが darwin-arm64 / windows-amd64 / linux-amd64 のバイナリを `GET /agent/<os-arch>` で配信し、Maven 拡張が初回ビルド時にホストに合うものを**自動でダウンロード**して `~/.kysymys/bin/` にキャッシュ・起動します。参加者は Go も常駐プロセスもバイナリの手動配置も不要です(2回目以降はキャッシュを使う。更新するときは `~/.kysymys/bin/` を消す)。

## 集まる信号

| 信号 | いつ送られるか | 何が分かるか |
|---|---|---|
| `BUILD_SUCCESS` / `BUILD_FAILURE` | `mvn` を回し終えるたび (Maven 拡張) | コンパイル/ビルドが通っているか。失敗時は先頭エラー行も付く |
| `HEARTBEAT` | ファイルを編集したとき (Go の常駐ウォッチャー) | 手が動いているか。最終活動からの経過で無活動を検知 |
| `STUCK` / `RESOLVED` | `mvn kysymys:stuck` / `kysymys:resolved` | 本人からの「詰まった」「解消した」申告 |

参加者は配布された JWT で識別されるので、人ごとに別のトークンを配れば自動的に別人として記録されます。

## 講師側の準備

### 1. サーバを起動する

```bash
cd kysymys-app && mvn exec:java        # 開発用 (H2 in-mem, ポート 3000)
```

参加者からアクセスできるよう、`kysymys.url` には講師マシンの IP を配ります (例 `http://192.168.1.10:3000`)。

### 2. トークンを配る

参加者ごとに本人用 JWT を、自分用に teacher 権限付き JWT を作ります。開発サーバは固定 HMAC 秘密鍵 (`kysymys-dev-jwt-secret-not-for-production`) で署名を検証するので、次のスクリプトで作れます。

```python
# mint.py : python3 mint.py <sub> <permissions(カンマ区切り)>
import sys, hmac, hashlib, base64, json
secret = "kysymys-dev-jwt-secret-not-for-production"
b64 = lambda b: base64.urlsafe_b64encode(b).decode().rstrip("=")
sub, perms = sys.argv[1], (sys.argv[2].split(",") if len(sys.argv) > 2 and sys.argv[2] else [])
seg = b64(json.dumps({"alg":"HS256","typ":"JWT"}).encode()) + "." + \
      b64(json.dumps({"uid":"1","sub":sub,"permissions":perms,"exp":4102444800}).encode())
sig = hmac.new(secret.encode(), seg.encode(), hashlib.sha256).digest()
print(seg + "." + b64(sig))
```

```bash
python3 mint.py teacher1 TEACHER     # 講師用 (ダッシュボードを見る)
python3 mint.py alice                # 参加者 alice 用
python3 mint.py bob                  # 参加者 bob 用 ...
```

本番 (Bouncr 本接続) では発行は Bouncr が担うので、このスクリプトは開発・研修用です。

### 3. ダッシュボードを開く

フロントエンド (`kysymys-frontend`) を起動し、ログイン画面で teacher トークンを貼り付けて `/dashboard` を開きます。3 秒ごとに更新され、参加者は「手助けが要りそうな順」(stuck → 無活動が長い → ビルド失敗) に並びます。行の色は無活動時間で緑 (<2分) / 橙 (2〜10分) / 赤 (>10分 または stuck)。

## 参加者側の準備

演習リポジトリ (各自が `mvn test` を回すプロジェクト) のルートに 2 ファイルを置くだけです。

1. `.mvn/extensions.xml` — `kysymys-activity-agent/example/extensions.xml` をコピー
2. `kysymys.properties` — `kysymys-activity-agent/example/kysymys.properties.template` をコピーし、配布された `kysymys.url` / `kysymys.problemId` / `kysymys.token` を記入

以降は普段どおり `mvn test` などを回すだけで、ビルド成否と編集の heartbeat が自動で送られます。詰まったら:

```bash
mvn kysymys:stuck       # 講師に「詰まった」を通知
mvn kysymys:resolved    # 解消したら取り消す
```

> `kysymys-activity-agent`(および親 POM `kysymys-parent`)が参加者のローカルリポジトリ (`~/.m2`) に入っている必要があります(`.mvn/extensions.xml` の解決用)。研修前に `mvn -N install`(親 POM)と `mvn -pl kysymys-activity-agent install`(拡張本体)を流して配布するか、社内 Maven リポジトリに deploy しておきます。この jar に Go バイナリは入っていないので軽量で、ビルドに Go は要りません。
>
> Go バイナリ自体は **Kysymys サーバが配信**します。サーバ側 (`kysymys-app`) を `mvn package` でビルドすると、`kysymys-agent/` の Go ソースが3 OS/arch 向けにクロスコンパイルされ、`GET /agent/<os-arch>` から配信されます。サーバをビルドするマシンには Go ツールチェインが必要です(`mvn package` 時のみ。`mvn test` には不要)。dev で配信を有効にして起動するには `cd kysymys-app && mvn package -DskipTests exec:java`。

## ウォッチャーがうまく動かないとき

常駐ウォッチャー (`kysymys-agent watch`) は初回ビルド時に Maven 拡張が別プロセスとして起動します (二重起動はロックファイルで防止、無活動 15 分で自己終了)。次の場合でもビルドイベント (`BUILD_SUCCESS` / `BUILD_FAILURE`) と `mvn kysymys:stuck` は問題なく届き、無活動判定は「最後のビルドからの経過」で近似されます。

- プロセス起動が環境依存でこけたとき
- 同梱外の OS/arch のとき (例: Intel Mac, Linux arm)。ビルドイベントは JVM から HTTP で送るので影響を受けない
