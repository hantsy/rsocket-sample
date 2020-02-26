package com.example.client;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

@SpringBootApplication
public class ClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    @Bean
    public RSocketRequester rSocketRequester(RSocketRequester.Builder b) {
        return b.dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .setupRoute("connect")
                .setupData("user")
                .connectTcp("localhost", 7000).block();
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