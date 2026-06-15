# Sub-D: React/TS フロントエンド構築

**作成日**: 2026-05-03
**対象ブランチ**: `feature/sub-d-react-frontend`
**ベース**: `develop` (Sub-A, Sub-B, Sub-C 完了済み)

## Context

Sub-A〜C で kysymys-app バックエンドが新スタック (Enkan / Kotowari-restful / Raoh / JOOQ / Bouncr) で完成し、JSON API として 4 Bounded Context (Lesson / User / Avatar / Notification) のすべての操作を露出している。本 spec は **そのバックエンドに対する SPA フロントエンド** を新規構築する作業。

旧 Thymeleaf テンプレートは Sub-A で全削除済み。Sub-D は完全な新規実装。React 19 + TypeScript + Vite + TanStack Router/Query + Zod + Tailwind CSS + shadcn/ui で構築し、`kysymys-frontend/` ディレクトリを repo root に新設する。

## ゴール

15 画面の SPA がローカル開発環境で動作する。Vite dev server (port 5173) が kysymys-app (port 3000) に `/api/*` パスをプロキシして API 呼び出しが行える。dev 用にハードコードした Bouncr JWT を `localStorage` に置けば、Lesson / User / Avatar / Notification の全機能が画面操作で完結する。

Mobile first で完全 responsive (Tailwind の breakpoint で <768px → 768px〜 → 1024px〜)。日本語/英語の i18n を react-i18next で備える。Vitest で API client と key components の smoke test。

## 全体方針 (確定済み決定事項)

| 観点 | 決定 |
|---|---|
| スコープ | 全 15 画面カバー (Dashboard / Login / Problems list,detail,new,edit / Submit / My answers / Answer detail / Users / User profile / Profile edit / Teachers / Offers / Notifications / Following) |
| 技術スタック | React 19、Vite、TypeScript 5.x、TanStack Router、TanStack Query、Zod、Tailwind CSS |
| UI primitives | shadcn/ui (Radix UI primitives + Tailwind)。コードを直接 `src/components/ui/` に置く |
| モジュール配置 | `kysymys-frontend/` を repo root に新設。kysymys-backend (TS 実験) はそのまま残す |
| 認証 | 開発時は `/tmp/GenTokenWithRole.java` で生成した HMAC JWT を `localStorage.kysymysToken` に置く。Bouncr 本物接続は Sub-F で |
| API client | fetch + Zod 手書きスキーマ。`src/lib/api/` 配下に endpoint ごとの関数 |
| レイアウト | ヘッダー (検索 + 通知 + Avatar) + サイドバー (Nav) + メイン。Mobile first で <768px はサイドバー collapse |
| i18n | react-i18next + `messages.ja.json` / `messages.en.json`。default は ja、ヘッダーで切替 |
| テスト | Vitest unit/component。E2E は kysymys-app の Hurl で代替 |
| PR | 1 spec、内部 5 phase 分割 (Sub-D-1〜5) |
| デプロイ | 開発: Vite dev (5173) + kysymys-app (3000) を別々起動、proxy で `/api/*` を後者へ。本番デプロイは別 Sub |
| CORS | kysymys-app に CORS 設定を追加 (Sub-D の小スコープ修正)。`KYSYMYS_CORS_ORIGINS` 環境変数で許可 origin を設定 |

## 完了条件

1. `cd kysymys-frontend && npm install && npm run dev` で Vite dev server が port 5173 で起動。
2. `cd kysymys-app && mvn exec:java` で kysymys-app が port 3000 で起動。両者を別ターミナルで起動した状態で SPA から API 呼び出しが proxy 経由で動く。
3. ブラウザで `http://localhost:5173` を開いた最初の画面で、`localStorage.kysymysToken` が空なら **Login (dev) 画面** に redirect、token があれば **Dashboard** に遷移する。
4. Login (dev) 画面で JWT を貼り付けて保存ボタンを押すと、Dashboard に遷移し、ヘッダーに自分の Avatar / 名前が表示される。
5. 各 route (15) で対応する API を叩き、データが取得できて画面に反映される。
6. Mobile (<768px) viewport で各 route のレイアウトが崩れず操作可能。
7. ヘッダーの言語切替で日本語/英語のラベルが切り替わる。
8. `npm run test` で Vitest が成功する。
9. `npm run build` で `dist/` が生成され、ビルドエラーなし。
10. kysymys-app に CORS middleware を追加し、`KYSYMYS_CORS_ORIGINS=http://localhost:5173` の設定で SPA からの API 呼び出しが通る。
11. Sub-A〜C の Hurl scenarios が引き続き全 PASS (regression なし)。

## 含めない (後続 Sub に回す)

- Bouncr 本物スタック接続 (Envoy + bouncr-proxy + bouncr-api-server)、OIDC redirect ログイン — **Sub-F**
- 本番デプロイ手順 (S3 / Netlify / Cloudflare Pages へのアップロード) — 別 Sub
- Playwright E2E、Visual regression test — 別 Sub
- PWA (Service Worker, offline 対応) — 別 Sub
- Markdown レンダリング (問題文、コメント) — 必要なら別 Sub
- code highlighting (commit hash や README プレビュー) — 別 Sub
- ファイルアップロード UI (Avatar 差し替え) — Sub-C で multipart upload は未実装、別 Sub
- kysymys-maven-plugin / kysymys-scorer-java の API 追随 — **Sub-E**

## ディレクトリ構造

```
kysymys-frontend/
├── package.json
├── tsconfig.json
├── vite.config.ts                       -- proxy /api → http://localhost:3000
├── tailwind.config.ts
├── postcss.config.js
├── index.html
├── public/
├── src/
│   ├── main.tsx                         -- entry point
│   ├── App.tsx                          -- TanStack Router provider + i18n + QueryClient
│   ├── router.tsx                       -- route 定義
│   ├── i18n/
│   │   ├── index.ts                     -- i18next 設定
│   │   ├── ja.json
│   │   └── en.json
│   ├── lib/
│   │   ├── api/
│   │   │   ├── client.ts                -- fetch wrapper、x-bouncr-credential 自動付与、Zod parse
│   │   │   ├── lesson.ts                -- Problem/Answer/Comment endpoints
│   │   │   ├── user.ts                  -- User/Offer/Connection/Teachers endpoints
│   │   │   ├── notification.ts          -- WhatsNew endpoints
│   │   │   ├── avatar.ts                -- avatar URL helper
│   │   │   └── schemas.ts               -- Zod schemas (Problem, Answer, User, ...)
│   │   ├── auth.ts                      -- localStorage token 管理
│   │   └── utils.ts
│   ├── components/
│   │   ├── ui/                          -- shadcn/ui primitives (Button, Card, Input, Dialog, ...)
│   │   ├── layout/
│   │   │   ├── AppShell.tsx             -- Header + Sidebar + Main
│   │   │   ├── Header.tsx
│   │   │   ├── Sidebar.tsx
│   │   │   ├── NotificationBell.tsx     -- 未読バッジ
│   │   │   └── LanguageSwitch.tsx
│   │   ├── problem/
│   │   │   ├── ProblemForm.tsx          -- create/edit 共通フォーム
│   │   │   └── ProblemRepositoryInput.tsx -- discriminated union 入力
│   │   ├── answer/
│   │   │   ├── AnswerForm.tsx
│   │   │   ├── AnswerCard.tsx
│   │   │   └── CommentList.tsx
│   │   ├── user/
│   │   │   ├── UserCard.tsx
│   │   │   ├── ProfileForm.tsx
│   │   │   └── AvatarImage.tsx
│   │   └── common/
│   │       ├── ErrorBoundary.tsx
│   │       ├── LoadingSpinner.tsx
│   │       └── PermissionGate.tsx       -- TEACHER 限定 UI のラッパー
│   ├── routes/
│   │   ├── __root.tsx                   -- AppShell レイアウト適用
│   │   ├── index.tsx                    -- /
│   │   ├── login.tsx
│   │   ├── problems/
│   │   │   ├── index.tsx                -- /problems
│   │   │   ├── new.tsx                  -- /problems/new
│   │   │   ├── $id.tsx                  -- /problems/:id
│   │   │   ├── $id.edit.tsx             -- /problems/:id/edit
│   │   │   └── $id.answer.tsx           -- /problems/:id/answer (Submit)
│   │   ├── answers/
│   │   │   ├── index.tsx                -- /answers (My)
│   │   │   └── $id.tsx                  -- /answers/:id
│   │   ├── users/
│   │   │   ├── index.tsx                -- /users
│   │   │   ├── $id.tsx                  -- /users/:id
│   │   │   └── $id.edit.tsx             -- /users/:id/edit
│   │   ├── teachers.tsx                 -- /teachers
│   │   ├── offers.tsx                   -- /offers
│   │   ├── notifications.tsx            -- /notifications
│   │   └── followers.answers.tsx        -- /followers/answers
│   └── styles/
│       └── globals.css                  -- Tailwind base + shadcn/ui CSS variables
└── tests/
    ├── api/
    │   └── client.test.ts
    └── components/
        └── (各 component の smoke test)
```

## API client / Zod スキーマ

例: Problem の sealed `ProblemRepository` に対応する Zod。

```typescript
// src/lib/api/schemas.ts
import { z } from 'zod';

export const githubProblemRepositorySchema = z.object({
  type: z.literal('github'),
  url: z.string().url().max(255),
  branch: z.string().min(1).max(100),
  readmePath: z.string().min(1).max(100),
});

export const bitbucketProblemRepositorySchema = z.object({
  type: z.literal('bitbucket'),
  url: z.string().url().max(255),
  branch: z.string().min(1).max(100),
  readmePath: z.string().min(1).max(100),
});

export const genericProblemRepositorySchema = z.object({
  type: z.literal('generic'),
  url: z.string().url().max(255),
  branch: z.string().min(1).max(100),
});

export const problemRepositorySchema = z.discriminatedUnion('type', [
  githubProblemRepositorySchema,
  bitbucketProblemRepositorySchema,
  genericProblemRepositorySchema,
]);

export const problemSchema = z.object({
  id: z.string().length(21),
  name: z.string().min(1).max(100),
  repository: problemRepositorySchema,
  status: z.enum(['ACTIVE', 'ARCHIVED']),
  problemUrl: z.string().url(),
});
export type Problem = z.infer<typeof problemSchema>;
```

API client は token を `localStorage.kysymysToken` から自動付与:

```typescript
// src/lib/api/client.ts
export async function apiFetch<T>(path: string, init: RequestInit, schema: z.ZodType<T>): Promise<T> {
  const token = localStorage.getItem('kysymysToken');
  const res = await fetch(`/api${path}`, {
    ...init,
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json',
      ...(token ? { 'x-bouncr-credential': token } : {}),
      ...init.headers,
    },
  });
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new ApiError(res.status, body);
  }
  const json = await res.json();
  return schema.parse(json);
}
```

Vite proxy で `/api/*` → `http://localhost:3000/*` (path prefix 無し):

```typescript
// vite.config.ts
export default {
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:3000',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
};
```

## kysymys-app 側の修正 (CORS)

Sub-D で kysymys-app に小さな修正を加える:

- `enkan-web` の `CorsMiddleware` を `KysymysApplicationFactory` に追加
- `KYSYMYS_CORS_ORIGINS` 環境変数で許可 origin を読む (dev: `http://localhost:5173`、本番: 後で設定)
- `Access-Control-Allow-Headers: Content-Type, Accept, x-bouncr-credential`
- `Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS`

これは middleware を1つ追加するだけ。Sub-A spec で "EventBusBindMiddleware を Auth より後に置くと regression する" 既知問題があるので、CORS は ParamsMiddleware の直後 (auth より前) に配置。

## 認可とアクセス制御

各 route が `useAuth()` hook で principal を取得し、permissions に応じて UI を出し分け:

| Route | 必要 permission |
|---|---|
| 全 routes | 認証済み (token あり) |
| `/problems/new`, `/problems/:id/edit`, `/problems/:id/edit` 内の archive ボタン | `TEACHER` |
| `/teachers` の grant role フォーム | `TEACHER` |
| `/users/:id/edit` | path id が自分の userId と一致 |

`PermissionGate` コンポーネントで条件レンダリング:

```tsx
<PermissionGate require="TEACHER">
  <Button>Create new problem</Button>
</PermissionGate>
```

API 側でも認可チェックは効いているので、UI 出し分けは UX 用。

## i18n

- `react-i18next` で日本語/英語を切替
- 翻訳ファイル: `src/i18n/ja.json` / `en.json`、namespace は分けない (small app なので)
- ヘッダーの `LanguageSwitch` で `i18n.changeLanguage('ja' | 'en')`
- 言語選択は `localStorage.kysymysLang` に永続化、初期値はブラウザの `navigator.language`

## レスポンシブ設計

Mobile first で:

- **< 768px** (sm): サイドバーを drawer (Sheet) に格納、ヘッダーにハンバーガーボタン。
- **768px〜 1024px** (md): サイドバー collapse (icon only) + ホバーで展開。
- **>= 1024px** (lg): サイドバー full + メイン最大幅 1280px 中央寄せ。

## テスト

Vitest + Testing Library で:

- API client (`apiFetch`) の Zod parse 失敗時のエラー処理
- `PermissionGate` の出し分けロジック
- 各 form component の入力 validation (Zod schema との整合)
- `LanguageSwitch` の動作

E2E (画面横断シナリオ) は Sub-D では作らず、kysymys-app 側の Hurl で代替。

## 検証

```bash
# 1. backend 起動
cd kysymys-app && mvn exec:java &

# 2. frontend 起動
cd kysymys-frontend && npm install && npm run dev

# 3. Browser で http://localhost:5173 を開く
# 4. Login (dev) に遷移、JWT を貼り付け
TOKEN=$(java /tmp/GenTokenWithRole.java aaaaaaaaaaaaaaaaaaaaa teacher@example.com Tanaka TEACHER)
echo "$TOKEN"  # 貼り付け用

# 5. Dashboard に遷移、各 route を巡回
# 6. <768px viewport で responsive 確認
# 7. 言語切替で日本語/英語確認
# 8. unit test
cd kysymys-frontend && npm run test
# 9. build
cd kysymys-frontend && npm run build

# 10. backend regression
hurl --variable host=http://localhost:3000 --variable token="$TOKEN" \
  kysymys-app/src/test/hurl/{lesson,user,avatar,notification}.hurl
```

## PR 戦略

5 phase 分割 (Sub-A〜C と同方針):

- **Sub-D-1 (toolchain + 基盤)**: kysymys-frontend ディレクトリ新設、Vite + TS + TanStack Router + Tailwind + shadcn/ui setup、AppShell layout、i18n setup、API client wrapper、kysymys-app に CORS 追加
- **Sub-D-2 (auth + dashboard)**: Login (dev) 画面、token 管理、Dashboard、Header の Avatar/Notification ベル/言語切替
- **Sub-D-3 (Lesson 画面群)**: Problems list/detail/new/edit、Submit Answer、My Answers、Answer detail、Following feed
- **Sub-D-4 (User 画面群)**: Users 検索、User profile/edit、Teachers + GrantTeacherRole、Offers
- **Sub-D-5 (Notification + 仕上げ)**: Notifications center、Avatar 統合、エラー処理、責任分界点の整理、最終 build/test

## 参照する外部リソース

- [TanStack Router](https://tanstack.com/router) — type-safe routing
- [TanStack Query v5](https://tanstack.com/query) — async state
- [shadcn/ui](https://ui.shadcn.com/) — component primitives
- [Tailwind CSS v3](https://tailwindcss.com/)
- [Radix UI](https://www.radix-ui.com/)
- [Zod](https://zod.dev/)
- [react-i18next](https://react.i18next.com/)

## 既知のリスク / 注意点

1. **CORS 設定**: kysymys-app の現在の middleware order を壊さないよう、`ContentNegotiationMiddleware` の前 (ParamsMiddleware 直後) に CORS を挿入。Auth の後に middleware を入れると POST 201→200 化け regression が出る (Sub-C で経験済み)。
2. **shadcn/ui のセットアップ**: `npx shadcn-ui@latest init` を実行し、必要な component を都度 `npx shadcn-ui@latest add button card input dialog ...` で追加する。コードは `src/components/ui/` に直接コピーされる。
3. **TanStack Router の file-based routing**: `src/routes/` の file 構造から自動生成。プラグイン `@tanstack/router-plugin/vite` で dev 時に自動再生成。
4. **Bouncr 本物接続時の影響**: Sub-F で OIDC redirect 認証になると、現在の hard-coded JWT 入力フローは廃止。`/login` route は捨てて、未認証時は Bouncr login page に redirect する形に変わる。Sub-D 設計時にこの将来変更が痛みなく行えるよう、`useAuth()` の責務を「token を取得 / 保存 / 検証する」に限定する。
