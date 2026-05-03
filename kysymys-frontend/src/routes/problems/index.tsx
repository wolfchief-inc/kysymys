import { createFileRoute } from "@tanstack/react-router";

function ProblemsIndex() {
  return <h1 className="text-2xl font-bold">Problems</h1>;
}

export const Route = createFileRoute("/problems/")({
  component: ProblemsIndex,
});
