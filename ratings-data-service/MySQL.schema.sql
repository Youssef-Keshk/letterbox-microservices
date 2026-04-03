CREATE DATABASE movie_ratings_db;

USE movie_ratings_db;

CREATE TABLE IF NOT EXISTS ratings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id VARCHAR(255),
    movie_id VARCHAR(255),
    rating INT NOT NULL,
    PRIMARY KEY (id),

    UNIQUE KEY unique_user_movie (user_id, movie_id),
    INDEX idx_movie (movie_id)
);

CREATE TABLE IF NOT EXISTS movie_aggregates (
      movie_id VARCHAR(255) PRIMARY KEY,
      sum_ratings DOUBLE NOT NULL,
      num_ratings INT NOT NULL,
      avg_rating DOUBLE NOT NULL
);