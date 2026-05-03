import { createFileRoute } from "@tanstack/react-router";

function AnswerDetail() {
  const { id } = Route.useParams();
  return <h1 className="text-2xl font-bold">Answer {id}</h1>;
}

export const Route = createFileRoute("/answers/$id")({
  component: AnswerDetail,
});
