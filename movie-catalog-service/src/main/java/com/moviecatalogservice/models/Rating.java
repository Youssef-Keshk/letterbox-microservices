package com.moviecatalogservice.models;

public class Rating {


    private String userId;
    private String movieId;
    private double rating;

    public Rating() {
    }

    public Rating(String movieId, double rating) {
        this.movieId = movieId;
        this.rating = rating;
    }

    public Rating(String userId, String movieId, double rating) {
        this.userId = userId;
        this.movieId = movieId;
        this.rating = rating;
    }

    public String getUserId() {return userId;}
    public void setUserId(String userId) {this.userId = userId;}

    public String getMovieId() {return movieId;}
    public void setMovieId(String movieId) {this.movieId = movieId;}

    public double getRating() {
        return rating;
    }
    public void setRating(double rating) {this.rating = rating;}
}
