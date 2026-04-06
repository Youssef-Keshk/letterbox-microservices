package com.example.ratingsservice.services;

import com.example.ratingsservice.models.Rating;
import com.example.ratingsservice.repositories.RatingRepository;
import com.example.ratingsservice.repositories.MovieAggregateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class RatingSubmissionService {

    private static final String TRENDING_KEY = "trending:movies";
    private static final int MIN_RATINGS = 3;
    private static final int MAX_TRENDING = 100;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private MovieAggregateRepository aggregateRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void saveRatingAndUpdateTrending(Rating rating) {
        // Save rating to MySQL
        ratingRepository.save(rating);

        // Atomically update aggregates in MySQL
        aggregateRepository.upsertAggregate(rating.getMovieId(), rating.getRating());

        // Fetch the newly calculated stats
        Object[][] stats = aggregateRepository.getAggregateStats(rating.getMovieId());
        // Parse BigDecimal and BigInteger from MySQL
        double avgRating = ((Number) stats[0][0]).doubleValue();
        int numRatings = ((Number) stats[0][1]).intValue();

        // Redis ZSET
        String movieIdStr = rating.getMovieId();
        Double currentScore = redisTemplate.opsForZSet().score(TRENDING_KEY, movieIdStr);
        boolean existsInRedis = (currentScore != null);

        if (existsInRedis || numRatings >= MIN_RATINGS) {

            if (!existsInRedis && redisTemplate.opsForZSet().size(TRENDING_KEY) >= MAX_TRENDING) {
                // Check if it's greater than the lowest score in the Top 100
                Set<ZSetOperations.TypedTuple<String>> lowest =
                        redisTemplate.opsForZSet().rangeWithScores(TRENDING_KEY, 0, 0);

                if (lowest != null && !lowest.isEmpty()) {
                    double lowestScore = lowest.iterator().next().getScore();
                    if (avgRating <= lowestScore) {
                        return; // Not high enough. Skip Redis.
                    }
                }
            }

            // Add or update the score
            redisTemplate.opsForZSet().add(TRENDING_KEY, movieIdStr, avgRating);

            // Trim the ZSET to keep only the top 100 (ZSET is sorted ascending, so drop from index 0)
            long size = redisTemplate.opsForZSet().size(TRENDING_KEY);
            if (size > MAX_TRENDING) {
                redisTemplate.opsForZSet().removeRange(TRENDING_KEY, 0, size - MAX_TRENDING - 1);
            }
        }
    }
}