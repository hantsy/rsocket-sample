package com.example.demo;

import io.rsocket.frame.decoder.PayloadDecoder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.rsocket.context.RSocketServerBootstrap;
import org.springframework.boot.rsocket.netty.NettyRSocketServerFactory;
import org.springframework.boot.rsocket.server.RSocketServer;
import org.springframework.boot.rsocket.server.RSocketServerFactory;
import org.springframework.boot.rsocket.server.ServerRSocketFactoryProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;
import org.springframework.security.rsocket.metadata.BasicAuthenticationDecoder;
import org.springframework.security.rsocket.metadata.BasicAuthenticationEncoder;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.time.Instant;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}

@Configuration
@EnableRSocketSecurity
class SecurityConfig {

    @Bean
    public RSocketMessageHandler messageHandler() {
        RSocketMessageHandler handler = new RSocketMessageHandler();
        handler.setRSocketStrategies(rsocketStrategies());
        return handler;
    }

    @Bean
    public RSocketStrategies rsocketStrategies() {
        return RSocketStrategies.builder()
                .decoder(new BasicAuthenticationDecoder(), new Jackson2JsonDecoder())
                .encoder(new BasicAuthenticationEncoder(), new Jackson2JsonEncoder())
                .build();
    }

    @Bean
    public PayloadSocketAcceptorInterceptor rsocketInterceptor(RSocketSecurity rsocket) {
        return rsocket
                .authorizePayload(
                        authorize -> {
                            authorize
                                    // must have ROLE_SETUP to make connection
                                    .setup().hasRole("SETUP")
                                    // must have ROLE_ADMIN for routes starting with "greet."
                                    .route("greet*").hasRole("ADMIN")
                                    // any other request must be authenticated for
                                    .anyRequest().authenticated();
                        }
                )
                .basicAuthentication(Customizer.withDefaults())
                .build();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails adminUser = User.withUsername("admin")
                .passwordEncoder(p -> passwordEncoder.encode(p))
                .password("password")
                .roles("ADMIN")
                .build();
        UserDetails setupUser = User.withUsername("setup")
                .passwordEncoder(p -> passwordEncoder.encode(p))
                .password("password")
                .roles("SETUP")
                .build();

        return new MapReactiveUserDetailsService(adminUser, setupUser);
    }


    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    //see: https://github.com/spring-projects/spring-security/issues/7497
    @Bean
    ReactorResourceFactory reactorResourceFactory() {
        return new ReactorResourceFactory();
    }

    @Bean
    RSocketServerFactory rSocketServerFactory(ReactorResourceFactory resourceFactory,
                                              ServerRSocketFactoryProcessor frameDecoderServerFactoryProcessor) throws Exception {
        NettyRSocketServerFactory factory = new NettyRSocketServerFactory();
        factory.setResourceFactory(resourceFactory);
        factory.setTransport(RSocketServer.Transport.TCP);
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(InetAddress.getByName("localhost")).to(factory::setAddress);
        map.from(7000).to(factory::setPort);
        factory.addSocketFactoryProcessors(frameDecoderServerFactoryProcessor);
        return factory;
    }

    @Bean
    RSocketServerBootstrap rSocketServerBootstrap(RSocketServerFactory rSocketServerFactory,
                                                  RSocketMessageHandler rSocketMessageHandler) {
        return new RSocketServerBootstrap(rSocketServerFactory, rSocketMessageHandler.responder());
    }

    @Bean
    ServerRSocketFactoryProcessor frameDecoderServerFactoryProcessor(
            RSocketMessageHandler rSocketMessageHandler, PayloadSocketAcceptorInterceptor rsocketInterceptor) {
        return (serverRSocketFactory) -> {
            if (rSocketMessageHandler.getRSocketStrategies()
                    .dataBufferFactory() instanceof NettyDataBufferFactory) {
                serverRSocketFactory.frameDecoder(PayloadDecoder.ZERO_COPY);
            }
            return serverRSocketFactory.addSocketAcceptorPlugin(rsocketInterceptor);
        };
    }

}


@Controller
class GreetingController {

    @MessageMapping("greet")
    Mono<GreetingResponse> greet(GreetingRequest request) {
        return Mono.just(new GreetingResponse("Hello " + request.getName() + " @ " + Instant.now()));
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class GreetingRequest {
    private String name;

    public static GreetingRequest of(String name) {
        var greeting = new GreetingRequest();
        greeting.setName(name);
        return greeting;
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class GreetingResponse {
    private String message;
}
