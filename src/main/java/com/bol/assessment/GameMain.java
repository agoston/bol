package com.bol.assessment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
@EnableAutoConfiguration
public class GameMain {
    public static void main(String[] args) {
        SpringApplication.run(GameMain.class, args);
    }
}
