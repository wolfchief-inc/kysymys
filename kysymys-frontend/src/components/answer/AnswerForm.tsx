import { useState } from "react";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import type { AnswerRepo } from "@/lib/api/schemas";

type Props = {
  submitting?: boolean;
  error?: string | null;
  onSubmit: (input: { repository: AnswerRepo; commitHash: string }) => void;
  onCancel: () => void;
};

export function AnswerForm({ submitting, error, onSubmit, onCancel }: Props) {
  const { t } = useTranslation();
  const [type, setType] = useState<AnswerRepo["type"]>("github");
  const [url, setUrl] = useState("");
  const [commitHash, setCommitHash] = useState("");

  const handle = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit({ repository: { type, url } as AnswerRepo, commitHash });
  };

  return (
    <form onSubmit={handle} className="space-y-5">
      <div className="flex flex-wrap gap-3 text-sm">
        {(["github", "bitbucket", "generic"] as const).map((opt) => (
          <label key={opt} className="flex items-center gap-2">
            <input
              type="radio"
              name="answer-type"
              checked={type === opt}
              onChange={() => setType(opt)}
            />
            <span>{opt}</span>
          </label>
        ))}
      </div>

      <div className="space-y-2">
        <Label htmlFor="url">{t("problem.url")}</Label>
        <Input
          id="url"
          type="url"
          required
          maxLength={255}
          value={url}
          onChange={(e) => setUrl(e.target.value)}
        />
      </div>

      <div className="space-y-2">
        <Label htmlFor="commitHash">{t("answer.commitHash")}</Label>
        <Input
          id="commitHash"
          required
          minLength={40}
          maxLength={40}
          pattern="[0-9a-f]{40}"
          value={commitHash}
          onChange={(e) => setCommitHash(e.target.value)}
          className="font-mono"
        />
      </div>

      {error && <p className="text-sm text-destructive">{error}</p>}

      <div className="flex justify-end gap-2">
        <Button type="button" variant="outline" onClick={onCancel}>
          {t("common.cancel")}
        </Button>
        <Button type="submit" disabled={submitting}>
          {t("common.submit")}
        </Button>
      </div>
    </form>
  );
}
