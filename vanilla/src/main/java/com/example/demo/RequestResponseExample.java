package com.example.demo;

import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import java.time.Instant;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

public class RequestResponseExample {

    public static void main(String[] args) throws InterruptedException {
        final int port = 7000;

        final AbstractRSocket responseHandler = new AbstractRSocket() {

            @Override
            public Mono<Payload> requestResponse(Payload payload) {

                System.out.printf("received request-response payload: %s%n", payload.getDataUtf8());
                return Mono.just(DefaultPayload.create(payload.getDataUtf8() + " at "+ Instant.now()));
            }
        };

        Disposable server = RSocketFactory.receive()
                .acceptor(
                        (setupPayload, reactiveSocket)
                        -> Mono.just(responseHandler)
                )
                .transport(TcpServerTransport.create("localhost", port))
                .start()
                .subscribe();
        System.out.printf("server is started on port:%d%n", port);

        RSocket socket = RSocketFactory.connect()
                .transport(TcpClientTransport.create("localhost", port))
                .start()
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
