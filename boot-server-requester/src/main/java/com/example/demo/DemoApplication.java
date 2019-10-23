package com.example.demo;

import io.rsocket.ConnectionSetupPayload;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public RSocketMessageHandler messageHandler() {
        RSocketMessageHandler handler = new RSocketMessageHandler();
        handler.setRSocketStrategies(rsocketStrategies());
        return handler;
    }

    @Bean
    public RSocketStrategies rsocketStrategies() {
        return RSocketStrategies.builder()
                .decoder(new Jackson2JsonDecoder())
                .encoder(new Jackson2JsonEncoder())
                .build();
    }
}


@Controller
@Slf4j
class GreetingController {

    @ConnectMapping("greet.*")
    public void setup( RSocketRequester requester) {
                log.info("@ConnectMapping(greet*), setup");
    }

    @MessageMapping("hello")
    public Mono<Void> hello(Greeting p) {
        log.info("received: {} at {}", p, Instant.now());
        return Mono.empty();
    }

    @MessageMapping("greet.{name}")
    public Mono<String> greet(@DestinationVariable String name, @Payload Greeting p) {
        log.info("received: {}, {} at {}", name, p, Instant.now());
        return Mono.just("Hello " + name + ", " + p.getMessage() + " at " + Instant.now());
    }

    @MessageMapping("greet-stream")
    public Flux<String> greetStream(@Payload Greeting p) {
        log.info("received:  {} at {}", p, Instant.now());
        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> "greet-stream#(Hello #" + i + "," + p.getMessage() + ") at " + Instant.now());
    }

    @MessageMapping("greet-channel")
    public Flux<String> greetChannel(@Payload Flux<Greeting> p) {
        log.info("received:  {} at {}", p, Instant.now());
        return p.delayElements(Duration.ofSeconds(1))
                .map(m -> "greet-channel#(" + m + ") at " + Instant.now());
    }

}

@Data
@Builder
class Greeting {
    String message;
}
