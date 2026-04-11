package com.moviecatalogservice.models;

public class CatalogItem {

    private String name;
    private String description;
    private double rating;

    public CatalogItem() {

    }

    public CatalogItem(String name, String description, double rating) {
        this.name = name;
        this.description = description;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}
