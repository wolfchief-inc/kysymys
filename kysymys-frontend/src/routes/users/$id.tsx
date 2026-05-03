import { createFileRoute } from "@tanstack/react-router";

function UserDetail() {
  const { id } = Route.useParams();
  return <h1 className="text-2xl font-bold">User {id}</h1>;
}

export const Route = createFileRoute("/users/$id")({
  component: UserDetail,
});
