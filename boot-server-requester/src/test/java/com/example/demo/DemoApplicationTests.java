package com.example.demo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.util.MimeTypeUtils;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

// an example: https://github.com/rwinch/rsocket-security/blob/master/src/test/java/org/springframework/security/rsocket/itests/RSocketMessageHandlerITests.java
@SpringBootTest
class DemoApplicationTests {

    @Autowired
    RSocketMessageHandler handler;

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
        String data = "welcome back";
        this.requester.route("greet.hantsy")
                .data(Greeting.builder().message(data).build())
                .retrieveMono(String.class)
                .as(StepVerifier::create)
                .consumeNextWith(s -> assertThat(s).contains("hantsy"))
                .verifyComplete();
    }


}
