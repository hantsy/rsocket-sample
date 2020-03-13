package com.example.client;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class ClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    @Bean
    public RSocketRequester rSocketRequester(RSocketRequester.Builder b) {
        return b.dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .connectTcp("localhost", 7000)
                .block();
    }

}

@Slf4j
@RequiredArgsConstructor
@RestController()
@RequestMapping("/posts")
class PostClientController {

    private final RSocketRequester requester;

    @GetMapping("")
    Flux<Post> all(@RequestParam(name = "title", required = false) String title) {
        if (StringUtils.hasText(title)) {
            return this.requester.route("posts.titleContains")
                    .data(title).retrieveFlux(Post.class);
        } else {
            return this.requester.route("posts.findAll")
                    .retrieveFlux(Post.class);
        }
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
