import { useEffect, type ReactNode } from "react";
import { useNavigate, useLocation } from "@tanstack/react-router";
import { getToken } from "@/lib/auth";

export function RequireAuth({ children }: { children: ReactNode }) {
  const navigate = useNavigate();
  const location = useLocation();
  const token = getToken();

  useEffect(() => {
    if (!token && location.pathname !== "/login") {
      navigate({ to: "/login", search: { redirect: location.pathname } });
    }
  }, [token, location.pathname, navigate]);

  if (!token) return null;
  return <>{children}</>;
}
