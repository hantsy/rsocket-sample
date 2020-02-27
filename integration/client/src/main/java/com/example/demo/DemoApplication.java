package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.rsocket.ClientRSocketConnector;
import org.springframework.integration.rsocket.RSocketInteractionModel;
import org.springframework.integration.rsocket.dsl.RSockets;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.function.Function;

@SpringBootApplication
@EnableIntegration
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public ClientRSocketConnector clientRSocketConnector() {
        ClientRSocketConnector clientRSocketConnector = new ClientRSocketConnector("localhost", 7000);
        clientRSocketConnector.setAutoStartup(false);
        return clientRSocketConnector;
    }

    @Bean
    public IntegrationFlow rsocketUpperCaseRequestFlow(ClientRSocketConnector clientRSocketConnector) {
        return IntegrationFlows
                .from(Function.class)
                .handle(RSockets.outboundGateway("/uppercase")
                        .interactionModel((message) -> RSocketInteractionModel.requestChannel)
                        .expectedResponseType("T(java.lang.String)")
                        .clientRSocketConnector(clientRSocketConnector))
                .get();
    }
}

@RestController
class HelloController {

    @Autowired()
    @Lazy
    @Qualifier("rsocketUpperCaseRequestFlow.gateway")
    private Function<Flux<String>, Flux<String>> rsocketUpperCaseFlowFunction;

    @GetMapping(value = "hello", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> uppercase() {
        return rsocketUpperCaseFlowFunction.apply(Flux.just("a", "b", "c", "d"));
    }
}