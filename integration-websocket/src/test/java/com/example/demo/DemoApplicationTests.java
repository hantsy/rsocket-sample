package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.function.Function;

@ContextConfiguration(classes = DemoApplication.class)
class DemoApplicationTests {

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
}
