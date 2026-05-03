import { useState } from "react";
import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { ProblemForm } from "@/components/problem/ProblemForm";
import { ApiError } from "@/lib/api/client";
import { createProblem } from "@/lib/api/lesson";

function ProblemNew() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const [error, setError] = useState<string | null>(null);

  const create = useMutation({
    mutationFn: createProblem,
    onSuccess: (p) => {
      qc.invalidateQueries({ queryKey: ["problems"] });
      navigate({ to: "/problems/$id", params: { id: p.id } });
    },
    onError: (e: unknown) => {
      setError(e instanceof ApiError ? `${e.status} ${e.message}` : String(e));
    },
  });

  return (
    <div className="mx-auto max-w-2xl">
      <Card>
        <CardHeader>
          <CardTitle>{t("problem.new")}</CardTitle>
        </CardHeader>
        <CardContent>
          <ProblemForm
            submitting={create.isPending}
            error={error}
            onSubmit={(input) => {
              setError(null);
              create.mutate(input);
            }}
            onCancel={() => navigate({ to: "/problems" })}
          />
        </CardContent>
      </Card>
    </div>
  );
}

export const Route = createFileRoute("/problems/new")({
  component: ProblemNew,
});
