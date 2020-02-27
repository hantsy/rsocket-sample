package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.rsocket.RSocketInteractionModel;
import org.springframework.integration.rsocket.ServerRSocketConnector;
import org.springframework.integration.rsocket.dsl.RSockets;
import reactor.core.publisher.Flux;

@SpringBootApplication
@EnableIntegration
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public ServerRSocketConnector serverRSocketConnector() {
        return new ServerRSocketConnector("localhost", 7000);
    }

    @Bean
    public IntegrationFlow rsocketUpperCaseFlow(ServerRSocketConnector serverRSocketConnector) {
        return IntegrationFlows
                .from(RSockets.inboundGateway("/uppercase")
                        .interactionModels(RSocketInteractionModel.requestChannel)
                        .rsocketConnector(serverRSocketConnector)
                )
                .<Flux<String>, Flux<String>>transform((flux) -> flux.map(String::toUpperCase))
                .get();
    }

}
