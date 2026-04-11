package com.example.ratingsservice.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "movie_aggregates")
public class MovieAggregate {

    @Id
    private String movieId;
    private double sumRatings;
    private int numRatings;
    private double avgRating;

    // Empty constructor for JPA
    public MovieAggregate() {}

    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }

    public double getSumRatings() { return sumRatings; }
    public void setSumRatings(double sumRatings) { this.sumRatings = sumRatings; }

    public int getNumRatings() { return numRatings; }
    public void setNumRatings(int numRatings) { this.numRatings = numRatings; }

    public double getAvgRating() { return avgRating; }
    public void setAvgRating(double avgRating) { this.avgRating = avgRating; }
}