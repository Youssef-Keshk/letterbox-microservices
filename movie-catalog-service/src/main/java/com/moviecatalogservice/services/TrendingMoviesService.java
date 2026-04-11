package com.moviecatalogservice.services;

import com.example.trendingmoviesservice.grpc.TrendingRequest;
import com.example.trendingmoviesservice.grpc.TrendingResponse;
import com.example.trendingmoviesservice.grpc.TrendingServiceGrpc;
import com.example.trendingmoviesservice.grpc.MovieScore;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class TrendingMoviesService {

    @GrpcClient("trending-service")
    private TrendingServiceGrpc.TrendingServiceBlockingStub trendingServiceStub;

    @HystrixCommand(fallbackMethod = "getFallbackTrending",
            commandProperties = {
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000"),
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "5"),
                    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50"),
                    @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "5000")
            })

    public TrendingResponse getTrendingMovies(int limit) {
        TrendingRequest request = TrendingRequest.newBuilder().setLimit(limit).build();
        return trendingServiceStub.getTrendingMovies(request);
    }

    public TrendingResponse getFallbackTrending(int limit) {
        // If gRPC is down, return an empty list of IDs to prevent a 500 error
        return TrendingResponse.newBuilder().addMovies(MovieScore.newBuilder().setMovieId("Error").setAverageScore(0.0).build())
                .build();
    }
}