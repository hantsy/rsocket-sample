package com.example.demo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.rsocket.context.RSocketServerInitializedEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.MimeTypeUtils;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// see: https://github.com/rwinch/rsocket-security/blob/master/src/test/java/org/springframework/security/rsocket/itests/RSocketMessageHandlerITests.java
// and  https://github.com/spring-projects/spring-security/blob/5.2.0.RELEASE/samples/boot/hellorsocket/src/integration-test/java/sample/HelloRSocketApplicationITests.java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.rsocket.server.port=0")
class DemoApplicationTests {

    // FIXME: Waiting for @LocalRSocketServerPort
    // https://github.com/spring-projects/spring-boot/pull/18287

    @Autowired
    RSocketMessageHandler handler;

    private RSocketRequester requester;

    @BeforeEach
    public void setup() {

        this.requester = RSocketRequester.builder()
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                //				.rsocketFactory(factory -> factory.addRequesterPlugin(payloadInterceptor))
                .rsocketStrategies(this.handler.getRSocketStrategies())
                .connectTcp("localhost", getPort())
                .block();
    }

    @AfterEach
    public void dispose() {
        this.requester.rsocket().dispose();
    }

    @Test
    public void retrieveMonoWhenSecureThenDenied() throws Exception {
        String data = "hantsy";
        assertThatThrownBy(
                () -> this.requester.route("greet*")
                        .data(GreetingRequest.of(data))
                        .retrieveMono(GreetingResponse.class)
                        .block()
        ).isNotNull();
    }


    @Test
    public void retrieveMonoWithSetupUserSecureThenDenied() throws Exception {
        UsernamePasswordMetadata credentials = new UsernamePasswordMetadata("setup", "password");

        this.requester = RSocketRequester.builder()
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .setupMetadata(credentials, UsernamePasswordMetadata.BASIC_AUTHENTICATION_MIME_TYPE)
                //				.rsocketFactory(factory -> factory.addRequesterPlugin(payloadInterceptor))
                .rsocketStrategies(this.handler.getRSocketStrategies())
                .connectTcp("localhost", getPort())
                .block();
        String data = "hantsy";
        assertThatThrownBy(
                () -> this.requester.route("greet")
                        .data(GreetingRequest.of(data))
                        .retrieveMono(GreetingResponse.class)
                        .block()
        ).isNotNull();
    }


    @Test
    public void retrieveMonoWithSetupUserAndAdminUserThenOK() throws Exception {
        UsernamePasswordMetadata credentials = new UsernamePasswordMetadata("setup", "password");

        this.requester = RSocketRequester.builder()
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .setupMetadata(credentials, UsernamePasswordMetadata.BASIC_AUTHENTICATION_MIME_TYPE)
                //				.rsocketFactory(factory -> factory.addRequesterPlugin(payloadInterceptor))
                .rsocketStrategies(this.handler.getRSocketStrategies())
                .connectTcp("localhost", getPort())
                .block();

        UsernamePasswordMetadata adminCredentials = new UsernamePasswordMetadata("admin", "password");

        String data = "hantsy";
        this.requester.route("greet")
                .metadata(adminCredentials, UsernamePasswordMetadata.BASIC_AUTHENTICATION_MIME_TYPE)
                .data(GreetingRequest.of(data))
                .retrieveMono(GreetingResponse.class)
                .as(StepVerifier::create)
                .consumeNextWith(c -> assertThat(c.getMessage()).contains("hantsy"))
                .verifyComplete();
    }


    @Autowired
    Config config;

    private int getPort() {
        return this.config.port;
    }

    @TestConfiguration
    static class Config implements ApplicationListener<RSocketServerInitializedEvent> {
        private int port;

        @Override
        public void onApplicationEvent(RSocketServerInitializedEvent event) {
            this.port = event.getServer().address().getPort();
        }
    }

}
