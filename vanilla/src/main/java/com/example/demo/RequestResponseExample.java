package com.example.demo;

import io.rsocket.core.RSocketConnector;
import io.rsocket.Payload;
import io.rsocket.core.RSocketServer;
import io.rsocket.RSocket;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import java.time.Instant;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

public class RequestResponseExample {

    public static void main(String[] args) throws InterruptedException {
        final int port = 7000;

        final RSocket responseHandler = new RSocket() {

            @Override
            public Mono<Payload> requestResponse(Payload payload) {

                System.out.printf("received request-response payload: %s%n", payload.getDataUtf8());
                return Mono.just(DefaultPayload.create(payload.getDataUtf8() + " at "+ Instant.now()));
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
        socket.requestResponse(DefaultPayload.create("Hello"))
                .map(p -> p.getDataUtf8())
                .doOnNext(System.out::println)
                .block();

        socket.dispose();
        server.dispose();
    }
}
