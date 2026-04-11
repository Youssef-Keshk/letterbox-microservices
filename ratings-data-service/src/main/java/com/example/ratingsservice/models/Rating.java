package com.example.ratingsservice.models;
import javax.persistence.*;

@Entity
@Table(name = "ratings",
        indexes = {
                @Index(name = "idx_movieId", columnList = "movieId")
        }
)
@IdClass(RatingId.class)
public class Rating {

    @Id // Primary Key Part 1
    private String userId;
    @Id // Primary Key Part 2
    private String movieId;
    private double rating;

    public Rating() {
    }

    public Rating(String userId, String movieId, double rating) {
        this.userId = userId;
        this.movieId = movieId;
        this.rating = rating;
    }

    public String getMovieId() {
        return movieId;
    }
    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public double getRating() {
        return rating;
    }
    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
