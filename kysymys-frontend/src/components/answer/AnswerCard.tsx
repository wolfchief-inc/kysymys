import { Link } from "@tanstack/react-router";
import { useTranslation } from "react-i18next";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import type { Answer } from "@/lib/api/schemas";

export function AnswerCard({ answer }: { answer: Answer }) {
  const { t } = useTranslation();
  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center justify-between gap-2">
          <Link
            to="/answers/$id"
            params={{ id: answer.id }}
            className="hover:underline"
          >
            {answer.repository.url}
          </Link>
          <Badge variant="outline">{answer.repository.type}</Badge>
        </CardTitle>
      </CardHeader>
      <CardContent className="text-sm text-muted-foreground">
        <div className="font-mono text-xs">
          {answer.latestCommitHash?.slice(0, 12) ?? "-"}
        </div>
        {answer.comments.length > 0 && (
          <div className="mt-1">
            {t("answer.comments")}: {answer.comments.length}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
