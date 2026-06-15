import { useState } from "react";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import type { Problem, ProblemRepo } from "@/lib/api/schemas";
import { ProblemRepositoryInput } from "./ProblemRepositoryInput";

const DEFAULT_REPO: ProblemRepo = {
  type: "github",
  url: "",
  branch: "main",
  readmePath: "/README.md",
};

type Props = {
  initial?: Problem;
  submitting?: boolean;
  error?: string | null;
  onSubmit: (input: { name: string; repository: ProblemRepo }) => void;
  onCancel: () => void;
};

export function ProblemForm({
  initial,
  submitting,
  error,
  onSubmit,
  onCancel,
}: Props) {
  const { t } = useTranslation();
  const [name, setName] = useState(initial?.name ?? "");
  const [repo, setRepo] = useState<ProblemRepo>(
    initial?.repository ?? DEFAULT_REPO,
  );

  const handle = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit({ name, repository: repo });
  };

  return (
    <form onSubmit={handle} className="space-y-5">
      <div className="space-y-2">
        <Label htmlFor="name">{t("problem.title")}</Label>
        <Input
          id="name"
          required
          maxLength={100}
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
      </div>

      <ProblemRepositoryInput value={repo} onChange={setRepo} />

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
