


package com.example.demo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.rsocket.ClientRSocketConnector;
import org.springframework.integration.rsocket.RSocketInteractionModel;
import org.springframework.integration.rsocket.ServerRSocketConnector;
import org.springframework.integration.rsocket.dsl.RSockets;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.function.Function;

@SpringJUnitConfig(classes = {DemoApplication.class, DemoApplicationTests.TestConfig.class})
@DirtiesContext
public class DemoApplicationTests {

    @Autowired
    @Qualifier("rsocketUpperCaseRequestFlow.gateway")
    private Function<Flux<String>, Flux<String>> rsocketUpperCaseFlowFunction;

    @Test
    void testRsocketUpperCaseFlows() {
        Flux<String> result = this.rsocketUpperCaseFlowFunction.apply(Flux.just("a\n", "b\n", "c\n"));

        StepVerifier.create(result)
                .expectNext("A", "B", "C")
                .verifyComplete();
    }

    @Configuration
    public static class TestConfig {

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
                            .interactionModel((message) -> RSocketInteractionModel.requestChannel)
                            .expectedResponseType("T(java.lang.String)")
                            .clientRSocketConnector(clientRSocketConnector))
                    .get();
        }

    }

}