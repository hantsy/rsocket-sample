/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import static com.example.demo.DemoApplication.PORT;
import io.rsocket.core.RSocketServer;
import io.rsocket.core.RSocketConnector;
import io.rsocket.RSocket;
import io.rsocket.Payload;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 *
 * @author hantsy
 */
@SpringBootApplication
@Slf4j
public class DemoApplication {

    final static int PORT = 7000;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

@Component
@Order(0)
@Slf4j
class Producer {

    @EventListener(value = ApplicationReadyEvent.class)
    void init() {

        final RSocket responseHandler = new RSocket() {

            @Override
            public Mono<Payload> requestResponse(Payload payload) {
                log.info("received request-response payload: {}", payload.getDataUtf8());
                return Mono.just(DefaultPayload.create("received (" + payload.getDataUtf8() + ") at " + Instant.now()));
            }
        };

        RSocketServer.create(
                        (setupPayload, reactiveSocket) -> Mono.just(responseHandler)
                )
                .bind(TcpServerTransport.create("localhost", PORT))
                .block();
    }
}

@Component
@Slf4j
class Consumer {

    @EventListener(value = ApplicationReadyEvent.class)
    void init() {
        RSocketConnector.create()
                .connect(TcpClientTransport.create("localhost", PORT))
                .flatMap(r -> r.requestResponse(DefaultPayload.create("Hello")))
                .subscribe(r -> log.info("handled result:#" + r.getDataUtf8()));
    }
}
