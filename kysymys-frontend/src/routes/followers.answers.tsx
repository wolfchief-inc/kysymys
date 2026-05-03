import { createFileRoute } from "@tanstack/react-router";

function FollowerAnswers() {
  return <h1 className="text-2xl font-bold">Followers' answers</h1>;
}

export const Route = createFileRoute("/followers/answers")({
  component: FollowerAnswers,
});
