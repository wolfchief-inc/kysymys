import { createFileRoute } from "@tanstack/react-router";

function ProblemEdit() {
  const { id } = Route.useParams();
  return <h1 className="text-2xl font-bold">Edit problem {id}</h1>;
}

export const Route = createFileRoute("/problems/$id/edit")({
  component: ProblemEdit,
});
