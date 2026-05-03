import { createFileRoute } from "@tanstack/react-router";

function AnswersIndex() {
  return <h1 className="text-2xl font-bold">My answers</h1>;
}

export const Route = createFileRoute("/answers/")({
  component: AnswersIndex,
});
