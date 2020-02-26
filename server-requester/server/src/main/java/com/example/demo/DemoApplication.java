package com.example.demo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}

@Controller
@Slf4j
class GreetingController {
    private final Map<String, RSocketRequester> REQESTER_MAP = new HashMap<>();

    @ConnectMapping("connect")
    void setup(RSocketRequester requester, @Payload String user) {
        log.info("@ConnectMapping(connect), user:{}", user);
        requester.rsocket()
                .onClose()
                .doFinally(
                        f -> REQESTER_MAP.remove(user, requester)
                );
        REQESTER_MAP.put(user, requester);
        log.info("send status back to client...");
        requester.route("status").data("user:" + user + " is connected!")
                .retrieveMono(String.class)
                .subscribe(
                        data -> log.info("received data from the client: {}", data),
                        error -> log.error("error: {}", error),
                        () -> log.info("done")
                );
    }

    @MessageMapping("hello")
    Mono<String> ping(@Payload String message) {
        log.info("@MessageMapping(hello), payload : {}", message);
        return Mono.just("received (" + message + ") at " + LocalDateTime.now());
    }
}

