import { cn } from "@/lib/utils";
import { avatarUrl } from "@/lib/api/avatar";

type Props = {
  userId: string;
  size?: number;
  className?: string;
  alt?: string;
};

export function AvatarImage({ userId, size = 32, className, alt }: Props) {
  return (
    <img
      src={avatarUrl(userId)}
      width={size}
      height={size}
      alt={alt ?? "avatar"}
      className={cn(
        "inline-block rounded-full bg-muted object-cover",
        className,
      )}
      style={{ width: size, height: size, imageRendering: "pixelated" }}
    />
  );
}
