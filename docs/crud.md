# Creating a CRUD application with Spring RSocket







```bash 
D:\hantsylabs\rsocket-sample\integration\server-boot>curl http://localhost:8080/posts
[{"id":1,"title":"post one in data.sql","content":"content of post one in data.sql"},{"id":2,"title":"Post one","content":"The content of post one"},{"id":3,"title":"Post tow","content":"The content of post tow"}]
D:\hantsylabs\rsocket-sample\integration\server-boot>curl http://localhost:8080/posts/1
{"id":1,"title":"post one in data.sql","content":"content of post one in data.sql"}
D:\hantsylabs\rsocket-sample\integration\server-boot>curl http://localhost:8080/posts/3
{"id":3,"title":"Post tow","content":"The content of post tow"}
D:\hantsylabs\rsocket-sample\integration\server-boot>curl http://localhost:8080/posts/2
{"id":2,"title":"Post one","content":"The content of post one"}
D:\hantsylabs\rsocket-sample\integration\server-boot>curl http://localhost:8080/posts -d '{"title":"my save title", "content":"my content of my post"}'
{"timestamp":"2020-03-13T11:09:52.653+0000","path":"/posts","status":500,"error":"Internal Server Error","message":"In a WebFlux application, form data is accessed via ServerWebExchange.getFormData().","requestId":"efbd0803-5"}curl: (3) [globbing] unmatched close brace/bracket in column 30

D:\hantsylabs\rsocket-sample\integration\server-boot>curl http://localhost:8080/posts -d '{"title":"my save title", "content":"my content of my post"}' -H "Content-Type:application/json"
{"timestamp":"2020-03-13T11:10:18.814+0000","path":"/posts","status":400,"error":"Bad Request","message":"Failed to read HTTP message","requestId":"b02c8cb9-6"}curl: (3) [globbing] unmatched close brace/bracket in column 30

D:\hantsylabs\rsocket-sample\integration\server-boot>curl http://localhost:8080/posts -d '{"title":"my save title", "content":"my content of my post"}' -H "Content-Type:application/json" -X POST
{"timestamp":"2020-03-13T11:11:07.798+0000","path":"/posts","status":400,"error":"Bad Request","message":"Failed to read HTTP message","requestId":"96dca4ed-7"}curl: (3) [globbing] unmatched close brace/bracket in column 30

D:\hantsylabs\rsocket-sample\integration\server-boot>curl http://localhost:8080/posts -d \"{\"title\":\"my save title\", \"content\":\"my content of my post\"}\" -H "Content-Type:application/json" -X POST
{"timestamp":"2020-03-13T11:12:49.995+0000","path":"/posts","status":400,"error":"Bad Request","message":"Failed to read HTTP message","requestId":"52bb940b-8"}curl: (6) Could not resolve host: save
curl: (6) Could not resolve host: title",
curl: (3) Port number ended with '"'
curl: (6) Could not resolve host: content
curl: (6) Could not resolve host: of
curl: (6) Could not resolve host: my
curl: (3) [globbing] unmatched close brace/bracket in column 6

D:\hantsylabs\rsocket-sample\integration\server-boot>curl http://localhost:8080/posts -d "{\"title\":\"my save title\", \"content\":\"my content of my post\"}" -H "Content-Type:application/json" -X POST
{"id":4,"title":"my save title","content":"my content of my post"}
D:\hantsylabs\rsocket-sample\integration\server-boot>curl http://localhost:8080/posts/4 -d "{\"title\":\"my save title\", \"content\":\"update my content of my post\"}" -H "Content-Type:application/json" -X PUT
{"id":4,"title":"my save title","content":"update my content of my post"}
D:\hantsylabs\rsocket-sample\integration\server-boot>curl http://localhost:8080/posts
[{"id":1,"title":"post one in data.sql","content":"content of post one in data.sql"},{"id":2,"title":"Post one","content":"The content of post one"},{"id":3,"title":"Post tow","content":"The content of post tow"},{"id":4,"title":"my save title","content":"update my content of my post"}]
D:\hantsylabs\rsocket-sample\integration\server-boot>curl http://localhost:8080/posts/4 -X DELETE

D:\hantsylabs\rsocket-sample\integration\server-boot>curl http://localhost:8080/posts
[{"id":1,"title":"post one in data.sql","content":"content of post one in data.sql"},{"id":2,"title":"Post one","content":"The content of post one"},{"id":3,"title":"Post tow","content":"The content of post tow"}]
D:\hantsylabs\rsocket-sample\integration\server-boot>curl http://localhost:8080/posts


```

