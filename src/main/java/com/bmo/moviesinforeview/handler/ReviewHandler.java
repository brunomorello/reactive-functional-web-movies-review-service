package com.bmo.moviesinforeview.handler;

import com.bmo.moviesinforeview.domain.MovieReview;
import com.bmo.moviesinforeview.repository.MovieReviewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class ReviewHandler {

    private MovieReviewRepository movieReviewRepository;

    public ReviewHandler(MovieReviewRepository movieReviewRepository) {
        this.movieReviewRepository = movieReviewRepository;
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(MovieReview.class)
                .flatMap(movieReviewRepository::save)
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    public Mono<ServerResponse> getAllReviews(ServerRequest request) {
        Optional<String> moveInfoIdOpt = request.queryParam("moveInfoId");

        if (moveInfoIdOpt.isPresent()) {
            return getReviewByMoveInfoId(moveInfoIdOpt.get());
        }

        Flux<MovieReview> allMovieReviewFlux = movieReviewRepository.findAll();
        return ServerResponse.ok().body(allMovieReviewFlux, MovieReview.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        final String id = request.pathVariable("id");
        Mono<MovieReview> movieReviewFoundById = movieReviewRepository.findById(id).log();

        return movieReviewFoundById
                .flatMap(movieReview -> request.bodyToMono(MovieReview.class)
                        .map(requestReview -> {
                            movieReview.setComment(requestReview.getComment());
                            movieReview.setRating(requestReview.getRating());
                            return movieReview;
                        })
                        .flatMap(movieReviewRepository::save)
                        .log()
                        .flatMap(savedMovieReview -> ServerResponse.ok().bodyValue(savedMovieReview))
                )
              .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {
        final String id = request.pathVariable("id");
        Mono<MovieReview> movieReviewFoundById = movieReviewRepository.findById(id).log();

        return movieReviewFoundById
                .flatMap(movieReview -> movieReviewRepository.deleteById(id))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> getReviewByMoveInfoId(String moveInfoId) {
        Flux<MovieReview> movieReviewFlux = movieReviewRepository.findReviewsByMoveInfoId(moveInfoId).log();

        return ServerResponse.ok().body(movieReviewFlux, MovieReview.class);
    }
}
