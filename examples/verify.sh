#!/usr/bin/env bash
#
# Kysymys の学びのループを、本物のコマンドでエンドツーエンドに通す検証スクリプト。
#
# 通す流れ:
#   1. dev サーバ起動を待つ (未起動なら自分で mvn exec:java で起動)
#   2. dev JWT を発行 (teacher / student)
#   3. 遅延サインアップ (GET /users)
#   4. teacher が問題を登録         POST /problems
#   5. student が解答を提出         mvn kysymys:submit  (git リモート URL + commit を送る)
#   6. teacher がレビューコメント    POST /answers/:id/comments
#   7. student に通知が届く         GET /whats-news
#   8. 手元で自己採点               mvn exec:java -> 100
#   9. 作業状況テレメトリ           mvn kysymys:stuck / :resolved + ビルドイベント
#  10. 講師ダッシュボードに反映     GET /activity/status
#
# スコープ境界 (README のコンセプトと現状の差):
#   - サーバ採点は存在しない。採点は student のローカルで走る (scorer)。
#   - 本物の Bouncr スタックではなく dev の HMAC 固定鍵で自前署名する。
#   - Go 常駐ウォッチャーの heartbeat は best-effort。ここでは拡張のビルドイベントと
#     stuck/resolved の到達までを必須検証とする。
#
set -uo pipefail

# --- 設定 -------------------------------------------------------------------
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$HERE/.." && pwd)"
HOST="${KYSYMYS_URL:-http://localhost:3000}"
MINT="$HERE/bin/mint-jwt"
EXAMPLE="$HERE/fizzbuzz"
PLUGIN="net.unit8.kysymys:kysymys-maven-plugin:0.2.0-SNAPSHOT"

# UserId はこのシステムでは 21 文字の NanoID。offer の targetUserId が fixedLength(21) を
# 要求するので、dev トークンの sub も 21 文字に揃える。
STUDENT_ID="$(printf '%-21s' alice | tr ' ' '0')"   # alice0000000000000000
TEACHER_ID="$(printf '%-21s' sensei | tr ' ' '0')"  # sensei000000000000000

WORK="$(mktemp -d "${TMPDIR:-/tmp}/kysymys-verify.XXXXXX")"
SERVER_LOG="$WORK/server.log"
STARTED_SERVER=0
MVN_PID=""

PASS=0
FAIL=0

# --- 出力ヘルパ -------------------------------------------------------------
c_green='\033[0;32m'; c_red='\033[0;31m'; c_yellow='\033[0;33m'; c_reset='\033[0m'
info() { printf "%b\n" "${c_yellow}• $*${c_reset}"; }
pass() { printf "%b\n" "${c_green}✓ $*${c_reset}"; PASS=$((PASS + 1)); }
fail() { printf "%b\n" "${c_red}✗ $*${c_reset}"; FAIL=$((FAIL + 1)); }

cleanup() {
  if [[ "$STARTED_SERVER" == "1" ]]; then
    info "dev サーバを停止します"
    [[ -n "$MVN_PID" ]] && kill "$MVN_PID" 2>/dev/null
    pkill -f 'net.unit8.kysymys.KysymysDevMain' 2>/dev/null
  fi
  rm -rf "$WORK"
}
trap cleanup EXIT

# --- HTTP ヘルパ ------------------------------------------------------------
# req METHOD PATH TOKEN [JSON_BODY] -> グローバル RESP_CODE / RESP_BODY を設定
req() {
  local method="$1" path="$2" token="$3" body="${4:-}"
  local out="$WORK/resp.$$"
  local -a args=(-s -o "$out" -w '%{http_code}' -X "$method" -H "x-bouncr-credential: $token" -H "Accept: application/json")
  if [[ -n "$body" ]]; then
    args+=(-H 'Content-Type: application/json' --data "$body")
  fi
  RESP_CODE="$(curl "${args[@]}" "$HOST$path")"
  RESP_BODY="$(cat "$out")"
  rm -f "$out"
}

# jget JSON PYEXPR  ('d' に読み込んだ dict/list を渡す python 式)
jget() { python3 -c 'import json,sys; d=json.load(sys.stdin); print(eval(sys.argv[1]))' "$2" <<<"$1"; }

# --- サーバ起動 -------------------------------------------------------------
wait_health() {
  for _ in $(seq 1 90); do
    if curl -sf "$HOST/health" >/dev/null 2>&1; then return 0; fi
    sleep 1
  done
  return 1
}

info "対象サーバ: $HOST"
if curl -sf "$HOST/health" >/dev/null 2>&1; then
  info "既に起動中のサーバを使います"
else
  info "サーバが見つからないので mvn exec:java で起動します (ログ: $SERVER_LOG)"
  ( cd "$REPO_ROOT/kysymys-app" && exec mvn -q exec:java ) >"$SERVER_LOG" 2>&1 &
  MVN_PID=$!
  STARTED_SERVER=1
  if ! wait_health; then
    fail "サーバが起動しませんでした。ログ末尾:"
    tail -20 "$SERVER_LOG" || true
    exit 1
  fi
fi
pass "サーバが応答 (GET /health)"

# --- トークン発行 -----------------------------------------------------------
STUDENT_TOKEN="$("$MINT" --sub "$STUDENT_ID" --perm STUDENT --name Alice --email alice@example.com)"
TEACHER_TOKEN="$("$MINT" --sub "$TEACHER_ID" --perm TEACHER --name Sensei --email sensei@example.com)"
pass "dev JWT を発行 (student=$STUDENT_ID / teacher=$TEACHER_ID)"

# --- 遅延サインアップ (User 行を作る) ---------------------------------------
req GET /users "$STUDENT_TOKEN"; [[ "$RESP_CODE" =~ ^2 ]] && pass "student 遅延サインアップ (GET /users -> $RESP_CODE)" || fail "student サインアップ失敗 ($RESP_CODE): $RESP_BODY"
req GET /users "$TEACHER_TOKEN"; [[ "$RESP_CODE" =~ ^2 ]] && pass "teacher 遅延サインアップ (GET /users -> $RESP_CODE)" || fail "teacher サインアップ失敗 ($RESP_CODE): $RESP_BODY"

# --- 4. 問題登録 ------------------------------------------------------------
PROBLEM_BODY='{"name":"FizzBuzz","repository":{"type":"github","url":"https://github.com/example/fizzbuzz","branch":"main","readmePath":"/README.md"}}'
req POST /problems "$TEACHER_TOKEN" "$PROBLEM_BODY"
if [[ "$RESP_CODE" == "201" ]]; then
  PROBLEM_ID="$(jget "$RESP_BODY" "d['id']")"
  pass "問題を登録 (POST /problems -> 201, id=$PROBLEM_ID)"
else
  fail "問題登録に失敗 ($RESP_CODE): $RESP_BODY"; exit 1
fi

# --- 5. 解答リポジトリを用意して提出 ----------------------------------------
ANSWER="$WORK/answer"
cp -r "$EXAMPLE" "$ANSWER"
rm -rf "$ANSWER/target" "$ANSWER/solution"
# スタブを参考解答で上書き (「解けた状態」を再現)
cp "$EXAMPLE/solution/FizzBuzz.java" "$ANSWER/src/main/java/net/unit8/kysymys/example/fizzbuzz/FizzBuzz.java"
# 提出は git リモート URL + HEAD コミットを送るので、独立した git リポジトリにする
(
  cd "$ANSWER"
  git init -q
  git config user.email "alice@example.com"; git config user.name "Alice"
  git remote add origin "https://github.com/example/fizzbuzz-answer.git"
  git add -A && git commit -q -m "Solve FizzBuzz"
)
# 拡張 (activity-agent) が読む kysymys.properties を用意 -> mvn を回すたびビルドイベントが飛ぶ
cat >"$ANSWER/kysymys.properties" <<EOF
kysymys.url=$HOST
kysymys.problemId=$PROBLEM_ID
kysymys.token=$STUDENT_TOKEN
EOF

info "解答を提出 (mvn $PLUGIN:submit)"
( cd "$ANSWER" && mvn -q -B "$PLUGIN:submit" \
    -Dkysymys.url="$HOST" -Dkysymys.problemId="$PROBLEM_ID" -Dkysymys.token="$STUDENT_TOKEN" ) \
    >"$WORK/submit.log" 2>&1
if [[ $? -eq 0 ]]; then pass "解答を提出 (mvn kysymys:submit)"; else fail "提出に失敗。ログ末尾:"; tail -15 "$WORK/submit.log"; fi

# 提出された answer の id を取る
req GET /answers "$STUDENT_TOKEN"
ANSWER_ID="$(jget "$RESP_BODY" "([a['id'] for a in (d if isinstance(d,list) else d.get('items',d.get('answers',[]))) if a.get('problemId')=='$PROBLEM_ID'] or [''])[0]" 2>/dev/null || echo "")"
if [[ -n "$ANSWER_ID" ]]; then pass "解答が記録された (GET /answers, id=$ANSWER_ID)"; else fail "解答が見つからない: $RESP_BODY"; fi

# --- 6. レビューコメント ----------------------------------------------------
if [[ -n "${ANSWER_ID:-}" ]]; then
  req POST "/answers/$ANSWER_ID/comments" "$TEACHER_TOKEN" '{"description":"Fizz と Buzz の順序を確認してみて"}'
  [[ "$RESP_CODE" == "201" ]] && pass "teacher がレビューコメント投稿 (201)" || fail "コメント投稿失敗 ($RESP_CODE): $RESP_BODY"
fi

# news_count TOKEN -> 標準出力に whats-news の件数 (レスポンスは JSON 配列)
news_count() { req GET /whats-news "$1"; jget "$RESP_BODY" "len(d if isinstance(d,list) else d.get('items',[]))" 2>/dev/null || echo 0; }

# --- 7a. コメント -> 通知 (README は約束するが現状は未実装: 既知の差分) ---------
COMMENT_NEWS="$(news_count "$STUDENT_TOKEN")"
if [[ "${COMMENT_NEWS:-0}" -ge 1 ]]; then
  pass "コメントが通知として届いた (GET /whats-news, $COMMENT_NEWS 件)"
else
  info "既知の差分: コメントは通知を生成しない。README は「コメントは学習者に通知として届く」"
  info "  と述べるが、PostComment はイベントを発火せず (CommentPostedEvent 自体が無い)、"
  info "  通知を作る RecordWhatsNew は SubmittedAnswer / OfferedToFollow にしか反応しない。"
fi

# --- 7b. 通知機構そのものの検証: フォロー申請 -> 対象者へ通知 -------------------
req POST /offers "$TEACHER_TOKEN" "{\"targetUserId\":\"$STUDENT_ID\"}"
if [[ "$RESP_CODE" == "201" ]]; then
  pass "teacher が student にフォロー申請 (POST /offers -> 201)"
  OFFER_NEWS="$(news_count "$STUDENT_TOKEN")"
  if [[ "${OFFER_NEWS:-0}" -ge 1 ]]; then
    pass "通知が届いた (OfferedToFollow -> GET /whats-news, $OFFER_NEWS 件) — 通知機構は動作"
  else
    fail "フォロー申請の通知が届かない: $RESP_BODY"
  fi
else
  fail "フォロー申請に失敗 ($RESP_CODE): $RESP_BODY"
fi

# --- 8. 自己採点 ------------------------------------------------------------
info "自己採点 (mvn exec:java)"
SCORE="$( cd "$ANSWER" && mvn -q -B test-compile exec:java 2>/dev/null | grep -Eo '^[0-9]+$' | tail -1 )"
if [[ "$SCORE" == "100" ]]; then pass "解答が満点 (scorer -> 100)"; else fail "採点が 100 でない: '$SCORE' (ログは mvn exec:java を直接実行して確認)"; fi

# --- 9-10. テレメトリと講師ダッシュボード -----------------------------------
info "「詰まった」を送信 (mvn kysymys:stuck)"
( cd "$ANSWER" && mvn -q -B "$PLUGIN:stuck" \
    -Dkysymys.url="$HOST" -Dkysymys.problemId="$PROBLEM_ID" -Dkysymys.token="$STUDENT_TOKEN" ) >/dev/null 2>&1 \
    && pass "stuck を送信" || fail "stuck 送信に失敗"

req GET /activity/status "$TEACHER_TOKEN"
if [[ "$RESP_CODE" == "200" ]]; then
  STUCK="$(jget "$RESP_BODY" "([p['stuck'] for p in d['participants'] if p['participantId']=='$STUDENT_ID'] or ['MISSING'])[0]")"
  if [[ "$STUCK" == "True" ]]; then pass "ダッシュボードに student が stuck で現れた (GET /activity/status)"; else fail "status に stuck 状態が反映されない (=$STUCK): $RESP_BODY"; fi
else
  fail "status 取得に失敗 ($RESP_CODE): $RESP_BODY"
fi

info "「解消した」を送信 (mvn kysymys:resolved)"
( cd "$ANSWER" && mvn -q -B "$PLUGIN:resolved" \
    -Dkysymys.url="$HOST" -Dkysymys.problemId="$PROBLEM_ID" -Dkysymys.token="$STUDENT_TOKEN" ) >/dev/null 2>&1 \
    && pass "resolved を送信" || fail "resolved 送信に失敗"

req GET /activity/status "$TEACHER_TOKEN"
STUCK_AFTER="$(jget "$RESP_BODY" "([p['stuck'] for p in d['participants'] if p['participantId']=='$STUDENT_ID'] or ['MISSING'])[0]" 2>/dev/null || echo MISSING)"
[[ "$STUCK_AFTER" == "False" ]] && pass "resolved 後は stuck=false に戻った" || fail "resolved 後も stuck が解けていない (=$STUCK_AFTER)"

# ビルドイベントが届いているか (submit / scoring の mvn 実行で拡張が送ったはず)
LAST_BUILD="$(jget "$RESP_BODY" "([p.get('lastBuildKind') for p in d['participants'] if p['participantId']=='$STUDENT_ID'] or ['MISSING'])[0]" 2>/dev/null || echo MISSING)"
if [[ "$LAST_BUILD" == "BUILD_SUCCESS" || "$LAST_BUILD" == "BUILD_FAILURE" ]]; then
  pass "拡張からビルドイベントが届いている (lastBuildKind=$LAST_BUILD)"
else
  info "ビルドイベント未確認 (lastBuildKind=$LAST_BUILD) — 拡張の Go バイナリ配信は best-effort"
fi

# --- 集計 -------------------------------------------------------------------
echo
printf "%b\n" "結果: ${c_green}${PASS} passed${c_reset}, ${c_red}${FAIL} failed${c_reset}"
[[ "$FAIL" -eq 0 ]]
