package com.example.server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
public class HelloController {

    @GetMapping
    public String get() {
        return "server greeting at :" + Instant.now();
    }
}
