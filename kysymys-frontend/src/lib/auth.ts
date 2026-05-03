const TOKEN_KEY = "kysymysToken";

export type JwtPayload = {
  sub?: string;
  email?: string;
  name?: string;
  permissions?: string[];
  exp?: number;
  iat?: number;
};

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}

function base64UrlDecode(s: string): string {
  const pad = "=".repeat((4 - (s.length % 4)) % 4);
  const b64 = (s + pad).replace(/-/g, "+").replace(/_/g, "/");
  return atob(b64);
}

export function decodeJwt(token: string): JwtPayload | null {
  try {
    const parts = token.split(".");
    if (parts.length !== 3) return null;
    const json = base64UrlDecode(parts[1]);
    return JSON.parse(decodeURIComponent(escape(json))) as JwtPayload;
  } catch {
    return null;
  }
}

export function getCurrentPayload(): JwtPayload | null {
  const t = getToken();
  return t ? decodeJwt(t) : null;
}

export function hasPermission(permission: string): boolean {
  return getCurrentPayload()?.permissions?.includes(permission) ?? false;
}

export function isTeacher(): boolean {
  return hasPermission("TEACHER");
}

export function getCurrentUserId(): string | null {
  return getCurrentPayload()?.sub ?? null;
}
