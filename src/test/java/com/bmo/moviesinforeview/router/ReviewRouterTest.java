package com.bmo.moviesinforeview.router;

import com.bmo.moviesinforeview.domain.MovieReview;
import com.bmo.moviesinforeview.repository.MovieReviewRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class ReviewRouterTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private MovieReviewRepository repository;

    private static final String API_URL = "/v1/reviews";

    @BeforeEach
    void setUp() {
        var reviewList = List.of(
                MovieReview.builder()
                        .id(null)
                        .moveInfoId("1SW")
                        .comment("Great Movie")
                        .rating(9.0)
                        .build(),
                MovieReview.builder()
                        .id(null)
                        .moveInfoId("1SW")
                        .comment("Good Movie")
                        .rating(8.0)
                        .build(),
                MovieReview.builder()
                        .id(null)
                        .moveInfoId("1SW")
                        .comment("Worst Movie Ever")
                        .rating(1.0)
                        .build()
        );
        repository.saveAll(reviewList).blockLast();
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll().block();
    }

    @Test
    void when_POST_new_review_then_create_it() {
        var review = MovieReview.builder()
                .id(null)
                .moveInfoId("1SW")
                .comment("Best movie ever")
                .rating(10.0)
                .build();

        webTestClient.post()
                .uri(API_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieReview.class)
                .consumeWith(movieReviewEntityExchangeResult -> {
                    MovieReview responseBody = movieReviewEntityExchangeResult.getResponseBody();
                    assertNotNull(responseBody.getId());
                    assertEquals("1SW", responseBody.getMoveInfoId());
                    assertEquals("Best movie ever", responseBody.getComment());
                    assertEquals(10.0, responseBody.getRating());
                });
    }

    @Test
    void when_GET_without_id_then_return_all_movies_review() {
        webTestClient.get()
                .uri(API_URL)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(MovieReview.class)
                .consumeWith(listEntityExchangeResult -> {
                    List<MovieReview> movieReviewList = listEntityExchangeResult.getResponseBody();
                    movieReviewList.forEach(movieReview -> System.out.println(movieReview));
                    assertFalse(movieReviewList.isEmpty());
                });
    }

    @Test
    void when_PUT_then_update_movie_review() {
        MovieReview movieReview = repository.save(MovieReview.builder()
                .moveInfoId("1SW")
                .comment("Best movie ever")
                .rating(10.0)
                .build()
        ).block();

        movieReview.setRating(2);
        movieReview.setComment("Sucks");

        webTestClient.put()
                .uri(API_URL + "/{id}", movieReview.getId())
                .bodyValue(movieReview)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(MovieReview.class)
                .consumeWith(movieReviewEntityExchangeResult -> {
                    MovieReview responseBody = movieReviewEntityExchangeResult.getResponseBody();
                    assertEquals("Sucks", responseBody.getComment());
                    assertEquals(2, responseBody.getRating());
                });
    }

    @Test
    void when_PUT_with_inexistent_id_then_return_not_found() {
        MovieReview movieReview = MovieReview.builder()
                .moveInfoId("1SW")
                .comment("Best movie ever")
                .rating(10.0)
                .build();

        webTestClient.put()
                .uri(API_URL + "/EST12312")
                .bodyValue(movieReview)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void when_DELETE_then_do_it() {
        MovieReview movieReview = MovieReview.builder()
                .moveInfoId("1231SW")
                .comment("Best movie ever")
                .rating(10.0)
                .build();

        MovieReview movieReviewCreated = repository.save(movieReview).block();

        webTestClient.delete()
                .uri(API_URL + "/{id}", movieReviewCreated.getId())
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void when_GET_review_by_movieInfoId_then_return_data() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(API_URL)
                        .queryParam("moveInfoId", "1SW")
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(MovieReview.class)
                .hasSize(3)
                .consumeWith(listEntityExchangeResult -> {
                    List<MovieReview> movieReviewList = listEntityExchangeResult.getResponseBody();
                    assertFalse(movieReviewList.isEmpty());
                });
    }

    @Test
    void when_GET_review_by_movieInfoId_inexistent_then_not_found() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(API_URL)
                        .queryParam("moveInfoId", "21SW")
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(MovieReview.class)
                .hasSize(0);
    }

}