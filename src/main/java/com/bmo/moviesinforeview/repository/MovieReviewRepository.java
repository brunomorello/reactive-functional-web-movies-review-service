package com.bmo.moviesinforeview.repository;

import com.bmo.moviesinforeview.domain.MovieReview;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MovieReviewRepository extends ReactiveMongoRepository<MovieReview, String> {
    Flux<MovieReview> findReviewsByMoveInfoId(String moveInfoId);
}
