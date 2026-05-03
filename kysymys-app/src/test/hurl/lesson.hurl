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
