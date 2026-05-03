import { createFileRoute } from "@tanstack/react-router";

function ProblemAnswer() {
  const { id } = Route.useParams();
  return <h1 className="text-2xl font-bold">Answer for {id}</h1>;
}

export const Route = createFileRoute("/problems/$id/answer")({
  component: ProblemAnswer,
});
