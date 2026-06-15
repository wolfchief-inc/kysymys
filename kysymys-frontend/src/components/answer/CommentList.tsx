import { useTranslation } from "react-i18next";
import { AvatarImage } from "@/components/user/AvatarImage";
import type { ReviewComment } from "@/lib/api/schemas";

export function CommentList({ comments }: { comments: ReviewComment[] }) {
  const { t } = useTranslation();
  if (comments.length === 0) {
    return <p className="text-sm text-muted-foreground">{t("dashboard.empty")}</p>;
  }
  return (
    <ul className="space-y-3">
      {comments.map((c) => (
        <li
          key={c.id}
          className="flex gap-3 rounded-md border border-border bg-muted/30 p-3"
        >
          <AvatarImage userId={c.commenterId} size={32} />
          <div className="flex-1 space-y-1">
            <div className="flex items-center justify-between text-xs text-muted-foreground">
              <span className="font-mono">{c.commenterId.slice(0, 8)}</span>
              <span>{new Date(c.postedAt).toLocaleString()}</span>
            </div>
            <p className="text-sm whitespace-pre-wrap">{c.description}</p>
          </div>
        </li>
      ))}
    </ul>
  );
}
