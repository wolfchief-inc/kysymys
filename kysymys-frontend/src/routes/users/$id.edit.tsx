import { useState } from "react";
import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { LoadingSpinner } from "@/components/common/LoadingSpinner";
import { ProfileForm } from "@/components/user/ProfileForm";
import { ApiError } from "@/lib/api/client";
import { getUser, updateProfile } from "@/lib/api/user";

function UserEdit() {
  const { t } = useTranslation();
  const { id } = Route.useParams();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const [error, setError] = useState<string | null>(null);

  const { data, isLoading } = useQuery({
    queryKey: ["user", id],
    queryFn: () => getUser(id),
  });

  const update = useMutation({
    mutationFn: (input: Parameters<typeof updateProfile>[1]) =>
      updateProfile(id, input),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["user", id] });
      qc.invalidateQueries({ queryKey: ["users"] });
      navigate({ to: "/users/$id", params: { id } });
    },
    onError: (e: unknown) => {
      setError(e instanceof ApiError ? `${e.status} ${e.message}` : String(e));
    },
  });

  if (isLoading || !data) return <LoadingSpinner />;

  return (
    <div className="mx-auto max-w-2xl">
      <Card>
        <CardHeader>
          <CardTitle>{t("user.edit")}</CardTitle>
        </CardHeader>
        <CardContent>
          <ProfileForm
            initial={data}
            submitting={update.isPending}
            error={error}
            onSubmit={(input) => {
              setError(null);
              update.mutate(input);
            }}
            onCancel={() => navigate({ to: "/users/$id", params: { id } })}
          />
        </CardContent>
      </Card>
    </div>
  );
}

export const Route = createFileRoute("/users/$id/edit")({ component: UserEdit });
