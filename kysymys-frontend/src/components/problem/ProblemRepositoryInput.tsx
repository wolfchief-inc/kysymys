import { useId } from "react";
import { useTranslation } from "react-i18next";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import type { ProblemRepo } from "@/lib/api/schemas";

type Props = {
  value: ProblemRepo;
  onChange: (next: ProblemRepo) => void;
};

const DEFAULT_GITHUB: ProblemRepo = {
  type: "github",
  url: "",
  branch: "main",
  readmePath: "/README.md",
};
const DEFAULT_BITBUCKET: ProblemRepo = {
  type: "bitbucket",
  url: "",
  branch: "main",
  readmePath: "/README.md",
};
const DEFAULT_GENERIC: ProblemRepo = {
  type: "generic",
  url: "",
  branch: "main",
};

export function ProblemRepositoryInput({ value, onChange }: Props) {
  const { t } = useTranslation();
  const groupName = useId();

  const setType = (next: ProblemRepo["type"]) => {
    if (next === value.type) return;
    if (next === "github") onChange({ ...DEFAULT_GITHUB, url: value.url });
    else if (next === "bitbucket")
      onChange({ ...DEFAULT_BITBUCKET, url: value.url });
    else onChange({ ...DEFAULT_GENERIC, url: value.url });
  };

  return (
    <fieldset className="space-y-3 rounded-md border border-border p-4">
      <legend className="text-sm font-semibold">{t("problem.repository")}</legend>

      <div className="flex flex-wrap gap-3 text-sm">
        {(["github", "bitbucket", "generic"] as const).map((option) => (
          <label key={option} className="flex items-center gap-2">
            <input
              type="radio"
              name={groupName}
              value={option}
              checked={value.type === option}
              onChange={() => setType(option)}
            />
            <span>{option}</span>
          </label>
        ))}
      </div>

      <div className="space-y-2">
        <Label>{t("problem.url")}</Label>
        <Input
          type="url"
          required
          value={value.url}
          onChange={(e) => onChange({ ...value, url: e.target.value })}
        />
      </div>

      <div className="grid gap-3 sm:grid-cols-2">
        <div className="space-y-2">
          <Label>branch</Label>
          <Input
            required
            value={value.branch}
            onChange={(e) => onChange({ ...value, branch: e.target.value })}
          />
        </div>
        {value.type !== "generic" && (
          <div className="space-y-2">
            <Label>readmePath</Label>
            <Input
              required
              value={value.readmePath}
              onChange={(e) =>
                onChange({ ...value, readmePath: e.target.value })
              }
            />
          </div>
        )}
      </div>
    </fieldset>
  );
}
