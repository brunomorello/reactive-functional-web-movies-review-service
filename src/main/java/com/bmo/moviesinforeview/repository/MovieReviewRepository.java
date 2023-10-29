package com.bmo.moviesinforeview.repository;

import com.bmo.moviesinforeview.domain.MovieReview;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieReviewRepository extends ReactiveMongoRepository<MovieReview, String> {
}
