import { createFileRoute } from "@tanstack/react-router";

function UsersIndex() {
  return <h1 className="text-2xl font-bold">Users</h1>;
}

export const Route = createFileRoute("/users/")({
  component: UsersIndex,
});
