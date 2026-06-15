import { useState } from "react";
import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useTranslation } from "react-i18next";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { setToken } from "@/lib/auth";

const searchSchema = z.object({ redirect: z.string().optional() });

function LoginRoute() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const search = Route.useSearch();
  const [token, setLocalToken] = useState("");

  const onSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!token.trim()) return;
    setToken(token.trim());
    navigate({ to: search.redirect ?? "/" });
  };

  return (
    <div className="flex min-h-full items-center justify-center bg-background p-6">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle>{t("auth.title")}</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={onSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="token">{t("auth.tokenLabel")}</Label>
              <Textarea
                id="token"
                rows={6}
                value={token}
                onChange={(e) => setLocalToken(e.target.value)}
                placeholder="eyJhbGciOi..."
                className="font-mono text-xs"
              />
              <p className="text-xs text-muted-foreground">
                {t("auth.tokenHelp")}
              </p>
            </div>
            <Button type="submit" className="w-full">
              {t("auth.save")}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}

export const Route = createFileRoute("/login")({
  component: LoginRoute,
  validateSearch: searchSchema,
});
