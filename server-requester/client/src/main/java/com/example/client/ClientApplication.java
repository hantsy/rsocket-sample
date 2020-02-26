package com.example.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

@SpringBootApplication
public class ClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    @Bean
    public RSocketRequester rSocketRequester(RSocketRequester.Builder b, RSocketStrategies rSocketStrategies) {
        return b.dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .rsocketFactory(RSocketMessageHandler.clientResponder(rSocketStrategies, new ClientHandler()))
                .setupRoute("connect")
                .setupData("user")
                .connectTcp("localhost", 7000)
                .block();
    }

}

@Slf4j
class ClientHandler {

    @MessageMapping("status")
    public Mono<String> statusUpdate(String status) {
        log.info("Received (" + status + ") at " + LocalDateTime.now());
        return Mono.just("confirmed").delayElement(Duration.ofSeconds(1));
    }
}

@Slf4j
@RestController()
@RequiredArgsConstructor
class GreetingController {

    private final RSocketRequester requester;

    @GetMapping("hello")
    Mono<String> hello() {
        return this.requester.route("hello").data("Hello RSocket").retrieveMono(String.class);
    }

}