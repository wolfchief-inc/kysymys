import { Link, useLocation } from "@tanstack/react-router";
import { useTranslation } from "react-i18next";
import {
  LayoutDashboard,
  BookOpen,
  PencilLine,
  Users,
  GraduationCap,
  Mail,
  Bell,
  UsersRound,
  MonitorDot,
} from "lucide-react";
import { cn } from "@/lib/utils";
import { isTeacher } from "@/lib/auth";

type Item = {
  to: string;
  label: string;
  icon: React.ComponentType<{ className?: string }>;
};

export function Sidebar({ onNavigate }: { onNavigate?: () => void }) {
  const { t } = useTranslation();
  const location = useLocation();

  const items: Item[] = [
    { to: "/", label: t("nav.dashboard"), icon: LayoutDashboard },
    { to: "/problems", label: t("nav.problems"), icon: BookOpen },
    { to: "/answers", label: t("nav.myAnswers"), icon: PencilLine },
    { to: "/followers/answers", label: t("nav.followers"), icon: UsersRound },
    { to: "/users", label: t("nav.users"), icon: Users },
    { to: "/teachers", label: t("nav.teachers"), icon: GraduationCap },
    ...(isTeacher()
      ? [
          {
            to: "/dashboard",
            label: t("nav.instructorDashboard"),
            icon: MonitorDot,
          },
        ]
      : []),
    { to: "/offers", label: t("nav.offers"), icon: Mail },
    { to: "/notifications", label: t("nav.notifications"), icon: Bell },
  ];

  return (
    <nav className="flex flex-col gap-1 p-3">
      {items.map(({ to, label, icon: Icon }) => {
        const active =
          to === "/"
            ? location.pathname === "/"
            : location.pathname === to ||
              location.pathname.startsWith(`${to}/`);
        return (
          <Link
            key={to}
            to={to}
            onClick={onNavigate}
            className={cn(
              "flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors",
              active
                ? "bg-accent text-accent-foreground"
                : "text-muted-foreground hover:bg-accent hover:text-accent-foreground",
            )}
          >
            <Icon className="h-4 w-4 shrink-0" />
            <span className="truncate">{label}</span>
          </Link>
        );
      })}
    </nav>
  );
}
