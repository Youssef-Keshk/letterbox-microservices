package com.example.ratingsservice.resources;

import com.example.ratingsservice.models.Rating;
import com.example.ratingsservice.models.UserRating;
import com.example.ratingsservice.repositories.RatingRepository;
import com.example.ratingsservice.services.RatingSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ratings")
public class RatingsResource {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private RatingSubmissionService ratingSubmissionService;

    @RequestMapping("users/{userId}")
    public UserRating getRatingsOfUser(@PathVariable String userId) {
        List<Rating> userRatings = ratingRepository.findByUserId(userId);

        return new UserRating(userRatings);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addRating(@RequestBody Rating rating) {

        if (rating.getRating() < 0.0 || rating.getRating() > 5.0) {
            return ResponseEntity.badRequest().body("Validation Error: Rating must be between 0.0 and 5.0");
        }
        if (rating.getUserId().isEmpty() || rating.getMovieId().isEmpty()) {
            return ResponseEntity.badRequest().body("Validation Error: Invalid User ID or Movie ID");
        }

        try {
            // Call the service that handles MySQL and Redis logic
            ratingSubmissionService.saveRatingAndUpdateTrending(rating);
            return ResponseEntity.ok("Rating saved successfully and trending cache updated.");

        } catch (Exception e) {
            // If the user tries to rate the exact same movie twice, our MySQL UNIQUE KEY will throw an error
            return ResponseEntity.status(500).body("Error saving rating. Have you already rated this movie?");
        }
    }
}
