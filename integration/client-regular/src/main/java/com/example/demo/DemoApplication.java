package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}

@RestController
class HelloController {
    Mono<RSocketRequester> requesterMono;

    public HelloController(RSocketRequester.Builder builder) {
        this.requesterMono = builder.connectTcp("localhost", 7000);
    }

    @GetMapping(value = "hello", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> uppercase() {
        return requesterMono.flatMapMany(
                rSocketRequester -> rSocketRequester.route("/uppercase")
                        .data(Flux.just("a", "b", "c", "d"))
                        .retrieveFlux(String.class)
        );
    }
}