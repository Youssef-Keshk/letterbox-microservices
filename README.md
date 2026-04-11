# spring-boot-microservices

A distributed microservices backend for a sample movie rating application (IMDB-like). Services communicate to deliver movie info, user ratings, and a real-time trending leaderboard.

---

## Technologies

- **Spring Boot** — Core framework for all microservices
- **Spring Cloud Eureka** — Service discovery
- **Spring Cloud Hystrix** — Circuit breaker, bulkhead pattern, and dashboard
- **gRPC** — High-performance communication for the trending service
- **MySQL** — Persistent storage for ratings and aggregates
- **Redis** — In-memory cache for the trending leaderboard

---

## Architecture Overview

### Original Architecture

<img src="https://github.com/user-attachments/assets/2106ef66-3dab-427f-812e-db1fa6df3a37" alt="Original Architecture" width="50%"/>

### Updated Architecture (with MySQL, gRPC & Trending Service)

<img src="https://github.com/user-attachments/assets/d00601cf-5dc0-485e-add9-f4bf728c0886" alt="New Architecture" width="50%"/>

### Final Architecture (with Redis Cache)

![Final Architecture with Redis](https://github.com/user-attachments/assets/0ea2abd4-c646-49d7-8c46-ce59745b7b71)

```
Client
  └── movie-catalog-service (API Gateway / BFF)
        ├── movie-info-service        (fetches metadata from TMDB)
        ├── ratings-data-service      (stores ratings in MySQL)
        └── trending-movies-service   (serves leaderboard from Redis via gRPC)

Infrastructure: Eureka (Discovery) · MySQL (Persistence) · Redis (Cache)
```

### Services

| Service | Port | Role |
|---|---|---|
| Discovery Server | 8761 | Eureka service registry |
| Movie Catalog | 8081 | API Gateway — orchestrates calls, aggregates responses |
| Movie Info | 8082 | Fetches & caches movie metadata from TMDB API |
| Ratings Data | 8083 | Handles rating submissions, writes to MySQL, updates cache |
| Trending Movies | 9090 | gRPC server — serves top-rated movies from Redis |

### Data & Background Jobs

- **MySQL** stores raw ratings and a pre-computed `movie_aggregates` table for fast reads.
- **Redis** maintains a sorted set (ZSET) of the Top 100 rated movies (minimum 3 ratings required).
- A **Spring `@Scheduled` job** inside the Trending service rebuilds the Redis cache from MySQL every 5 minutes.

### Fault Tolerance

`movie-catalog-service` uses **Hystrix** for resilience:

- If `ratings-data-service` is down → returns an empty list instead of a 500 error.
- If the TMDB API is down → returns placeholder text, keeping the rest of the app operational.
- Thread pools isolate downstream calls to prevent cascading failures.

---

## Running

Start **Discovery Server first**, then the remaining services in any order. Each can be run via your IDE or:

```bash
mvn spring-boot:run
```

### Endpoints

#### Discovery Server
| URL |
|---|
| http://localhost:8761 |

#### Hystrix Dashboard
Navigate to http://localhost:8081/hystrix and enter `http://localhost:8081/actuator/hystrix.stream` in the input box.
 
---

#### 1. API Gateway — `movie-catalog-service` (Port 8081)
The main entry point. These endpoints orchestrate calls to all downstream services.

| Method | URL | Description |
|---|---|---|
| `GET` | `http://localhost:8081/catalog/{userId}` | User's full catalog (movies + ratings) |
| `GET` | `http://localhost:8081/catalog/trending?limit={n}` | Top-rated movies from Redis via gRPC |
| `GET` | `http://localhost:8081/catalog/movie/{movieId}` | Raw TMDB movie details (proxy) |
| `POST` | `http://localhost:8081/catalog/rating/add` | Submit a new rating |

**POST body** (`Content-Type: application/json`):
```json
{
  "userId": "User-1",
  "movieId": "550",
  "rating": 5.0
}
```
 
---

#### 2. Ratings Service — `ratings-data-service` (Port 8083)
Direct access to the ratings database, bypassing the Gateway.

| Method | URL | Description |
|---|---|---|
| `GET` | `http://localhost:8083/ratings/users/{userId}` | Raw ratings for a user |
| `POST` | `http://localhost:8083/ratings/add` | Submit a rating directly |
 
---

#### 3. Movie Info Service — `movie-info-service` (Port 8082)

| Method | URL | Description |
|---|---|---|
| `GET` | `http://localhost:8082/movies/{movieId}` | Title & description from TMDB API |
 
---

#### 4. Trending Service — `trending-movies-service` (Port 9090 — gRPC)
This service runs over HTTP/2 (gRPC) and **cannot be tested in a browser**. Use [Postman](https://www.postman.com/) (select *gRPC Request* and import `trending.proto`) or `grpcurl`.

| Protocol | Host | Method | Example Payload |
|---|---|---|---|
| `grpc://` | `localhost:9090` | `GetTrendingMovies` | `{"limit": 5}` |
 
---

## External Dependencies

- **TMDB API** — A valid API key is required for `movie-info-service` to fetch movie metadata. Set it in the service's `application.properties`.
- **MySQL** — Required by `ratings-data-service`. Configure connection details in its `application.properties`.
- **Redis** — Required by `trending-movies-service`. Defaults to `localhost:6379`.
 