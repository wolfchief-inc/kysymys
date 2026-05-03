import { createFileRoute } from "@tanstack/react-router";

function Offers() {
  return <h1 className="text-2xl font-bold">Offers</h1>;
}

export const Route = createFileRoute("/offers")({ component: Offers });
