package com.example.demosass;

import org.springframework.boot.SpringApplication;

public class TestDemosassApplication {

    public static void main(String[] args) {
        SpringApplication.from(DemosassApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
