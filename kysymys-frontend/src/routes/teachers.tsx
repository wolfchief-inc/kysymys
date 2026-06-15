import { useState } from "react";
import { createFileRoute } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { LoadingSpinner } from "@/components/common/LoadingSpinner";
import { PermissionGate } from "@/components/common/PermissionGate";
import { UserCard } from "@/components/user/UserCard";
import { ApiError } from "@/lib/api/client";
import { grantTeacherRole, listTeachers } from "@/lib/api/user";

function Teachers() {
  const { t } = useTranslation();
  const qc = useQueryClient();
  const { data, isLoading } = useQuery({
    queryKey: ["teachers"],
    queryFn: listTeachers,
  });

  const [target, setTarget] = useState("");
  const [error, setError] = useState<string | null>(null);
  const grant = useMutation({
    mutationFn: () => grantTeacherRole(target),
    onSuccess: () => {
      setTarget("");
      qc.invalidateQueries({ queryKey: ["teachers"] });
      qc.invalidateQueries({ queryKey: ["users"] });
    },
    onError: (e: unknown) => {
      setError(e instanceof ApiError ? `${e.status} ${e.message}` : String(e));
    },
  });

  return (
    <div className="space-y-5">
      <h1 className="text-2xl font-bold">{t("nav.teachers")}</h1>

      <PermissionGate permission="TEACHER">
        <Card>
          <CardHeader>
            <CardTitle>{t("user.grantTeacher")}</CardTitle>
          </CardHeader>
          <CardContent>
            <form
              onSubmit={(e) => {
                e.preventDefault();
                setError(null);
                if (target.length === 21) grant.mutate();
              }}
              className="flex flex-wrap items-end gap-3"
            >
              <div className="flex-1 space-y-1 min-w-[240px]">
                <Label htmlFor="target">{t("user.targetUserId")}</Label>
                <Input
                  id="target"
                  required
                  minLength={21}
                  maxLength={21}
                  value={target}
                  onChange={(e) => setTarget(e.target.value)}
                  className="font-mono"
                />
              </div>
              <Button type="submit" disabled={grant.isPending || target.length !== 21}>
                {t("common.submit")}
              </Button>
            </form>
            {error && (
              <p className="mt-2 text-sm text-destructive">{error}</p>
            )}
          </CardContent>
        </Card>
      </PermissionGate>

      {isLoading ? (
        <LoadingSpinner />
      ) : (data ?? []).length === 0 ? (
        <p className="text-muted-foreground">{t("dashboard.empty")}</p>
      ) : (
        <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
          {data!.map((u) => (
            <UserCard key={u.id} user={u} />
          ))}
        </div>
      )}
    </div>
  );
}

export const Route = createFileRoute("/teachers")({ component: Teachers });
