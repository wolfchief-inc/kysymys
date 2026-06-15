import { apiFetch } from "./client";
import {
  type Offer,
  offerListSchema,
  offerSchema,
  type User,
  userListSchema,
  userSchema,
} from "./schemas";

export type UpdateProfileInput = {
  email?: string;
  name?: string;
};

export const listUsers = (q?: string) => {
  const qs = q ? `?q=${encodeURIComponent(q)}` : "";
  return apiFetch(`/users${qs}`, {}, userListSchema);
};

export const getUser = (id: string) =>
  apiFetch(`/users/${id}`, {}, userSchema);

export const updateProfile = (
  id: string,
  input: UpdateProfileInput,
): Promise<User> =>
  apiFetch(`/users/${id}`, { method: "PUT", body: input }, userSchema);

export const listFollowers = (id: string) =>
  apiFetch(`/users/${id}/followers`, {}, userListSchema);

export const listTeachers = () =>
  apiFetch("/teachers", {}, userListSchema);

export const grantTeacherRole = (targetUserId: string) =>
  apiFetch(
    "/grant-teacher-role",
    { method: "POST", body: { targetUserId } },
    userSchema,
  );

export const offerToFollow = (targetUserId: string): Promise<Offer> =>
  apiFetch("/offers", { method: "POST", body: { targetUserId } }, offerSchema);

export const listOffers = () =>
  apiFetch("/offers", {}, offerListSchema);

export const acceptOffer = (offerId: string) =>
  apiFetch(`/offers/${offerId}/accept`, { method: "PUT" });
