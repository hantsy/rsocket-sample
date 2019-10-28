package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.rsocket.ClientRSocketConnector;
import org.springframework.integration.rsocket.ServerRSocketConnector;
import org.springframework.integration.rsocket.dsl.RSockets;
import org.springframework.integration.rsocket.outbound.RSocketOutboundGateway;
import reactor.core.publisher.Flux;

import java.util.function.Function;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}

@Configuration
class DemoIntegrationConfig {

    @Bean
    public ServerRSocketConnector serverRSocketConnector() {
        return new ServerRSocketConnector("localhost", 0);
    }

    @Bean
    public ClientRSocketConnector clientRSocketConnector(ServerRSocketConnector serverRSocketConnector) {
        int port = serverRSocketConnector.getBoundPort().block();
        ClientRSocketConnector clientRSocketConnector = new ClientRSocketConnector("localhost", port);
        clientRSocketConnector.setAutoStartup(false);
        return clientRSocketConnector;
    }

    @Bean
    public IntegrationFlow rsocketUpperCaseRequestFlow(ClientRSocketConnector clientRSocketConnector) {
        return IntegrationFlows
                .from(Function.class)
                .handle(RSockets.outboundGateway("/uppercase")
                        .command((message) -> RSocketOutboundGateway.Command.requestStreamOrChannel)
                        .expectedResponseType("T(java.lang.String)")
                        .clientRSocketConnector(clientRSocketConnector))
                .get();
    }

    @Bean
    public IntegrationFlow rsocketUpperCaseFlow() {
        return IntegrationFlows
                .from(RSockets.inboundGateway("/uppercase"))
                .<Flux<String>, Flux<String>>transform((flux) -> flux.map(String::toUpperCase))
                .get();
    }

}

