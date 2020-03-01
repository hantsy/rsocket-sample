package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.rsocket.RSocketInteractionModel;
import org.springframework.integration.rsocket.ServerRSocketConnector;
import org.springframework.integration.rsocket.ServerRSocketMessageHandler;
import org.springframework.integration.rsocket.dsl.RSockets;
import org.springframework.messaging.rsocket.RSocketStrategies;
import reactor.core.publisher.Flux;

import java.io.IOException;

@SpringBootApplication
@EnableIntegration
public class DemoApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(DemoApplication.class, args);
    }

    // see PR: https://github.com/spring-projects/spring-boot/pull/18834
    @Bean
    ServerRSocketMessageHandler serverRSocketMessageHandler(RSocketStrategies rSocketStrategies) {
        var handler = new ServerRSocketMessageHandler(true);
        handler.setRSocketStrategies(rSocketStrategies);
        return handler;
    }

    @Bean
    public ServerRSocketConnector serverRSocketConnector(ServerRSocketMessageHandler serverRSocketMessageHandler) {
        return new ServerRSocketConnector(serverRSocketMessageHandler);
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
