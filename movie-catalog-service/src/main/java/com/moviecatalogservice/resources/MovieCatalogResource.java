package com.moviecatalogservice.resources;

import com.moviecatalogservice.models.CatalogItem;
import com.moviecatalogservice.models.Movie;
import com.moviecatalogservice.models.Rating;
import com.moviecatalogservice.services.MovieInfoService;
import com.moviecatalogservice.services.UserRatingService;
import com.moviecatalogservice.services.TrendingMoviesService;

import com.example.trendingmoviesservice.grpc.TrendingResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/catalog")
public class MovieCatalogResource {

    private final MovieInfoService movieInfoService;
    private final UserRatingService userRatingService;
    private final TrendingMoviesService trendingMoviesService;

    public MovieCatalogResource(MovieInfoService movieInfoService,
                                UserRatingService userRatingService,
                                TrendingMoviesService trendingServiceProxy) {
        this.movieInfoService = movieInfoService;
        this.userRatingService = userRatingService;
        this.trendingMoviesService = trendingServiceProxy;
    }

    // View User Catalog
    @RequestMapping("/{userId}")
    public List<CatalogItem> getCatalog(@PathVariable String userId) {
        List<Rating> ratings = userRatingService.getUserRating(userId).getRatings();
        return ratings.stream().map(movieInfoService::getCatalogItem).collect(Collectors.toList());
    }

    // View Trending Movies
    // Example URL: http://localhost:8081/catalog/trending?limit=5
    @RequestMapping("/trending")
    public List<CatalogItem> getTrendingMovies(@RequestParam(value = "limit", defaultValue = "10") int limit) {
        // Get the Top $limit IDs
        TrendingResponse response = trendingMoviesService.getTrendingMovies(limit);

        // Convert IDs to Ratings, and ask MovieInfoService for the text
        return response.getMoviesList().stream().map(movieScore -> {
            Rating rating = new Rating(movieScore.getMovieId(), movieScore.getAverageScore());
            return movieInfoService.getCatalogItem(rating);
        }).collect(Collectors.toList());
    }

    // Add a New Rating
    @PostMapping("/ratings/add")
    public ResponseEntity<String> addRating(@RequestBody Rating rating) {
        String resultMessage = userRatingService.addRating(rating);
        return ResponseEntity.ok(resultMessage);
    }

    // View Movie Info
    @GetMapping("/movie/{movieId}")
    public Movie getMovieInfo(@PathVariable String movieId) {
        return movieInfoService.getMovieInfo(movieId);
    }
}
