# Sub-C User context E2E.
# Variables expected:
#   host    e.g. http://localhost:3000
#   token_a JWT for user_a (claims: sub=user_a_id, email, name, permissions=[STUDENT])
#   token_b JWT for user_b
#   user_b_id  21-char nanoid for user B

# 1. lazy signup of A
GET {{host}}/users
x-bouncr-credential: {{token_a}}
Accept: application/json
HTTP 200

# 2. lazy signup of B
GET {{host}}/users
x-bouncr-credential: {{token_b}}
Accept: application/json
HTTP 200
[Asserts]
jsonpath "$" count >= 2

# 3. A offers to follow B
POST {{host}}/offers
x-bouncr-credential: {{token_a}}
Accept: application/json
Content-Type: application/json
{
  "targetUserId": "{{user_b_id}}"
}
HTTP 201
[Captures]
offer_id: jsonpath "$.id"

# 4. B sees the offer
GET {{host}}/offers
x-bouncr-credential: {{token_b}}
Accept: application/json
HTTP 200
[Asserts]
jsonpath "$" count >= 1
jsonpath "$[0].id" == "{{offer_id}}"

# 5. B accepts the offer
PUT {{host}}/offers/{{offer_id}}/accept
x-bouncr-credential: {{token_b}}
Accept: application/json
HTTP 200

# 6. B's followers list now contains A
GET {{host}}/users/{{user_b_id}}/followers
x-bouncr-credential: {{token_b}}
Accept: application/json
HTTP 200
[Asserts]
jsonpath "$" count >= 1
