package com.example.consumer;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class ConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }

    @Bean
    public RSocketRequester rSocketRequester(RSocketRequester.Builder b) {
        return b.connectTcp("localhost", 7000).block();
    }

}

@RestController
class GreetingController {

    private final RSocketRequester requester;

    public GreetingController(RSocketRequester requester) {
        this.requester = requester;
    }

    @GetMapping
    Mono<Void> hello() {
        return this.requester.route("hello").data(new Greeting("Welcome to Rsocket")).send();
    }

}

@Data
@AllArgsConstructor
class Greeting {

    String name;
}
