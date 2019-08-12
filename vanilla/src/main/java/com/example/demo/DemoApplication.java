/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import static com.example.demo.DemoApplication.PORT;
import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocketFactory;
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

        final AbstractRSocket responseHandler = new AbstractRSocket() {

            @Override
            public Mono<Payload> requestResponse(Payload payload) {
                log.info("received request-response payload: {}", payload.getDataUtf8());
                return Mono.just(DefaultPayload.create("received (" + payload.getDataUtf8() + ") at " + Instant.now()));
            }
        };

        RSocketFactory.receive()
                .acceptor(
                        (setupPayload, reactiveSocket)
                        -> Mono.just(responseHandler)
                )
                .transport(TcpServerTransport.create("localhost", PORT))
                .start()
                .block();
    }
}

@Component
@Slf4j
class Consumer {

    @EventListener(value = ApplicationReadyEvent.class)
    void init() {
        RSocketFactory.connect()
                .transport(TcpClientTransport.create("localhost", PORT))
                .start()
                .flatMap(r -> r.requestResponse(DefaultPayload.create("Hello")))
                .subscribe(r -> log.info("handled result:#" + r.getDataUtf8()));
    }
}
