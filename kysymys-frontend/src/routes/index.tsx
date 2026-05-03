import { createFileRoute } from "@tanstack/react-router";
import { useTranslation } from "react-i18next";

function DashboardRoute() {
  const { t } = useTranslation();
  return (
    <div>
      <h1 className="text-2xl font-bold">{t("dashboard.title")}</h1>
      <p className="mt-2 text-muted-foreground">
        Phase 2 will populate this dashboard.
      </p>
    </div>
  );
}

export const Route = createFileRoute("/")({ component: DashboardRoute });
