# Sub-B Lesson E2E scenarios.
# Variables expected: host (e.g. http://localhost:3000), token (HS256 JWT
# signed with KYSYMYS_JWT_SECRET=kysymys-dev-jwt-secret-not-for-production).

# scenario 1: create a Problem
POST {{host}}/problems
x-bouncr-credential: {{token}}
Accept: application/json
Content-Type: application/json
{
  "name": "FizzBuzz",
  "repository": {
    "type": "github",
    "url": "https://github.com/example/fizzbuzz",
    "branch": "main",
    "readmePath": "/README.md"
  }
}
HTTP 201
[Captures]
problem_id: jsonpath "$.id"

# scenario 2: list Problems
GET {{host}}/problems
x-bouncr-credential: {{token}}
Accept: application/json
HTTP 200
[Asserts]
jsonpath "$" count >= 1

# scenario 3: get Problem
GET {{host}}/problems/{{problem_id}}
x-bouncr-credential: {{token}}
Accept: application/json
HTTP 200
[Asserts]
jsonpath "$.name" == "FizzBuzz"
jsonpath "$.status" == "ACTIVE"

# scenario 4: submit first answer
POST {{host}}/problems/{{problem_id}}/answers
x-bouncr-credential: {{token}}
Accept: application/json
Content-Type: application/json
{
  "repository": { "type": "github", "url": "https://github.com/student/fizzbuzz-mine" },
  "commitHash": "0123456789012345678901234567890123456789"
}
HTTP 201
[Captures]
answer_id: jsonpath "$.id"

# scenario 5: submit second commit on same answer
POST {{host}}/problems/{{problem_id}}/answers
x-bouncr-credential: {{token}}
Accept: application/json
Content-Type: application/json
{
  "repository": { "type": "github", "url": "https://github.com/student/fizzbuzz-mine" },
  "commitHash": "1111111111111111111111111111111111111111"
}
HTTP 201
[Asserts]
jsonpath "$.id" == "{{answer_id}}"
jsonpath "$.latestCommitHash" == "1111111111111111111111111111111111111111"

# scenario 6: my answers list
GET {{host}}/answers
x-bouncr-credential: {{token}}
Accept: application/json
HTTP 200
[Asserts]
jsonpath "$" count == 1
jsonpath "$[0].id" == "{{answer_id}}"
jsonpath "$[0].latestCommitHash" == "1111111111111111111111111111111111111111"

# scenario 7: show answer (no comments yet)
GET {{host}}/answers/{{answer_id}}
x-bouncr-credential: {{token}}
Accept: application/json
HTTP 200
[Asserts]
jsonpath "$.latestCommitHash" == "1111111111111111111111111111111111111111"
jsonpath "$.comments" count == 0

# scenario 10: rename
PUT {{host}}/problems/{{problem_id}}
x-bouncr-credential: {{token}}
Accept: application/json
Content-Type: application/json
{
  "name": "FizzBuzz Renamed",
  "repository": {
    "type": "github",
    "url": "https://github.com/example/fizzbuzz",
    "branch": "main",
    "readmePath": "/README.md"
  }
}
HTTP 200
[Asserts]
jsonpath "$.name" == "FizzBuzz Renamed"

# scenario 11: archive
DELETE {{host}}/problems/{{problem_id}}
x-bouncr-credential: {{token}}
Accept: application/json
HTTP 200
[Asserts]
jsonpath "$.status" == "ARCHIVED"
