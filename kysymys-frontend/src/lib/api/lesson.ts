import { apiFetch } from "./client";
import {
  type Answer,
  answerListSchema,
  answerSchema,
  type AnswerRepo,
  type Problem,
  problemListSchema,
  problemSchema,
  type ProblemRepo,
} from "./schemas";

export type CreateProblemInput = {
  name: string;
  repository: ProblemRepo;
};

export type UpdateProblemInput = CreateProblemInput;

export type SubmitAnswerInput = {
  repository: AnswerRepo;
  commitHash: string;
};

export type PostCommentInput = {
  description: string;
};

export const listProblems = () =>
  apiFetch("/problems", {}, problemListSchema);

export const getProblem = (id: string) =>
  apiFetch(`/problems/${id}`, {}, problemSchema);

export const createProblem = (input: CreateProblemInput): Promise<Problem> =>
  apiFetch("/problems", { method: "POST", body: input }, problemSchema);

export const updateProblem = (
  id: string,
  input: UpdateProblemInput,
): Promise<Problem> =>
  apiFetch(`/problems/${id}`, { method: "PUT", body: input }, problemSchema);

export const archiveProblem = (id: string) =>
  apiFetch(`/problems/${id}`, { method: "DELETE" });

export const submitAnswer = (
  problemId: string,
  input: SubmitAnswerInput,
): Promise<Answer> =>
  apiFetch(
    `/problems/${problemId}/answers`,
    { method: "POST", body: input },
    answerSchema,
  );

export const listMyAnswers = () =>
  apiFetch("/answers", {}, answerListSchema);

export const getAnswer = (id: string) =>
  apiFetch(`/answers/${id}`, {}, answerSchema);

export const postComment = (answerId: string, input: PostCommentInput) =>
  apiFetch(`/answers/${answerId}/comments`, { method: "POST", body: input });

export const listFollowerAnswers = () =>
  apiFetch("/followers/answers", {}, answerListSchema);
