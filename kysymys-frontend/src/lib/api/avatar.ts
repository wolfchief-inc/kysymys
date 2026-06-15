import { apiUrl } from "./client";

export const avatarUrl = (userId: string) => apiUrl(`/users/${userId}/avatar`);
