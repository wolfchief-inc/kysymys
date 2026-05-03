import { useTranslation } from "react-i18next";
import { Languages } from "lucide-react";
import { Button } from "@/components/ui/button";

export function LanguageSwitch() {
  const { i18n, t } = useTranslation();
  const next = i18n.resolvedLanguage === "ja" ? "en" : "ja";

  return (
    <Button
      variant="ghost"
      size="sm"
      onClick={() => i18n.changeLanguage(next)}
      aria-label={t("common.language")}
    >
      <Languages className="h-4 w-4" />
      <span className="ml-1 text-xs uppercase">{next}</span>
    </Button>
  );
}
