package com.example.trendingmoviesservice.jobs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class TrendingSyncJob {

    private static final String TRENDING_KEY = "trending:movies";
    private static final String TEMP_KEY = "trending:movies:temp";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    // Runs every 5 minutes
    @Scheduled(fixedRate = 300000)
    public void recomputeTrendingCache() {
        // Query the aggregate table
        String sql = "SELECT movie_id, avg_rating FROM movie_aggregates " +
                "WHERE num_ratings >= 3 " +
                "ORDER BY avg_rating DESC LIMIT 100";

        List<Map<String, Object>> topMovies = jdbcTemplate.queryForList(sql);

        // Build into a temporary Redis key first to prevent zero-downtime gaps
        redisTemplate.delete(TEMP_KEY);
        for (Map<String, Object> row : topMovies) {
            String movieId = String.valueOf(row.get("movie_id"));
            Double avgRating = (Double) row.get("avg_rating");
            redisTemplate.opsForZSet().add(TEMP_KEY, movieId, avgRating);
        }

        // Rename the temp key to the active key
        redisTemplate.rename(TEMP_KEY, TRENDING_KEY);
    }
}