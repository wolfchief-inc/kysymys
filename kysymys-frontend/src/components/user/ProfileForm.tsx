import { useState } from "react";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import type { User } from "@/lib/api/schemas";

type Props = {
  initial: User;
  submitting?: boolean;
  error?: string | null;
  onSubmit: (input: { email?: string; name?: string }) => void;
  onCancel: () => void;
};

export function ProfileForm({
  initial,
  submitting,
  error,
  onSubmit,
  onCancel,
}: Props) {
  const { t } = useTranslation();
  const [email, setEmail] = useState(initial.email);
  const [name, setName] = useState(initial.name);

  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        onSubmit({ email, name });
      }}
      className="space-y-4"
    >
      <div className="space-y-2">
        <Label htmlFor="email">{t("user.email")}</Label>
        <Input
          id="email"
          type="email"
          required
          maxLength={100}
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
      </div>
      <div className="space-y-2">
        <Label htmlFor="name">{t("user.name")}</Label>
        <Input
          id="name"
          required
          maxLength={100}
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
      </div>
      {error && <p className="text-sm text-destructive">{error}</p>}
      <div className="flex justify-end gap-2">
        <Button type="button" variant="outline" onClick={onCancel}>
          {t("common.cancel")}
        </Button>
        <Button type="submit" disabled={submitting}>
          {t("common.save")}
        </Button>
      </div>
    </form>
  );
}
