import { createFileRoute } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { LoadingSpinner } from "@/components/common/LoadingSpinner";
import { listWhatsNews, markWhatsNewRead } from "@/lib/api/notification";
import type { WhatsNew } from "@/lib/api/schemas";

function renderBody(t: (k: string, opts?: Record<string, unknown>) => string, n: WhatsNew) {
  const params = (n.params ?? {}) as Record<string, string>;
  const known = new Set(["submittedAnswer", "offeredToFollow"]);
  if (known.has(n.templatePath)) {
    return t(`notification.templates.${n.templatePath}`, {
      actor: params.actorName ?? params.actorId ?? "?",
    });
  }
  return n.templatePath;
}

function Notifications() {
  const { t } = useTranslation();
  const qc = useQueryClient();
  const { data, isLoading } = useQuery({
    queryKey: ["whats-news"],
    queryFn: listWhatsNews,
    refetchInterval: 10000,
  });

  const markRead = useMutation({
    mutationFn: (id: string) => markWhatsNewRead(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["whats-news"] }),
  });

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">{t("notification.list")}</h1>
      {isLoading ? (
        <LoadingSpinner />
      ) : (data ?? []).length === 0 ? (
        <p className="text-muted-foreground">{t("dashboard.empty")}</p>
      ) : (
        <div className="space-y-3">
          {data!.map((n) => (
            <Card
              key={n.id}
              className={n.unread ? "border-primary/40 bg-primary/5" : undefined}
            >
              <CardHeader>
                <CardTitle className="flex items-center justify-between gap-2">
                  <span className="text-base font-medium">
                    {renderBody(t, n)}
                  </span>
                  <Badge variant={n.unread ? "default" : "secondary"}>
                    {n.unread ? t("notification.unread") : t("notification.read")}
                  </Badge>
                </CardTitle>
              </CardHeader>
              <CardContent className="flex items-center justify-between gap-3 text-sm text-muted-foreground">
                <span>{new Date(n.postedAt).toLocaleString()}</span>
                {n.unread && (
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => markRead.mutate(n.id)}
                    disabled={markRead.isPending}
                  >
                    {t("notification.markRead")}
                  </Button>
                )}
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}

export const Route = createFileRoute("/notifications")({
  component: Notifications,
});
