package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.boot.rsocket.server.ServerRSocketFactoryProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;
import org.springframework.security.rsocket.core.SecuritySocketAcceptorInterceptor;
import org.springframework.security.rsocket.metadata.BasicAuthenticationDecoder;
import org.springframework.security.rsocket.metadata.BasicAuthenticationEncoder;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

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
    RSocketStrategiesCustomizer rSocketStrategiesCustomizer() {
        return (b) -> b.decoder(new BasicAuthenticationDecoder(), new Jackson2JsonDecoder())
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

    // see: https://github.com/spring-projects/spring-security/issues/7497
    // and https://github.com/spring-projects/spring-security/blob/5.2.0.RELEASE/samples/boot/hellorsocket/src/main/java/sample/HelloRSocketSecurityConfig.java
    @Bean
    ServerRSocketFactoryProcessor springSecurityServerRSocketFactoryProcessor(
            SecuritySocketAcceptorInterceptor interceptor) {
        return builder -> builder.addSocketAcceptorPlugin(interceptor);
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
