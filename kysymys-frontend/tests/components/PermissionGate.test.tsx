import { describe, expect, it, beforeEach } from "vitest";
import { render, screen } from "@testing-library/react";
import { PermissionGate } from "@/components/common/PermissionGate";

const TOKEN_KEY = "kysymysToken";

function tokenWithPermissions(perms: string[]): string {
  const header = btoa(JSON.stringify({ alg: "HS256" }))
    .replace(/=+$/, "")
    .replace(/\+/g, "-")
    .replace(/\//g, "_");
  const payload = btoa(
    JSON.stringify({ sub: "x".repeat(21), permissions: perms }),
  )
    .replace(/=+$/, "")
    .replace(/\+/g, "-")
    .replace(/\//g, "_");
  return `${header}.${payload}.sig`;
}

beforeEach(() => {
  localStorage.clear();
});

describe("PermissionGate", () => {
  it("renders children when permission claim is present", () => {
    localStorage.setItem(TOKEN_KEY, tokenWithPermissions(["TEACHER"]));
    render(
      <PermissionGate permission="TEACHER">
        <span>granted</span>
      </PermissionGate>,
    );
    expect(screen.getByText("granted")).toBeInTheDocument();
  });

  it("hides children and shows fallback when permission missing", () => {
    localStorage.setItem(TOKEN_KEY, tokenWithPermissions(["STUDENT"]));
    render(
      <PermissionGate permission="TEACHER" fallback={<span>denied</span>}>
        <span>granted</span>
      </PermissionGate>,
    );
    expect(screen.queryByText("granted")).not.toBeInTheDocument();
    expect(screen.getByText("denied")).toBeInTheDocument();
  });

  it("hides children when no token at all", () => {
    render(
      <PermissionGate permission="TEACHER">
        <span>granted</span>
      </PermissionGate>,
    );
    expect(screen.queryByText("granted")).not.toBeInTheDocument();
  });
});
