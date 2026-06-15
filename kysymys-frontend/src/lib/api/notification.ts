import { apiFetch } from "./client";
import { whatsNewListSchema } from "./schemas";

export const listWhatsNews = () =>
  apiFetch("/whats-news", {}, whatsNewListSchema);

export const markWhatsNewRead = (id: string) =>
  apiFetch(`/whats-news/${id}/read`, { method: "PUT" });
