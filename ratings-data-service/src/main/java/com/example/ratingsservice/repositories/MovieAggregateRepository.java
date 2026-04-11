package com.example.ratingsservice.repositories;

import com.example.ratingsservice.models.MovieAggregate;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface MovieAggregateRepository extends CrudRepository<MovieAggregate, String> {

    // Atomic Upsert: Prevents race conditions when 100 users rate the same movie simultaneously.
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO movie_aggregates (movie_id, sum_ratings, num_ratings, avg_rating) " +
            "VALUES (:movieId, :rating, 1, :rating) " +
            "ON DUPLICATE KEY UPDATE " +
            "sum_ratings = sum_ratings + :rating, " +
            "num_ratings = num_ratings + 1, " +
            "avg_rating = sum_ratings / num_ratings", nativeQuery = true)
    void upsertAggregate(String movieId, double rating);

    @Query(value = "SELECT avg_rating, num_ratings FROM movie_aggregates WHERE movie_id = :movieId", nativeQuery = true)
    Object[][] getAggregateStats(String movieId);
}