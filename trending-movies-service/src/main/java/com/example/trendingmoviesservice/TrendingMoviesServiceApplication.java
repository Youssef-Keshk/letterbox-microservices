package com.example.trendingmoviesservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableEurekaClient
//@EnableCircuitBreaker
@EnableScheduling
public class TrendingMoviesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrendingMoviesServiceApplication.class, args);
    }

}