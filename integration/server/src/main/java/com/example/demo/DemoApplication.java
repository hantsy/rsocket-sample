package com.example.demo;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.rsocket.RSocketInteractionModel;
import org.springframework.integration.rsocket.ServerRSocketConnector;
import org.springframework.integration.rsocket.dsl.RSockets;
import reactor.core.publisher.Flux;

import java.io.IOException;

@Configuration
@ComponentScan
@IntegrationComponentScan
@EnableIntegration
public class DemoApplication {

    public static void main(String[] args) throws IOException {
        try (ConfigurableApplicationContext ctx = new AnnotationConfigApplicationContext(DemoApplication.class)) {
            System.out.println("Press any key to exit.");
            System.in.read();
        } finally {
            System.out.println("Exited.");
        }

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
