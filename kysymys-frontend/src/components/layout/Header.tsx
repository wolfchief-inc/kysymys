import { Link } from "@tanstack/react-router";
import { useTranslation } from "react-i18next";
import { Menu, LogOut } from "lucide-react";
import { Button } from "@/components/ui/button";
import { LanguageSwitch } from "./LanguageSwitch";
import { NotificationBell } from "./NotificationBell";
import { AvatarImage } from "@/components/user/AvatarImage";
import { clearToken, getCurrentPayload, getCurrentUserId, getToken } from "@/lib/auth";

export function Header({ onMenuClick }: { onMenuClick?: () => void }) {
  const { t } = useTranslation();
  const userId = getCurrentUserId();
  const payload = getCurrentPayload();
  const token = getToken();

  const handleLogout = () => {
    clearToken();
    window.location.href = "/login";
  };

  return (
    <header className="sticky top-0 z-30 flex h-14 items-center justify-between border-b border-border bg-background/90 px-3 backdrop-blur sm:px-4">
      <div className="flex items-center gap-2">
        <Button
          variant="ghost"
          size="icon"
          className="md:hidden"
          onClick={onMenuClick}
          aria-label="Menu"
        >
          <Menu className="h-5 w-5" />
        </Button>
        <Link to="/" className="flex items-baseline gap-2">
          <span className="text-lg font-bold">{t("app.name")}</span>
          <span className="hidden text-xs text-muted-foreground sm:inline">
            {t("app.tagline")}
          </span>
        </Link>
      </div>

      <div className="flex items-center gap-1 sm:gap-2">
        <LanguageSwitch />
        {token && (
          <>
            <NotificationBell />
            {userId && (
              <Link
                to="/users/$id"
                params={{ id: userId }}
                className="flex items-center gap-2 rounded-md px-2 py-1 text-sm hover:bg-accent"
              >
                <AvatarImage userId={userId} size={28} />
                <span className="hidden max-w-[120px] truncate sm:inline">
                  {payload?.name ?? userId.slice(0, 8)}
                </span>
              </Link>
            )}
            <Button
              variant="ghost"
              size="icon"
              onClick={handleLogout}
              aria-label={t("auth.logout")}
            >
              <LogOut className="h-5 w-5" />
            </Button>
          </>
        )}
      </div>
    </header>
  );
}
