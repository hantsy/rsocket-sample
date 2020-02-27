package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.rsocket.ClientRSocketConnector;
import org.springframework.integration.rsocket.RSocketInteractionModel;
import org.springframework.integration.rsocket.ServerRSocketConnector;
import org.springframework.integration.rsocket.ServerRSocketMessageHandler;
import org.springframework.integration.rsocket.dsl.RSockets;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.ClientRSocketFactoryConfigurer;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.function.Function;

@SpringBootApplication
@EnableIntegration
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public ClientRSocketConnector clientRSocketConnector(/*RSocketStrategies rSocketStrategies*/) {
        ClientRSocketConnector clientRSocketConnector = new ClientRSocketConnector(URI.create("ws://localhost:8080/rsocket"));
        clientRSocketConnector.setAutoStartup(false);
//        clientRSocketConnector.setRSocketStrategies(rSocketStrategies);
//        clientRSocketConnector.setFactoryConfigurer(clientRSocketFactory -> clientRSocketFactory.acceptor(new ServerRSocketMessageHandler (true).responder()));
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

//    @Bean
//    public IntegrationFlow rsocketUpperCaseFlow() {
//        return IntegrationFlows
//                .from(RSockets.inboundGateway("/uppercase")
//                        .interactionModels(RSocketInteractionModel.requestChannel))
//                .<Flux<String>, Flux<String>>transform((flux) -> flux.map(String::toUpperCase))
//                .get();
//    }

}

@Controller
class Handler{

    @MessageMapping("/uppercase")
    public Flux<String> uppercase(Flux<String> input) {
        return input.map(String::toUpperCase);
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