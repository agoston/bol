package com.bol.assessment;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GameStateController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/greeting")
    public GameState greeting(@RequestParam(value = "name", required = false, defaultValue = "World") String name) {
        return new GameState(counter.incrementAndGet(), String.format(template, name));
    }
}
