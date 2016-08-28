-- SignUP

curl -X POST http://localhost:9000/signup -H 'Content-Type: application/json' -d '{"firstName": "Pari", "lastName": "B", "email": "pari@yahoo.com", "password": "MyPassword123!!"}' -v

User will receive an email upon signup.

-- SignIn
curl -X POST http://localhost:9000/signin -H 'Content-Type: application/json' -d '{"email": "pari@yahoo.com", "password": "MyPassword123!!" , "rememberMe": true}' -v

User will not be allowed to sign in without confirmation. Above call will fail.

-- Confirm
Email will contain confirmation link
e.g. http://localhost:9000/confirm/fe35def7-bde5-4244 

Click on confirmation link


-- SignIn
curl -X POST http://localhost:9000/signin -H 'Content-Type: application/json' -d '{"email": "pari@yahoo.com", "password": "MyPassword123!!" , "rememberMe": true}' -v

Once confirmed, user will get JWT token

--Authentication/Authorization

Use JWT token while calling secured APIs.

curl http://localhost:9000/ -H 'X-Auth-Token: eyJ0eXAiOiJKV1QciOiJIUzI1NiJ9.eyJzdWIiOiJleUp3Y205MmFXUmxja2xFSWpvaVkzSmxaR1Z1ZEdsaGJITWlMQ0p3Y205MmFXUmxja3RsZVNJNkluQmlZWEpoY0dGMGNtVkFaMjFoYVd3dVkyOXRJbjA9IiwiaXNzIjoicGxheS1zaWxob3VldHRlIiwiZXhwIjoxNDc0NzgwMzk2LCJpYXQiOjE0NzIxODgzOTYsImp0aSI6IjMxNjI0NmExZDRhNTc0ZDY4Y2Y4NDExMjk1ZTBhMDU4NGUzMzA0ZjZhOTllZWQ5YzI3NWUyZDU2Y2I3ZWY3NGVkYzc5MTRiNzA5OWY3YWIzMjAzMjJiYjBiZjAwMDE4MmMyOTJlYWZjN2NlODdkM2JkYzIzNTA1YWU2NGZmM2NmOWY5Y2E1YmE0ZjhjZjk4OWFhMWQwYWYzNzU0ZTI5OTI2N2M2ZDZhMTU2ZTkyMjkzZWI2NjRlOGQ1NmY5NGM3MjYyNTYxYzQ4NDEwM2Y3NjM3OGZhMzZkYzk0MDAxMjk4MWVkZWZlMmUyNGY3NzcyMzIzNjZlMWY0ZGVmMGE5YzMifQ.02kX84ehMawgYKTVJa_gKBbokUCh53EV9Ix2MfVflok'

curl http://localhost:9000/secure -H 'X-Auth-Token: eyJ0eXAiOJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleUp3Y205MmFXUmxja2xFSWpvaVkzSmxaR1Z1ZEdsaGJITWlMQ0p3Y205MmFXUmxja3RsZVNJNkluQmlZWEpoY0dGMGNtVkFaMjFoYVd3dVkyOXRJbjA9IiwiaXNzIjoicGxheS1zaWxob3VldHRlIiwiZXhwIjoxNDc0NzgwMzk2LCJpYXQiOjE0NzIxODgzOTYsImp0aSI6IjMxNjI0NmExZDRhNTc0ZDY4Y2Y4NDExMjk1ZTBhMDU4NGUzMzA0ZjZhOTllZWQ5YzI3NWUyZDU2Y2I3ZWY3NGVkYzc5MTRiNzA5OWY3YWIzMjAzMjJiYjBiZjAwMDE4MmMyOTJlYWZjN2NlODdkM2JkYzIzNTA1YWU2NGZmM2NmOWY5Y2E1YmE0ZjhjZjk4OWFhMWQwYWYzNzU0ZTI5OTI2N2M2ZDZhMTU2ZTkyMjkzZWI2NjRlOGQ1NmY5NGM3MjYyNTYxYzQ4NDEwM2Y3NjM3OGZhMzZkYzk0MDAxMjk4MWVkZWZlMmUyNGY3NzcyMzIzNjZlMWY0ZGVmMGE5YzMifQ.02kX84ehMawgYKTVJa_gKBbokUCh53EV9Ix2MfVflok'

curl http://localhost:9000/admin -H 'X-Auth-Token: eyJ0eXAiOiCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleUp3Y205MmFXUmxja2xFSWpvaVkzSmxaR1Z1ZEdsaGJITWlMQ0p3Y205MmFXUmxja3RsZVNJNkluQmlZWEpoY0dGMGNtVkFaMjFoYVd3dVkyOXRJbjA9IiwiaXNzIjoicGxheS1zaWxob3VldHRlIiwiZXhwIjoxNDc0NzgwMzk2LCJpYXQiOjE0NzIxODgzOTYsImp0aSI6IjMxNjI0NmExZDRhNTc0ZDY4Y2Y4NDExMjk1ZTBhMDU4NGUzMzA0ZjZhOTllZWQ5YzI3NWUyZDU2Y2I3ZWY3NGVkYzc5MTRiNzA5OWY3YWIzMjAzMjJiYjBiZjAwMDE4MmMyOTJlYWZjN2NlODdkM2JkYzIzNTA1YWU2NGZmM2NmOWY5Y2E1YmE0ZjhjZjk4OWFhMWQwYWYzNzU0ZTI5OTI2N2M2ZDZhMTU2ZTkyMjkzZWI2NjRlOGQ1NmY5NGM3MjYyNTYxYzQ4NDEwM2Y3NjM3OGZhMzZkYzk0MDAxMjk4MWVkZWZlMmUyNGY3NzcyMzIzNjZlMWY0ZGVmMGE5YzMifQ.02kX84ehMawgYKTVJa_gKBbokUCh53EV9Ix2MfVflok'

curl -X POST http://localhost:9000/changePassword -H 'X-Auth-Token: eyJ0eXAiOciOiJIUzI1NiJ9.eyJzdWIiOiJleUp3Y205MmFXUmxja2xFSWpvaVkzSmxaR1Z1ZEdsaGJITWlMQ0p3Y205MmFXUmxja3RsZVNJNkluQmlZWEpoY0dGMGNtVkFaMjFoYVd3dVkyOXRJbjA9IiwiaXNzIjoicGxheS1zaWxob3VldHRlIiwiZXhwIjoxNDc0NzgwMzk2LCJpYXQiOjE0NzIxODgzOTYsImp0aSI6IjMxNjI0NmExZDRhNTc0ZDY4Y2Y4NDExMjk1ZTBhMDU4NGUzMzA0ZjZhOTllZWQ5YzI3NWUyZDU2Y2I3ZWY3NGVkYzc5MTRiNzA5OWY3YWIzMjAzMjJiYjBiZjAwMDE4MmMyOTJlYWZjN2NlODdkM2JkYzIzNTA1YWU2NGZmM2NmOWY5Y2E1YmE0ZjhjZjk4OWFhMWQwYWYzNzU0ZTI5OTI2N2M2ZDZhMTU2ZTkyMjkzZWI2NjRlOGQ1NmY5NGM3MjYyNTYxYzQ4NDEwM2Y3NjM3OGZhMzZkYzk0MDAxMjk4MWVkZWZlMmUyNGY3NzcyMzIzNjZlMWY0ZGVmMGE5YzMifQ.02kX84ehMawgYKTVJa_gKBbokUCh53EV9Ix2MfVflok' 'Content-Type: application/json' -d '{"password": "MyPassword987!!"}' -v


-- Reset/Forget Password

curl -X POST http://localhost:9000/startResetPassword -H 'Content-Type: application/json' -d '{"email": "pari@yahoo.com"}' -v
This API will send an email with password reset link

http://localhost:9000/resetPassword/e174b7af-e287-47ee-becf-b2c389cf6b3f
Click on password reset link.

Web page with Password Reser Form will open. Set new password.

Try to sign in with old password, this should fail.
Try to sign in with new password, this should succeed.


Thanks
Pari
