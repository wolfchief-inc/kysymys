import { createFileRoute } from "@tanstack/react-router";

function ProblemNew() {
  return <h1 className="text-2xl font-bold">New problem</h1>;
}

export const Route = createFileRoute("/problems/new")({
  component: ProblemNew,
});
