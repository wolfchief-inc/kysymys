# Avatar endpoint serves an 8-bit PNG, generated on first call and cached after.
# Variables: host, user_id (21-char nanoid)

GET {{host}}/users/{{user_id}}/avatar
Accept: image/png
HTTP 200
[Asserts]
header "Content-Type" == "image/png"
bytes count > 100

GET {{host}}/users/{{user_id}}/avatar
Accept: image/png
HTTP 200
[Asserts]
header "Content-Type" == "image/png"
