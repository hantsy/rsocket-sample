# Building a CRUD application with RSocket and Spring

In [the last post](https://medium.com/@hantsy/using-rsocket-with-spring-boot-cfc67924d06a), we explored the basic RSocket support in Spring and Spring Boot. In this post, we will create a CRUD application which is more close to the real world applications.

We will create a *client* and *server* applications to demonstrate the interactions between RSocket client and server side.

Firstly let's create the server application. 

You can simply generate a project template from [Spring initializr](https://start.spring.io), set the following properties.


* Build: Maven 
* Java: 11 
* Spring Boot version: 2.3.0.M3(I preferred the new version for practicing  new techniques)
* Dependencies: RSocket, Spring Data R2dbc, H2 Database, Lombok

> If you are new to Spring Data R2dbc, check the post [ Accessing RDBMS with Spring Data R2dbc](https://medium.com/@hantsy/reactive-accessing-rdbms-with-spring-data-r2dbc-d6e453f2837e).

In the server application, we will use RSocket to serve a RSocket server via TCP protocol.

Open the *src/main/resources/application.properties*, add the following properties.

```properties
spring.rsocket.server.port=7000
spring.rsocket.server.transport=tcp
```

Like what I have done in the former posts, firstly create a simple POJO.

```java
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("posts")
class Post {

    @Id
    @Column("id")
    private Integer id;

    @Column("title")
    private String title;

    @Column("content")
    private String content;

}
```

And create a simple Repository for `Post`.

```java

interface PostRepository extends R2dbcRepository<Post, Integer> {
}
```

Create a `Controller` class to handle the request messages.

```java
@Controller
@RequiredArgsConstructor
class PostController {

    private final PostRepository posts;

    @MessageMapping("posts.findAll")
    public Flux<Post> all() {
        return this.posts.findAll();
    }
    
    @MessageMapping("posts.findById.{id}")
    public Mono<Post> get(@DestinationVariable("id") Integer id) {
        return this.posts.findById(id);
    }

    @MessageMapping("posts.save")
    public Mono<Post> create(@Payload Post post) {
        return this.posts.save(post);
    }

    @MessageMapping("posts.update.{id}")
    public Mono<Post> update(@DestinationVariable("id") Integer id, @Payload Post post) {
        return this.posts.findById(id)
                .map(p -> {
                    p.setTitle(post.getTitle());
                    p.setContent(post.getContent());

                    return p;
                })
                .flatMap(p -> this.posts.save(p));
    }

    @MessageMapping("posts.deleteById.{id}")
    public Mono<Void> delete(@DestinationVariable("id") Integer id) {
        return this.posts.deleteById(id);
    }

}
```

Create a `schema.sql` and a `data.sql` to create tables and initialize the data.

```sql
-- schema.sql
CREATE TABLE posts (id SERIAL PRIMARY KEY, title VARCHAR(255), content VARCHAR(255));
```

```sql
-- data.sql
DELETE FROM posts;
INSERT INTO  posts (title, content) VALUES ('post one in data.sql', 'content of post one in data.sql');
```
> Note: In the Spring 2.3.0.M3,  Spring Data R2dbc is merged in the Spring Data release train. But unfortunately, the `ConnectionFactoryInitializer` is NOT ported.

To make the the `schema.sql` and `data.sql` is loaded and executed at the application startup, declare a `ConnectionFactoryInitializer` bean.

```java
@Bean
public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {

    ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
    initializer.setConnectionFactory(connectionFactory);

    CompositeDatabasePopulator populator = new CompositeDatabasePopulator();
    populator.addPopulators(new ResourceDatabasePopulator(new ClassPathResource("schema.sql")));
    populator.addPopulators(new ResourceDatabasePopulator(new ClassPathResource("data.sql")));
    initializer.setDatabasePopulator(populator);

return initializer;
}
```

Now, you can start the server application.

```bash
mvn spring-boot:run
```

Next, let's move to the client application.

Similarly, generate a project template from [Spring Initializr](https://start.spring.io), in the *Dependencies* area, make sure you have choose *WebFlux*, *RSocket*, *Lombok*.

The client application is a generic Webflux application, but use `RSocketRequester` to shake hands with the RSocket server.

Declare a `RSocketRequester` bean.

```java
@Bean
public RSocketRequester rSocketRequester(RSocketRequester.Builder b) {
    return b.dataMimeType(MimeTypeUtils.APPLICATION_JSON)
        .connectTcp("localhost", 7000)
        .block();
}
```

Create a generic `RestController` and use the `RSocketRequester` to communicate with the RSocket server.

```java
@Slf4j
@RequiredArgsConstructor
@RestController()
@RequestMapping("/posts")
class PostClientController {

    private final RSocketRequester requester;

    @GetMapping("")
    Flux<Post> all() {
        return this.requester.route("posts.findAll")
            .retrieveFlux(Post.class);
    }

    @GetMapping("{id}")
    Mono<Post> findById(@PathVariable Integer id) {
        return this.requester.route("posts.findById." + id)
                .retrieveMono(Post.class);
    }

    @PostMapping("")
    Mono<Post> save(@RequestBody Post post) {
        return this.requester.route("posts.save")
                .data(post)
                .retrieveMono(Post.class);
    }

    @PutMapping("{id}")
    Mono<Post> update(@PathVariable Integer id, @RequestBody Post post) {
        return this.requester.route("posts.update."+ id)
                .data(post)
                .retrieveMono(Post.class);
    }

    @DeleteMapping("{id}")
    Mono<Void> delete(@PathVariable Integer id) {
        return this.requester.route("posts.deleteById."+ id).send();
    }

}
```

Create a  POJO `Post` to present the message payload transferred between client and server side.

```java
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Post {
    private Integer id;
    private String title;
    private String content;
}
```
Start up the client application.

Try to test the CRUD operations by curl.


```bash 
# curl http://localhost:8080/posts
[{"id":1,"title":"post one in data.sql","content":"content of post one in data.sql"},{"id":2,"title":"Post one","content":"The content of post one"},{"id":3,"title":"Post tow","content":"The content of post tow"}]

# curl http://localhost:8080/posts/1
{"id":1,"title":"post one in data.sql","content":"content of post one in data.sql"}

# curl http://localhost:8080/posts/3
{"id":3,"title":"Post tow","content":"The content of post tow"}

# curl http://localhost:8080/posts/2
{"id":2,"title":"Post one","content":"The content of post one"}

# curl http://localhost:8080/posts -d "{\"title\":\"my save title\", \"content\":\"my content of my post\"}" -H "Content-Type:application/json" -X POST
{"id":4,"title":"my save title","content":"my content of my post"}

# curl http://localhost:8080/posts
[{"id":1,"title":"post one in data.sql","content":"content of post one in data.sql"},{"id":2,"title":"Post one","content":"The content of post one"},{"id":3,"title":"Post tow","content":"The content of post tow"},{"id":4,"title":"my save title","content":"update my content of my post"}]

# curl http://localhost:8080/posts/4 -X DELETE

# curl http://localhost:8080/posts
[{"id":1,"title":"post one in data.sql","content":"content of post one in data.sql"},{"id":2,"title":"Post one","content":"The content of post one"},{"id":3,"title":"Post tow","content":"The content of post tow"}]
```

As a bonus, try to add a filter to find the posts by keyword.

In the server application, create a new method in the `PostRepository`.

```java
@Query("SELECT * FROM posts WHERE title like $1")
Flux<Post> findByTitleContains(String name);
```

And in the `PostController` ,  create a new route to handle this request from client.

```java
class PostController {
	//...
    @MessageMapping("posts.titleContains")
    public Flux<Post> titleContains(@Payload String title) {
        return this.posts.findByTitleContains(title);
    }
	//...
}
```

In the client side, change the `PostClientController` 's  `all` method to the following:

```java
class PostClientController {
	//...
    Flux<Post> all(@RequestParam(name = "title", required = false) String title) {
        if (StringUtils.hasText(title)) {
            return this.requester.route("posts.titleContains")
                    .data(title).retrieveFlux(Post.class);
        } else {
            return this.requester.route("posts.findAll")
                    .retrieveFlux(Post.class);
        }
    }
    //... 
}    
```

Now, try to add an extra `title` request parameter to access http://localhost:8080/posts.

Get the [source codes](https://github.com/hantsy/rsocket-sample/tree/master/crud) from my Github.

