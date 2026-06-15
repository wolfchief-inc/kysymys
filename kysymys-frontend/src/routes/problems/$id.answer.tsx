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
import { AnswerForm } from "@/components/answer/AnswerForm";
import { ApiError } from "@/lib/api/client";
import { submitAnswer } from "@/lib/api/lesson";

function ProblemAnswer() {
  const { t } = useTranslation();
  const { id } = Route.useParams();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const [error, setError] = useState<string | null>(null);

  const submit = useMutation({
    mutationFn: (input: Parameters<typeof submitAnswer>[1]) =>
      submitAnswer(id, input),
    onSuccess: (a) => {
      qc.invalidateQueries({ queryKey: ["my-answers"] });
      navigate({ to: "/answers/$id", params: { id: a.id } });
    },
    onError: (e: unknown) => {
      setError(e instanceof ApiError ? `${e.status} ${e.message}` : String(e));
    },
  });

  return (
    <div className="mx-auto max-w-2xl">
      <Card>
        <CardHeader>
          <CardTitle>{t("answer.submit")}</CardTitle>
        </CardHeader>
        <CardContent>
          <AnswerForm
            submitting={submit.isPending}
            error={error}
            onSubmit={(input) => {
              setError(null);
              submit.mutate(input);
            }}
            onCancel={() =>
              navigate({ to: "/problems/$id", params: { id } })
            }
          />
        </CardContent>
      </Card>
    </div>
  );
}

export const Route = createFileRoute("/problems/$id/answer")({
  component: ProblemAnswer,
});
