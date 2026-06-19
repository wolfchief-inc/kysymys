import { useEffect, useState } from "react";
import { createFileRoute } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { Check, X } from "lucide-react";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { LoadingSpinner } from "@/components/common/LoadingSpinner";
import { PermissionGate } from "@/components/common/PermissionGate";
import { getActivityStatus } from "@/lib/api/activity";
import type { ActivityParticipant } from "@/lib/api/schemas";
import { ApiError } from "@/lib/api/client";
import { cn } from "@/lib/utils";

type Severity = "ok" | "warn" | "alert";

function idleSeconds(serverTime: string, lastActivityAt: string | null): number | null {
  if (!lastActivityAt) return null;
  const server = new Date(serverTime).getTime();
  const last = new Date(lastActivityAt).getTime();
  if (Number.isNaN(server) || Number.isNaN(last)) return null;
  return Math.max(0, Math.floor((server - last) / 1000));
}

function severityOf(idle: number | null, stuck: boolean): Severity {
  if (stuck) return "alert";
  if (idle === null) return "alert";
  if (idle > 600) return "alert";
  if (idle >= 120) return "warn";
  return "ok";
}

function formatDuration(
  seconds: number | null,
  t: (key: string) => string,
): string {
  if (seconds === null) return "—";
  if (seconds < 5) return t("instructorDashboard.justNow");
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  if (m === 0) return `${s}s`;
  return `${m}m ${s}s`;
}

function rankKey(p: ActivityParticipant, serverTime: string) {
  const idle = idleSeconds(serverTime, p.lastActivityAt);
  return {
    stuck: p.stuck ? 1 : 0,
    idle: idle ?? Number.MAX_SAFE_INTEGER,
    failing: p.lastBuildKind === "BUILD_FAILURE" ? 1 : 0,
  };
}

function sortParticipants(
  participants: ActivityParticipant[],
  serverTime: string,
): ActivityParticipant[] {
  return [...participants].sort((a, b) => {
    const ka = rankKey(a, serverTime);
    const kb = rankKey(b, serverTime);
    if (ka.stuck !== kb.stuck) return kb.stuck - ka.stuck;
    if (ka.idle !== kb.idle) return kb.idle - ka.idle;
    if (ka.failing !== kb.failing) return kb.failing - ka.failing;
    return a.participantId.localeCompare(b.participantId);
  });
}

const rowClassBySeverity: Record<Severity, string> = {
  ok: "border-l-4 border-l-green-500",
  warn: "border-l-4 border-l-amber-500",
  alert: "border-l-4 border-l-destructive",
};

function ParticipantRow({
  p,
  serverTime,
}: {
  p: ActivityParticipant;
  serverTime: string;
}) {
  const { t } = useTranslation();
  const idle = idleSeconds(serverTime, p.lastActivityAt);
  const severity = severityOf(idle, p.stuck);
  const failing = p.lastBuildKind === "BUILD_FAILURE";

  return (
    <Card className={cn("overflow-hidden", rowClassBySeverity[severity])}>
      <CardContent className="flex flex-wrap items-center gap-x-6 gap-y-2 p-4">
        <div className="min-w-[140px] flex-1">
          <div className="flex items-center gap-2">
            <span className="font-mono font-semibold">{p.participantId}</span>
            {p.stuck && (
              <Badge variant="destructive">
                {t("instructorDashboard.stuck")}
              </Badge>
            )}
          </div>
          <div className="text-xs text-muted-foreground">
            {p.problemId ?? t("instructorDashboard.noProblem")}
          </div>
        </div>

        <div className="min-w-[90px] text-sm">
          <div className="text-xs text-muted-foreground">
            {t("instructorDashboard.idle")}
          </div>
          <span
            className={cn(
              "font-medium",
              severity === "alert" && "text-destructive",
              severity === "warn" && "text-amber-600",
              severity === "ok" && "text-green-600",
            )}
          >
            {formatDuration(idle, t)}
          </span>
        </div>

        <div className="min-w-[160px] flex-1 text-sm">
          <div className="text-xs text-muted-foreground">
            {t("instructorDashboard.lastBuild")}
          </div>
          {p.lastBuildKind === null ? (
            <span className="text-muted-foreground">—</span>
          ) : failing ? (
            <div className="flex items-start gap-1.5 text-destructive">
              <X className="mt-0.5 h-4 w-4 shrink-0" />
              <span className="font-mono text-xs break-all">
                {p.lastBuildDetail ?? t("instructorDashboard.buildFailed")}
              </span>
            </div>
          ) : (
            <div className="flex items-center gap-1.5 text-green-600">
              <Check className="h-4 w-4 shrink-0" />
              <span>{t("instructorDashboard.buildPassed")}</span>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
}

function DashboardContent() {
  const { t } = useTranslation();
  const { data, isLoading, isError, error, dataUpdatedAt } = useQuery({
    queryKey: ["activity-status"],
    queryFn: getActivityStatus,
    refetchInterval: 3000,
  });

  // Tick once a second so "updated Ns ago" advances between polls.
  const [now, setNow] = useState(() => Date.now());
  useEffect(() => {
    const id = setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(id);
  }, []);

  if (isLoading) return <LoadingSpinner />;

  if (isError) {
    const message =
      error instanceof ApiError ? `${error.status} ${error.message}` : String(error);
    return (
      <Card>
        <CardHeader>
          <CardTitle>{t("errors.title")}</CardTitle>
        </CardHeader>
        <CardContent className="text-sm text-destructive">{message}</CardContent>
      </Card>
    );
  }

  const serverTime = data!.serverTime;
  const sorted = sortParticipants(data!.participants, serverTime);
  const updatedSecondsAgo = Math.max(
    0,
    Math.floor((now - dataUpdatedAt) / 1000),
  );

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-2 text-xs text-muted-foreground">
        <span className="inline-block h-2 w-2 animate-pulse rounded-full bg-green-500" />
        <span>
          {t("instructorDashboard.updatedAgo", { seconds: updatedSecondsAgo })}
        </span>
      </div>

      {sorted.length === 0 ? (
        <p className="text-muted-foreground">
          {t("instructorDashboard.empty")}
        </p>
      ) : (
        <div className="space-y-2">
          {sorted.map((p) => (
            <ParticipantRow
              key={p.participantId}
              p={p}
              serverTime={serverTime}
            />
          ))}
        </div>
      )}
    </div>
  );
}

function InstructorDashboard() {
  const { t } = useTranslation();
  return (
    <div className="space-y-5">
      <h1 className="text-2xl font-bold">{t("instructorDashboard.title")}</h1>
      <PermissionGate
        permission="TEACHER"
        fallback={
          <p className="text-muted-foreground">{t("errors.forbidden")}</p>
        }
      >
        <DashboardContent />
      </PermissionGate>
    </div>
  );
}

export const Route = createFileRoute("/dashboard")({
  component: InstructorDashboard,
});
