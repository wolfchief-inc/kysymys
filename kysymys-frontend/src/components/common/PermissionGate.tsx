import type { ReactNode } from "react";
import { hasPermission } from "@/lib/auth";

type Props = {
  permission: string;
  children: ReactNode;
  fallback?: ReactNode;
};

export function PermissionGate({ permission, children, fallback = null }: Props) {
  return hasPermission(permission) ? <>{children}</> : <>{fallback}</>;
}
