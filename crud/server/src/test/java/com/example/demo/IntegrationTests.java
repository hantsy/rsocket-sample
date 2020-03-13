package com.example.demo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.rsocket.context.LocalRSocketServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTests {

    @LocalRSocketServerPort
    int port;

    RSocketRequester rSocketRequester;

    @Autowired
    RSocketRequester.Builder builder;

    @BeforeAll
    public void setup() {
        this.rSocketRequester = builder.connectTcp("localhost", port).block();
    }

    @Test
    public void willLoadPosts() {
        this.rSocketRequester.route("posts.findAll")
                .retrieveFlux(Post.class)
                .as(StepVerifier::create)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    public void testTitleContains() {
        this.rSocketRequester.route("posts.titleContains")
                .data("%data.sql")
                .retrieveFlux(Post.class)
                .take(1)
                .as(StepVerifier::create)
                .consumeNextWith(p -> assertEquals("post one in data.sql", p.getTitle()))
                .verifyComplete();
    }

}

