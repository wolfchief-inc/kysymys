import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { z } from "zod";
import { ApiError, apiFetch } from "@/lib/api/client";

const TOKEN_KEY = "kysymysToken";

beforeEach(() => {
  localStorage.clear();
  globalThis.fetch = vi.fn();
});

afterEach(() => {
  vi.restoreAllMocks();
});

const respond = (status: number, body: unknown, contentType = "application/json") =>
  new Response(body === undefined ? null : JSON.stringify(body), {
    status,
    headers: { "content-type": contentType },
  });

describe("apiFetch", () => {
  it("parses a successful JSON response with the provided schema", async () => {
    const schema = z.object({ status: z.literal("ok") });
    vi.mocked(fetch).mockResolvedValueOnce(respond(200, { status: "ok" }));

    const result = await apiFetch("/health", {}, schema);
    expect(result).toEqual({ status: "ok" });
  });

  it("attaches x-bouncr-credential header when a token is set", async () => {
    localStorage.setItem(TOKEN_KEY, "abc.def.ghi");
    vi.mocked(fetch).mockResolvedValueOnce(respond(200, {}));

    await apiFetch("/me", {});

    const [, init] = vi.mocked(fetch).mock.calls[0]!;
    const headers = new Headers(init?.headers);
    expect(headers.get("x-bouncr-credential")).toBe("abc.def.ghi");
  });

  it("clears token and throws on 401", async () => {
    localStorage.setItem(TOKEN_KEY, "stale");
    vi.mocked(fetch).mockResolvedValueOnce(respond(401, { error: "nope" }));

    await expect(apiFetch("/me")).rejects.toBeInstanceOf(ApiError);
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
  });

  it("throws ApiError with body on non-ok response", async () => {
    vi.mocked(fetch).mockResolvedValueOnce(
      respond(400, { violations: [{ field: "/name", code: "MIN_LENGTH" }] }),
    );

    try {
      await apiFetch("/problems", { method: "POST", body: { name: "" } });
      expect.fail("expected ApiError");
    } catch (e) {
      expect(e).toBeInstanceOf(ApiError);
      expect((e as ApiError).status).toBe(400);
    }
  });

  it("rejects when JSON body fails schema validation", async () => {
    const schema = z.object({ status: z.literal("ok") });
    vi.mocked(fetch).mockResolvedValueOnce(respond(200, { status: "wrong" }));

    await expect(apiFetch("/health", {}, schema)).rejects.toBeTruthy();
  });
});
