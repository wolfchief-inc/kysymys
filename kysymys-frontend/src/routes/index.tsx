import { createFileRoute, Link } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { LoadingSpinner } from "@/components/common/LoadingSpinner";
import { listWhatsNews } from "@/lib/api/notification";
import { listFollowerAnswers, listMyAnswers } from "@/lib/api/lesson";
import type { WhatsNew, Answer } from "@/lib/api/schemas";

function DashboardRoute() {
  const { t } = useTranslation();

  const whatsNews = useQuery({ queryKey: ["whats-news"], queryFn: listWhatsNews });
  const myAnswers = useQuery({ queryKey: ["my-answers"], queryFn: listMyAnswers });
  const followers = useQuery({
    queryKey: ["follower-answers"],
    queryFn: listFollowerAnswers,
  });

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">{t("dashboard.title")}</h1>
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <Card>
          <CardHeader>
            <CardTitle>{t("dashboard.whatsNew")}</CardTitle>
          </CardHeader>
          <CardContent>
            {whatsNews.isLoading ? (
              <LoadingSpinner />
            ) : (whatsNews.data ?? []).length === 0 ? (
              <p className="text-sm text-muted-foreground">
                {t("dashboard.empty")}
              </p>
            ) : (
              <ul className="space-y-2 text-sm">
                {(whatsNews.data ?? []).slice(0, 5).map((n: WhatsNew) => (
                  <li key={n.id} className="flex justify-between gap-2">
                    <Link
                      to="/notifications"
                      className="truncate hover:underline"
                    >
                      {n.templatePath}
                    </Link>
                    {n.unread && (
                      <span className="text-[10px] text-destructive">●</span>
                    )}
                  </li>
                ))}
              </ul>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>{t("dashboard.myRecentAnswers")}</CardTitle>
          </CardHeader>
          <CardContent>
            {myAnswers.isLoading ? (
              <LoadingSpinner />
            ) : (myAnswers.data ?? []).length === 0 ? (
              <p className="text-sm text-muted-foreground">
                {t("dashboard.empty")}
              </p>
            ) : (
              <ul className="space-y-2 text-sm">
                {(myAnswers.data ?? []).slice(0, 5).map((a: Answer) => (
                  <li key={a.id} className="truncate">
                    <Link
                      to="/answers/$id"
                      params={{ id: a.id }}
                      className="hover:underline"
                    >
                      {a.repository.url}
                    </Link>
                  </li>
                ))}
              </ul>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>{t("dashboard.followersAnswers")}</CardTitle>
          </CardHeader>
          <CardContent>
            {followers.isLoading ? (
              <LoadingSpinner />
            ) : (followers.data ?? []).length === 0 ? (
              <p className="text-sm text-muted-foreground">
                {t("dashboard.empty")}
              </p>
            ) : (
              <ul className="space-y-2 text-sm">
                {(followers.data ?? []).slice(0, 5).map((a: Answer) => (
                  <li key={a.id} className="truncate">
                    <Link
                      to="/answers/$id"
                      params={{ id: a.id }}
                      className="hover:underline"
                    >
                      {a.repository.url}
                    </Link>
                  </li>
                ))}
              </ul>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

export const Route = createFileRoute("/")({ component: DashboardRoute });
