package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.rsocket.context.LocalRSocketServerPort;
import org.springframework.boot.rsocket.context.RSocketServerInitializedEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.security.rsocket.metadata.BasicAuthenticationEncoder;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.rsocket.metadata.UsernamePasswordMetadata.BASIC_AUTHENTICATION_MIME_TYPE;

// see: https://github.com/rwinch/rsocket-security/blob/master/src/test/java/org/springframework/security/rsocket/itests/RSocketMessageHandlerITests.java
// and  https://github.com/spring-projects/spring-security/blob/5.2.0.RELEASE/samples/boot/hellorsocket/src/integration-test/java/sample/HelloRSocketApplicationITests.java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.rsocket.server.port=0")
class DemoApplicationTests {


    @LocalRSocketServerPort
    int port;

    @Autowired
    RSocketRequester.Builder requester;

    @Test
    public void retrieveMonoWhenSecureThenDenied() throws Exception {
        RSocketRequester requester = this.requester
                .connectTcp("localhost", this.port)
                .block();
        String data = "hantsy";
        assertThatThrownBy(
                () -> requester.route("greet*")
                        .data(GreetingRequest.of(data))
                        .retrieveMono(GreetingResponse.class)
                        .block()
        ).isNotNull();
    }


    @Test
    public void retrieveMonoWithSetupUserWithoutAdminThenDenied() throws Exception {
        UsernamePasswordMetadata credentials = new UsernamePasswordMetadata("setup", "password");

        RSocketRequester requester = this.requester
                .rsocketStrategies(builder -> builder.encoder(new BasicAuthenticationEncoder()))
                .setupMetadata(credentials, BASIC_AUTHENTICATION_MIME_TYPE)
                .connectTcp("localhost", this.port)
                .block();
        String data = "hantsy";
        assertThatThrownBy(
                () -> requester.route("greet")
                        .data(GreetingRequest.of(data))
                        .retrieveMono(GreetingResponse.class)
                        .block()
        ).isNotNull();
    }


    @Test
    public void retrieveMonoWithSetupUserAndAdminUserThenOK() throws Exception {
        UsernamePasswordMetadata credentials = new UsernamePasswordMetadata("setup", "password");

        RSocketRequester requester = this.requester
                .rsocketStrategies(builder -> builder.encoder(new BasicAuthenticationEncoder()))
                .setupMetadata(credentials, BASIC_AUTHENTICATION_MIME_TYPE)
                .connectTcp("localhost", this.port)
                .block();
        UsernamePasswordMetadata adminCredentials = new UsernamePasswordMetadata("admin", "password");

        String data = "hantsy";
        requester.route("greet")
                .metadata(adminCredentials, BASIC_AUTHENTICATION_MIME_TYPE)
                .data(GreetingRequest.of(data))
                .retrieveMono(GreetingResponse.class)
                .as(StepVerifier::create)
                .consumeNextWith(c -> assertThat(c.getMessage()).contains("hantsy"))
                .verifyComplete();
    }


    // FIXME: Waiting for @LocalRSocketServerPort
    // https://github.com/spring-projects/spring-boot/pull/18287
//    @Autowired
//    Config config;
//
//    private int getPort() {
//        return this.config.port;
//    }
//
//    @TestConfiguration
//    static class Config implements ApplicationListener<RSocketServerInitializedEvent> {
//        private int port;
//
//        @Override
//        public void onApplicationEvent(RSocketServerInitializedEvent event) {
//            this.port = event.getServer().address().getPort();
//        }
//    }

}
