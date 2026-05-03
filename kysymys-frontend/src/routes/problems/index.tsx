import { createFileRoute, Link } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { LoadingSpinner } from "@/components/common/LoadingSpinner";
import { PermissionGate } from "@/components/common/PermissionGate";
import { listProblems } from "@/lib/api/lesson";

function ProblemsIndex() {
  const { t } = useTranslation();
  const { data, isLoading } = useQuery({
    queryKey: ["problems"],
    queryFn: listProblems,
  });

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">{t("problem.list")}</h1>
        <PermissionGate permission="TEACHER">
          <Button asChild>
            <Link to="/problems/new">
              <Plus className="h-4 w-4" />
              {t("problem.new")}
            </Link>
          </Button>
        </PermissionGate>
      </div>

      {isLoading ? (
        <LoadingSpinner />
      ) : (data ?? []).length === 0 ? (
        <p className="text-muted-foreground">{t("dashboard.empty")}</p>
      ) : (
        <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
          {data!.map((p) => (
            <Card key={p.id}>
              <CardHeader>
                <CardTitle className="flex items-center justify-between gap-2">
                  <Link
                    to="/problems/$id"
                    params={{ id: p.id }}
                    className="truncate hover:underline"
                  >
                    {p.name}
                  </Link>
                  <Badge variant="outline">{p.repository.type}</Badge>
                </CardTitle>
              </CardHeader>
              <CardContent className="truncate text-sm text-muted-foreground">
                {p.repository.url}
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}

export const Route = createFileRoute("/problems/")({ component: ProblemsIndex });
