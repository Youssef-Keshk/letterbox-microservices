package com.example.movieinfoservice.resources;

import com.example.movieinfoservice.models.Movie;
import com.example.movieinfoservice.models.MovieSummary;
import com.example.movieinfoservice.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@RestController
@RequestMapping("/movies")
public class MovieResource {

    @Value("${api.key}")
    private String apiKey;

    private RestTemplate restTemplate;

    private MovieRepository movieRepository;

    public MovieResource(RestTemplate restTemplate, MovieRepository movieRepository) {
        this.restTemplate = restTemplate;
        this.movieRepository = movieRepository;
    }

    @RequestMapping("/{movieId}")
    public Movie getMovieInfo(@PathVariable("movieId") String movieId) throws InterruptedException {
        try {
            Optional<Movie> cachedMovie = movieRepository.findById(movieId);

            // CACHE HIT
            if (cachedMovie.isPresent()) {
                System.out.println("Cache HIT: Returning movie " + movieId + " from MongoDB");
                return cachedMovie.get();
            }
            // CACHE MISS
            System.out.println("Cache MISS: Fetching movie " + movieId + " from TMDB API");

        } catch (Exception e) {
            System.out.println("WARNING: MongoDB is down or unreachable. Skipping cache lookup.");
        }

        try {
//            // Simulate 3rd party DB delay (during testing)
//            Thread.sleep(3000);

            // Get the movie info from TMDB
            final String url = "https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + apiKey;
            MovieSummary movieSummary = restTemplate.getForObject(url, MovieSummary.class);

            Movie movie = new Movie(movieId, movieSummary.getTitle(), movieSummary.getOverview());
            try {
                movieRepository.save(movie);
            } catch (Exception e) {
                System.out.println("WARNING: Could not save to MongoDB cache.");
            }
            return movie;
        }
        catch (Exception e) {
            System.out.println("ERROR: TMDB API Failed for ID " + movieId + ". Returning fallback.");
            return new Movie(movieId, "Unknown Movie (TMDB Error)", "Could not fetch details from TMDB.");
        }
    }
}
