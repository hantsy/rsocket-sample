package com.example.demo;

import io.rsocket.core.RSocketConnector;
import io.rsocket.Payload;
import io.rsocket.core.RSocketServer;
import io.rsocket.RSocket;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import java.time.Duration;
import java.time.Instant;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class RequestStreamExample {

    public static void main(String[] args) throws InterruptedException {
        final int port = 7000;

        final RSocket responseHandler = new RSocket() {
            @Override
            public Flux<Payload> requestStream(Payload payload) {
                System.out.printf("received request-stream payload: %s%n", payload.getDataUtf8());
                return Flux.interval(Duration.ofMillis(100))
                        .map(i -> DefaultPayload.create("message #" + i + " at " + Instant.now()));
            }

        };

        Disposable server = RSocketServer.create(
                (setupPayload, reactiveSocket) -> Mono.just(responseHandler)
        )
                .bind(TcpServerTransport.create("localhost", port))
                .subscribe();
        System.out.printf("server is started on port:%d%n", port);

        RSocket socket = RSocketConnector.create()
                .connect(TcpClientTransport.create("localhost", port))
                .block();
        System.out.printf("client is connecting to port:%d%n", port);
        socket.requestStream(DefaultPayload.create("Hello"))
                .map(p -> p.getDataUtf8())
                .doOnNext(System.out::println)
                .take(10)
                .then()
                .doFinally(s-> socket.dispose())
                .then().block();

        //Thread.sleep(5_000);
        //socket.dispose();
        server.dispose();
    }
}
