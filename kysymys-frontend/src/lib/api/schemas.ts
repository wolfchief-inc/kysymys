import { z } from "zod";

export const id21 = z.string().length(21);
export const commitHashSchema = z.string().regex(/^[0-9a-f]{40}$/);

export const githubProblemRepoSchema = z.object({
  type: z.literal("github"),
  url: z.string().min(1).max(255),
  branch: z.string().min(1).max(100),
  readmePath: z.string().min(1).max(100),
});

export const bitbucketProblemRepoSchema = z.object({
  type: z.literal("bitbucket"),
  url: z.string().min(1).max(255),
  branch: z.string().min(1).max(100),
  readmePath: z.string().min(1).max(100),
});

export const genericProblemRepoSchema = z.object({
  type: z.literal("generic"),
  url: z.string().min(1).max(255),
  branch: z.string().min(1).max(100),
});

export const problemRepoSchema = z.discriminatedUnion("type", [
  githubProblemRepoSchema,
  bitbucketProblemRepoSchema,
  genericProblemRepoSchema,
]);
export type ProblemRepo = z.infer<typeof problemRepoSchema>;

export const problemStatusSchema = z.enum(["ACTIVE", "ARCHIVED"]);
export type ProblemStatus = z.infer<typeof problemStatusSchema>;

export const problemSchema = z.object({
  id: id21,
  name: z.string(),
  repository: problemRepoSchema,
  status: problemStatusSchema,
  problemUrl: z.string(),
});
export type Problem = z.infer<typeof problemSchema>;
export const problemListSchema = z.array(problemSchema);

export const githubAnswerRepoSchema = z.object({
  type: z.literal("github"),
  url: z.string().min(1).max(255),
});
export const bitbucketAnswerRepoSchema = z.object({
  type: z.literal("bitbucket"),
  url: z.string().min(1).max(255),
});
export const genericAnswerRepoSchema = z.object({
  type: z.literal("generic"),
  url: z.string().min(1).max(255),
});
export const answerRepoSchema = z.discriminatedUnion("type", [
  githubAnswerRepoSchema,
  bitbucketAnswerRepoSchema,
  genericAnswerRepoSchema,
]);
export type AnswerRepo = z.infer<typeof answerRepoSchema>;

export const reviewCommentSchema = z.object({
  id: id21,
  commenterId: id21,
  description: z.string(),
  postedAt: z.string(),
});
export type ReviewComment = z.infer<typeof reviewCommentSchema>;

export const answerSchema = z.object({
  id: id21,
  problemId: id21,
  answererId: id21,
  repository: answerRepoSchema,
  latestCommitHash: z.string().optional(),
  latestSubmittedAt: z.string().optional(),
  answerUrl: z.string().optional(),
  comments: z.array(reviewCommentSchema).default([]),
});
export type Answer = z.infer<typeof answerSchema>;
export const answerListSchema = z.array(answerSchema);

export const roleSchema = z.enum(["STUDENT", "TEACHER"]);
export type Role = z.infer<typeof roleSchema>;

export const userSchema = z.object({
  id: id21,
  email: z.string(),
  name: z.string(),
  roles: z.array(roleSchema),
});
export type User = z.infer<typeof userSchema>;
export const userListSchema = z.array(userSchema);

export const offerSchema = z.object({
  id: id21,
  offeringUserId: id21,
  targetUserId: id21,
  offeredAt: z.string(),
});
export type Offer = z.infer<typeof offerSchema>;
export const offerListSchema = z.array(offerSchema);

export const whatsNewSchema = z.object({
  id: id21,
  templatePath: z.string(),
  params: z.record(z.string(), z.unknown()),
  postedAt: z.string(),
  unread: z.boolean(),
});
export type WhatsNew = z.infer<typeof whatsNewSchema>;
export const whatsNewListSchema = z.array(whatsNewSchema);

export const acknowledgeSchema = z.object({
  readWhatsNewId: id21.optional(),
});

export const buildKindSchema = z.enum(["BUILD_SUCCESS", "BUILD_FAILURE"]);
export type BuildKind = z.infer<typeof buildKindSchema>;

export const activityParticipantSchema = z.object({
  participantId: z.string(),
  problemId: z.string().nullable(),
  lastActivityAt: z.string().nullable(),
  lastBuildKind: buildKindSchema.nullable(),
  lastBuildAt: z.string().nullable(),
  lastBuildDetail: z.string().nullable(),
  stuck: z.boolean(),
});
export type ActivityParticipant = z.infer<typeof activityParticipantSchema>;

export const activityStatusSchema = z.object({
  serverTime: z.string(),
  participants: z.array(activityParticipantSchema),
});
export type ActivityStatus = z.infer<typeof activityStatusSchema>;

export const problemViolationSchema = z.object({
  type: z.string().optional(),
  title: z.string().optional(),
  status: z.number().optional(),
  detail: z.string().optional(),
  violations: z
    .array(
      z.object({
        field: z.string(),
        code: z.string().optional(),
        message: z.string().optional(),
      }),
    )
    .optional(),
});
