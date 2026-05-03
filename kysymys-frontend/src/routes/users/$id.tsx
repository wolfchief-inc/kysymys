import { createFileRoute, Link } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { Pencil, UserPlus } from "lucide-react";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { LoadingSpinner } from "@/components/common/LoadingSpinner";
import { AvatarImage } from "@/components/user/AvatarImage";
import { getCurrentUserId } from "@/lib/auth";
import { getUser, listFollowers, offerToFollow } from "@/lib/api/user";

function UserDetail() {
  const { t } = useTranslation();
  const { id } = Route.useParams();
  const me = getCurrentUserId();
  const isSelf = me === id;
  const qc = useQueryClient();

  const user = useQuery({ queryKey: ["user", id], queryFn: () => getUser(id) });
  const followers = useQuery({
    queryKey: ["followers", id],
    queryFn: () => listFollowers(id),
  });

  const offer = useMutation({
    mutationFn: () => offerToFollow(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["offers"] }),
  });

  if (user.isLoading || !user.data) return <LoadingSpinner />;

  const alreadyFollowing = (followers.data ?? []).some((f) => f.id === me);
  const u = user.data;

  return (
    <div className="space-y-5">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-3">
            <AvatarImage userId={u.id} size={56} />
            <div className="flex-1">
              <div className="text-xl">{u.name}</div>
              <div className="text-sm font-normal text-muted-foreground">
                {u.email}
              </div>
            </div>
            {isSelf ? (
              <Button variant="outline" asChild>
                <Link to="/users/$id/edit" params={{ id }}>
                  <Pencil className="h-4 w-4" />
                  {t("user.edit")}
                </Link>
              </Button>
            ) : (
              !alreadyFollowing && (
                <Button
                  onClick={() => offer.mutate()}
                  disabled={offer.isPending || offer.isSuccess}
                >
                  <UserPlus className="h-4 w-4" />
                  {t("user.follow")}
                </Button>
              )
            )}
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-3 text-sm">
          <div className="flex flex-wrap gap-1">
            {u.roles.map((r) => (
              <Badge
                key={r}
                variant={r === "TEACHER" ? "default" : "secondary"}
              >
                {r}
              </Badge>
            ))}
          </div>
          <div className="text-muted-foreground">
            {t("user.followers")}: {(followers.data ?? []).length}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

export const Route = createFileRoute("/users/$id")({ component: UserDetail });
