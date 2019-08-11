package com.example.demo;

import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import java.time.Duration;
import java.time.Instant;
import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class RequestChannelExample {

    public static void main(String[] args) throws InterruptedException {
        final int port = 7000;

        final AbstractRSocket responseHandler = new AbstractRSocket() {
            @Override
            public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
                return Flux.from(payloads)
                        .map(p->p.getDataUtf8())
                        .map(i -> DefaultPayload.create("received(" + i + ") at " + Instant.now()));
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
        socket.requestChannel(
                Flux.interval(Duration.ofSeconds(1))
                        .map(i -> DefaultPayload.create("message #" + i)))
                .map(p -> p.getDataUtf8())
                .doOnNext(System.out::println)
                .take(10)
                .then()
                .doFinally(s -> socket.dispose())
                .then().block();

        server.dispose();
    }
}
