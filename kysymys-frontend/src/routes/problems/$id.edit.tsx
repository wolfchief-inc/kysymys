import { useState } from "react";
import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { ProblemForm } from "@/components/problem/ProblemForm";
import { LoadingSpinner } from "@/components/common/LoadingSpinner";
import { ApiError } from "@/lib/api/client";
import { getProblem, updateProblem } from "@/lib/api/lesson";

function ProblemEdit() {
  const { t } = useTranslation();
  const { id } = Route.useParams();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const [error, setError] = useState<string | null>(null);

  const { data, isLoading } = useQuery({
    queryKey: ["problem", id],
    queryFn: () => getProblem(id),
  });

  const update = useMutation({
    mutationFn: (input: Parameters<typeof updateProblem>[1]) =>
      updateProblem(id, input),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["problems"] });
      qc.invalidateQueries({ queryKey: ["problem", id] });
      navigate({ to: "/problems/$id", params: { id } });
    },
    onError: (e: unknown) => {
      setError(e instanceof ApiError ? `${e.status} ${e.message}` : String(e));
    },
  });

  if (isLoading || !data) return <LoadingSpinner />;

  return (
    <div className="mx-auto max-w-2xl">
      <Card>
        <CardHeader>
          <CardTitle>{t("problem.edit")}</CardTitle>
        </CardHeader>
        <CardContent>
          <ProblemForm
            initial={data}
            submitting={update.isPending}
            error={error}
            onSubmit={(input) => {
              setError(null);
              update.mutate(input);
            }}
            onCancel={() => navigate({ to: "/problems/$id", params: { id } })}
          />
        </CardContent>
      </Card>
    </div>
  );
}

export const Route = createFileRoute("/problems/$id/edit")({
  component: ProblemEdit,
});
