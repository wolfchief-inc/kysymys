import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { Pencil, ExternalLink, Archive } from "lucide-react";
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
import { archiveProblem, getProblem } from "@/lib/api/lesson";

function ProblemDetail() {
  const { t } = useTranslation();
  const { id } = Route.useParams();
  const navigate = useNavigate();
  const qc = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ["problem", id],
    queryFn: () => getProblem(id),
  });

  const archive = useMutation({
    mutationFn: () => archiveProblem(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["problems"] });
      navigate({ to: "/problems" });
    },
  });

  if (isLoading) return <LoadingSpinner />;
  if (!data) return <p>{t("errors.notFound")}</p>;

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="space-y-1">
          <h1 className="text-2xl font-bold">{data.name}</h1>
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <Badge variant="outline">{data.repository.type}</Badge>
            <Badge
              variant={data.status === "ACTIVE" ? "default" : "secondary"}
            >
              {data.status}
            </Badge>
          </div>
        </div>
        <div className="flex gap-2">
          <Button asChild>
            <Link to="/problems/$id/answer" params={{ id }}>
              {t("problem.submitAnswer")}
            </Link>
          </Button>
          <PermissionGate permission="TEACHER">
            <Button variant="outline" asChild>
              <Link to="/problems/$id/edit" params={{ id }}>
                <Pencil className="h-4 w-4" />
                {t("common.edit")}
              </Link>
            </Button>
            {data.status === "ACTIVE" && (
              <Button
                variant="destructive"
                onClick={() => archive.mutate()}
                disabled={archive.isPending}
              >
                <Archive className="h-4 w-4" />
                {t("problem.archive")}
              </Button>
            )}
          </PermissionGate>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>{t("problem.repository")}</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2 text-sm">
          <a
            href={data.problemUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-1 text-primary hover:underline"
          >
            {t("problem.url")} <ExternalLink className="h-3 w-3" />
          </a>
          <pre className="rounded-md bg-muted p-3 font-mono text-xs">
            {JSON.stringify(data.repository, null, 2)}
          </pre>
        </CardContent>
      </Card>
    </div>
  );
}

export const Route = createFileRoute("/problems/$id")({
  component: ProblemDetail,
});
