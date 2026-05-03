import { Link } from "@tanstack/react-router";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { AvatarImage } from "@/components/user/AvatarImage";
import type { User } from "@/lib/api/schemas";

export function UserCard({ user }: { user: User }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-3">
          <AvatarImage userId={user.id} size={36} />
          <Link
            to="/users/$id"
            params={{ id: user.id }}
            className="truncate hover:underline"
          >
            {user.name}
          </Link>
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-2 text-sm text-muted-foreground">
        <div className="truncate">{user.email}</div>
        <div className="flex flex-wrap gap-1">
          {user.roles.map((r) => (
            <Badge key={r} variant={r === "TEACHER" ? "default" : "secondary"}>
              {r}
            </Badge>
          ))}
        </div>
      </CardContent>
    </Card>
  );
}
