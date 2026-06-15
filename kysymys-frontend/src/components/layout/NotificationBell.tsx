import { Link } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { Bell } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { listWhatsNews } from "@/lib/api/notification";
import { getToken } from "@/lib/auth";

export function NotificationBell() {
  const { data } = useQuery({
    queryKey: ["whats-news"],
    queryFn: listWhatsNews,
    refetchInterval: 10000,
    enabled: !!getToken(),
  });
  const unread = (data ?? []).filter((w) => w.unread).length;

  return (
    <Button variant="ghost" size="icon" asChild className="relative">
      <Link to="/notifications" aria-label="Notifications">
        <Bell className="h-5 w-5" />
        {unread > 0 && (
          <Badge
            variant="destructive"
            className="absolute -top-1 -right-1 h-5 min-w-5 justify-center px-1 text-[10px]"
          >
            {unread > 99 ? "99+" : unread}
          </Badge>
        )}
      </Link>
    </Button>
  );
}
