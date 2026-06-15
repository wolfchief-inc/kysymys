import { z } from "zod";
import { clearToken, getToken } from "@/lib/auth";

export class ApiError extends Error {
  status: number;
  body?: unknown;

  constructor(status: number, message: string, body?: unknown) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.body = body;
  }
}

type FetchInit = Omit<RequestInit, "body"> & { body?: unknown };

export async function apiFetch<T>(
  path: string,
  init: FetchInit = {},
  schema?: z.ZodType<T>,
): Promise<T> {
  const headers = new Headers(init.headers);
  headers.set("Accept", "application/json");
  if (init.body !== undefined) {
    headers.set("Content-Type", "application/json");
  }
  const token = getToken();
  if (token) {
    headers.set("x-bouncr-credential", token);
  }

  const url = path.startsWith("http") ? path : `/api${path}`;
  const res = await fetch(url, {
    ...init,
    headers,
    body: init.body !== undefined ? JSON.stringify(init.body) : undefined,
  });

  if (res.status === 401) {
    clearToken();
    throw new ApiError(401, "Unauthorized");
  }
  if (res.status === 204) {
    return undefined as T;
  }

  let data: unknown = undefined;
  const ct = res.headers.get("content-type") ?? "";
  if (ct.includes("application/json") || ct.includes("application/problem+json")) {
    data = await res.json();
  } else {
    const text = await res.text();
    data = text.length ? text : undefined;
  }

  if (!res.ok) {
    throw new ApiError(res.status, `HTTP ${res.status}`, data);
  }

  if (!schema) return data as T;
  return schema.parse(data);
}

export const apiUrl = (path: string) => `/api${path}`;
