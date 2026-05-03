import { createFileRoute } from "@tanstack/react-router";

function ProblemDetail() {
  const { id } = Route.useParams();
  return <h1 className="text-2xl font-bold">Problem {id}</h1>;
}

export const Route = createFileRoute("/problems/$id")({
  component: ProblemDetail,
});
