package com.example.producer;

import io.rsocket.Payload;
import java.time.Instant;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class ProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProducerApplication.class, args);
    }

}

@Controller
@Slf4j
class GreetingServerController {

    @MessageMapping("hello")
    public Mono<Void> hello(Greeting p) {
        log.info("received: {} at {}", p, Instant.now());
        return Mono.empty();
    }
}

@Data
class Greeting {
    String name;
}
