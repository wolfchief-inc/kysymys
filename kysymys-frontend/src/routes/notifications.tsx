import { createFileRoute } from "@tanstack/react-router";

function Notifications() {
  return <h1 className="text-2xl font-bold">Notifications</h1>;
}

export const Route = createFileRoute("/notifications")({
  component: Notifications,
});
