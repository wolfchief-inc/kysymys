import { createFileRoute } from "@tanstack/react-router";

function UserEdit() {
  const { id } = Route.useParams();
  return <h1 className="text-2xl font-bold">Edit user {id}</h1>;
}

export const Route = createFileRoute("/users/$id/edit")({
  component: UserEdit,
});
