package com.example.trendingmoviesservice.services;

import com.example.trendingmoviesservice.grpc.MovieScore;
import com.example.trendingmoviesservice.grpc.TrendingRequest;
import com.example.trendingmoviesservice.grpc.TrendingResponse;
import com.example.trendingmoviesservice.grpc.TrendingServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import java.util.Set;


// start up a gRPC server on port 9090 (by default)
@GrpcService
public class TrendingMoviesServiceImpl extends TrendingServiceGrpc.TrendingServiceImplBase {

    private static final String TRENDING_KEY = "trending:movies";

    @Autowired
    private StringRedisTemplate redisTemplate;

//    @Autowired
//    private RatingRepository ratingRepository;

    @Override
    public void getTrendingMovies(TrendingRequest request, StreamObserver<TrendingResponse> responseObserver) {
        int limit = (request.getLimit() > 0 && request.getLimit() < 11) ? request.getLimit() : 10; // Default to 10

        // ZREVRANGE fetches highest scores first
        Set<ZSetOperations.TypedTuple<String>> topMovies =
                redisTemplate.opsForZSet().reverseRangeWithScores(TRENDING_KEY, 0, limit - 1);

        TrendingResponse.Builder responseBuilder = TrendingResponse.newBuilder();

        if (topMovies != null) {
            for (ZSetOperations.TypedTuple<String> tuple : topMovies) {
                responseBuilder.addMovies(MovieScore.newBuilder()
                        .setMovieId(tuple.getValue())
                        .setAverageScore(tuple.getScore())
                        .build());
            }
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}