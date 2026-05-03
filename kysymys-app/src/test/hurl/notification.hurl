# Sub-C Notification E2E.
# Assumes A already follows B (run user.hurl first to set up the connection).
# Variables: host, token_a (follower), token_b (followee), problem_id

# B submits an answer; A receives a WhatsNew because A follows B.
POST {{host}}/problems/{{problem_id}}/answers
x-bouncr-credential: {{token_b}}
Accept: application/json
Content-Type: application/json
{
  "repository": { "type": "github", "url": "https://github.com/b/answer" },
  "commitHash": "0000000000000000000000000000000000000000"
}
HTTP 201

# A's whats-news list contains the new notification, marked unread.
GET {{host}}/whats-news
x-bouncr-credential: {{token_a}}
Accept: application/json
HTTP 200
[Captures]
notif_id: jsonpath "$[0].id"
[Asserts]
jsonpath "$[0].templatePath" == "submittedAnswer"
jsonpath "$[0].unread" == true

# A marks it as read.
PUT {{host}}/whats-news/{{notif_id}}/read
x-bouncr-credential: {{token_a}}
Accept: application/json
HTTP 200

# Read flag flipped.
GET {{host}}/whats-news
x-bouncr-credential: {{token_a}}
Accept: application/json
HTTP 200
[Asserts]
jsonpath "$[0].unread" == false
