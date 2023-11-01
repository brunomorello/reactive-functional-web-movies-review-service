package com.bmo.moviesinforeview.handler;

import com.bmo.moviesinforeview.domain.MovieReview;
import com.bmo.moviesinforeview.exception.ReviewDataException;
import com.bmo.moviesinforeview.exception.ReviewNotFoundException;
import com.bmo.moviesinforeview.repository.MovieReviewRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ReviewHandler {

    private MovieReviewRepository movieReviewRepository;

    private Validator validator;

    public ReviewHandler(MovieReviewRepository movieReviewRepository, Validator validator) {
        this.movieReviewRepository = movieReviewRepository;
        this.validator = validator;
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(MovieReview.class)
                .doOnNext(this::validateBody)
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
        Mono<MovieReview> movieReviewFoundById = movieReviewRepository.findById(id).log()
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Movie Review Not found")));

        return movieReviewFoundById
                .flatMap(movieReview -> request.bodyToMono(MovieReview.class)
                        .doOnNext(this::validateBody)
                        .map(requestReview -> {
                            movieReview.setComment(requestReview.getComment());
                            movieReview.setRating(requestReview.getRating());
                            return movieReview;
                        })
                        .flatMap(movieReviewRepository::save)
                        .log()
                        .flatMap(savedMovieReview -> ServerResponse.ok().bodyValue(savedMovieReview))
                );
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {
        final String id = request.pathVariable("id");
        Mono<MovieReview> movieReviewFoundById = movieReviewRepository.findById(id).log()
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Movie Review Not found")));

        return movieReviewFoundById
                .flatMap(movieReview -> movieReviewRepository.deleteById(id))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> getReviewByMoveInfoId(String moveInfoId) {
        Flux<MovieReview> movieReviewFlux = movieReviewRepository.findReviewsByMoveInfoId(moveInfoId).log()
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Movie Review Not found")));

        return ServerResponse.ok().body(movieReviewFlux, MovieReview.class);
    }

    private void validateBody(MovieReview movieReview) {
        Set<ConstraintViolation<MovieReview>> constraintViolations = validator.validate(movieReview);

        if (constraintViolations.size() > 0) {
            final String errors = constraintViolations.stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(", "));
            throw new ReviewDataException(errors);
        }
    }
}
