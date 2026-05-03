import { createFileRoute } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { Check } from "lucide-react";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { LoadingSpinner } from "@/components/common/LoadingSpinner";
import { AvatarImage } from "@/components/user/AvatarImage";
import { acceptOffer, listOffers } from "@/lib/api/user";

function Offers() {
  const { t } = useTranslation();
  const qc = useQueryClient();
  const { data, isLoading } = useQuery({
    queryKey: ["offers"],
    queryFn: listOffers,
  });

  const accept = useMutation({
    mutationFn: (offerId: string) => acceptOffer(offerId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["offers"] }),
  });

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">{t("offer.list")}</h1>
      {isLoading ? (
        <LoadingSpinner />
      ) : (data ?? []).length === 0 ? (
        <p className="text-muted-foreground">{t("dashboard.empty")}</p>
      ) : (
        <div className="grid gap-3 md:grid-cols-2">
          {data!.map((o) => (
            <Card key={o.id}>
              <CardHeader>
                <CardTitle className="flex items-center gap-3">
                  <AvatarImage userId={o.offeringUserId} size={36} />
                  <span className="truncate font-mono text-sm">
                    {o.offeringUserId.slice(0, 12)}…
                  </span>
                </CardTitle>
              </CardHeader>
              <CardContent className="flex items-center justify-between gap-3 text-sm text-muted-foreground">
                <span>{new Date(o.offeredAt).toLocaleString()}</span>
                <Button
                  size="sm"
                  onClick={() => accept.mutate(o.id)}
                  disabled={accept.isPending}
                >
                  <Check className="h-4 w-4" />
                  {t("offer.accept")}
                </Button>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}

export const Route = createFileRoute("/offers")({ component: Offers });
