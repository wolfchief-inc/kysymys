import { createFileRoute } from "@tanstack/react-router";

function Teachers() {
  return <h1 className="text-2xl font-bold">Teachers</h1>;
}

export const Route = createFileRoute("/teachers")({ component: Teachers });
