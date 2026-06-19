import { apiFetch } from "./client";
import { type ActivityStatus, activityStatusSchema } from "./schemas";

export const getActivityStatus = (): Promise<ActivityStatus> =>
  apiFetch("/activity/status", {}, activityStatusSchema);
