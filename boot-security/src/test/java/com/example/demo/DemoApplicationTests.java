package com.example.demo;

import io.rsocket.exceptions.ApplicationErrorException;
import io.rsocket.exceptions.RejectedSetupException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.util.MimeTypeUtils;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
// an example: https://github.com/rwinch/rsocket-security/blob/master/src/test/java/org/springframework/security/rsocket/itests/RSocketMessageHandlerITests.java
@SpringBootTest
class DemoApplicationTests {

    @Autowired
    RSocketMessageHandler handler;

    @Autowired
    PayloadSocketAcceptorInterceptor interceptor;

    private RSocketRequester requester;

    @BeforeEach
    public void setup() {

        this.requester = RSocketRequester.builder()
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                //				.rsocketFactory(factory -> factory.addRequesterPlugin(payloadInterceptor))
                .rsocketStrategies(this.handler.getRSocketStrategies())
                .connectTcp("localhost", 7000)
                .block();
    }

    @AfterEach
    public void dispose() {
        this.requester.rsocket().dispose();
    }

    @Test
    public void retrieveMonoWhenSecureThenDenied() throws Exception {
        String data = "hantsy";
        assertThatCode(() -> this.requester.route("greet*")
                .data(GreetingRequest.of(data))
                .retrieveMono(GreetingResponse.class)
                .block()
        ).isInstanceOf(RejectedSetupException.class);
    }


    @Test
    public void retrieveMonoWithSetupUserSecureThenDenied() throws Exception {
        UsernamePasswordMetadata credentials = new UsernamePasswordMetadata("setup", "password");

        this.requester = RSocketRequester.builder()
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .setupMetadata(credentials, UsernamePasswordMetadata.BASIC_AUTHENTICATION_MIME_TYPE)
                //				.rsocketFactory(factory -> factory.addRequesterPlugin(payloadInterceptor))
                .rsocketStrategies(this.handler.getRSocketStrategies())
                .connectTcp("localhost", 7000)
                .block();
        String data = "hantsy";
        assertThatCode(() -> this.requester.route("greet")
                .data(GreetingRequest.of(data))
                .retrieveMono(GreetingResponse.class)
                .block()
        ).isInstanceOf(ApplicationErrorException.class);
    }


    @Test
    public void retrieveMonoWithSetupUserAndAdminUserThenOK() throws Exception {
        UsernamePasswordMetadata credentials = new UsernamePasswordMetadata("setup", "password");

        this.requester = RSocketRequester.builder()
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .setupMetadata(credentials, UsernamePasswordMetadata.BASIC_AUTHENTICATION_MIME_TYPE)
                //				.rsocketFactory(factory -> factory.addRequesterPlugin(payloadInterceptor))
                .rsocketStrategies(this.handler.getRSocketStrategies())
                .connectTcp("localhost", 7000)
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

}
