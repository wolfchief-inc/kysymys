import { createFileRoute } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { LoadingSpinner } from "@/components/common/LoadingSpinner";
import { AnswerCard } from "@/components/answer/AnswerCard";
import { listFollowerAnswers } from "@/lib/api/lesson";

function FollowerAnswers() {
  const { t } = useTranslation();
  const { data, isLoading } = useQuery({
    queryKey: ["follower-answers"],
    queryFn: listFollowerAnswers,
  });

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">{t("nav.followers")}</h1>
      {isLoading ? (
        <LoadingSpinner />
      ) : (data ?? []).length === 0 ? (
        <p className="text-muted-foreground">{t("dashboard.empty")}</p>
      ) : (
        <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
          {data!.map((a) => (
            <AnswerCard key={a.id} answer={a} />
          ))}
        </div>
      )}
    </div>
  );
}

export const Route = createFileRoute("/followers/answers")({
  component: FollowerAnswers,
});
