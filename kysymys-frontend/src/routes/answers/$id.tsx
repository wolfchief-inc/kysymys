import { useState } from "react";
import { createFileRoute } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { LoadingSpinner } from "@/components/common/LoadingSpinner";
import { CommentList } from "@/components/answer/CommentList";
import { AvatarImage } from "@/components/user/AvatarImage";
import { getAnswer, postComment } from "@/lib/api/lesson";

function AnswerDetail() {
  const { t } = useTranslation();
  const { id } = Route.useParams();
  const qc = useQueryClient();
  const [comment, setComment] = useState("");

  const { data, isLoading } = useQuery({
    queryKey: ["answer", id],
    queryFn: () => getAnswer(id),
  });

  const post = useMutation({
    mutationFn: () => postComment(id, { description: comment }),
    onSuccess: () => {
      setComment("");
      qc.invalidateQueries({ queryKey: ["answer", id] });
    },
  });

  if (isLoading) return <LoadingSpinner />;
  if (!data) return <p>{t("errors.notFound")}</p>;

  return (
    <div className="space-y-5">
      <div className="space-y-1">
        <h1 className="break-all text-2xl font-bold">
          {data.repository.url}
        </h1>
        <div className="flex items-center gap-2">
          <Badge variant="outline">{data.repository.type}</Badge>
          <AvatarImage userId={data.answererId} size={24} />
          <span className="font-mono text-xs text-muted-foreground">
            {data.answererId.slice(0, 8)}
          </span>
        </div>
      </div>

      {data.latestCommitHash && (
        <Card>
          <CardHeader>
            <CardTitle>{t("answer.commitHash")}</CardTitle>
          </CardHeader>
          <CardContent>
            <a
              href={data.answerUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="font-mono text-xs text-primary hover:underline"
            >
              {data.latestCommitHash}
            </a>
            {data.latestSubmittedAt && (
              <div className="mt-1 text-xs text-muted-foreground">
                {new Date(data.latestSubmittedAt).toLocaleString()}
              </div>
            )}
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader>
          <CardTitle>{t("answer.comments")}</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <CommentList comments={data.comments} />
          <form
            className="space-y-2"
            onSubmit={(e) => {
              e.preventDefault();
              if (!comment.trim()) return;
              post.mutate();
            }}
          >
            <Textarea
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              placeholder={t("answer.comment")}
              required
            />
            <div className="flex justify-end">
              <Button type="submit" disabled={post.isPending || !comment.trim()}>
                {t("answer.postComment")}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}

export const Route = createFileRoute("/answers/$id")({
  component: AnswerDetail,
});
