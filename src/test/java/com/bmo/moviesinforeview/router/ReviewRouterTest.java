package com.bmo.moviesinforeview.router;

import com.bmo.moviesinforeview.domain.MovieReview;
import com.bmo.moviesinforeview.exceptionhandler.GlobalErrorHandler;
import com.bmo.moviesinforeview.handler.ReviewHandler;
import com.bmo.moviesinforeview.repository.MovieReviewRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalErrorHandler.class})
@AutoConfigureWebTestClient
class ReviewRouterTest {

    @MockBean
    private MovieReviewRepository repository;

    @Autowired
    private WebTestClient webTestClient;

    private static final String API_URL = "/v1/reviews";

    @Test
    void when_POST_new_review_then_create_it() {
        var review = MovieReview.builder()
                .id(null)
                .moveInfoId("1SW")
                .comment("Best movie ever")
                .rating(10.0)
                .build();

        when(repository.save(isA(MovieReview.class)))
                .thenReturn(Mono.just(MovieReview.builder()
                        .id(UUID.randomUUID().toString())
                        .moveInfoId("1SW")
                        .comment("Best movie ever")
                        .rating(10.0)
                        .build()));

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
    void when_POST_new_review_with_incorrect_data_then_bad_request() {
        var review = MovieReview.builder()
                .id(null)
                .moveInfoId(null)
                .comment("Best movie ever")
                .rating(-10.0)
                .build();

        webTestClient.post()
                .uri(API_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .isEqualTo("rating.move: value must not be null, rating.negative: rating is negative, pls provide a positive value");
    }

    @Test
    void when_GET_without_id_then_return_all_movies_review() {
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

        when(repository.findAll()).thenReturn(Flux.fromIterable(reviewList));

        webTestClient.get()
                .uri(API_URL)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(MovieReview.class)
                .hasSize(3)
                .consumeWith(listEntityExchangeResult -> {
                    List<MovieReview> movieReviewList = listEntityExchangeResult.getResponseBody();
                    movieReviewList.forEach(movieReview -> System.out.println(movieReview));
                    assertFalse(movieReviewList.isEmpty());
                });
    }

    @Test
    void when_PUT_then_update_movie_review() {
        final var movieReviewId = UUID.randomUUID().toString();

        final MovieReview movieReview = MovieReview.builder()
                .id(movieReviewId)
                .moveInfoId("1SW")
                .comment("Best movie ever")
                .rating(10.0)
                .build();

        when(repository.findById(Mockito.anyString()))
                .thenReturn(Mono.just(
                        MovieReview.builder()
                                .id(movieReviewId)
                                .moveInfoId("1SW")
                                .comment("Nice movie")
                                .rating(5.0)
                                .build()
                ));
        when(repository.save(isA(MovieReview.class)))
                .thenReturn(Mono.just(movieReview));

        webTestClient.put()
                .uri(API_URL + "/{id}", movieReviewId)
                .bodyValue(movieReview)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(MovieReview.class)
                .consumeWith(movieReviewEntityExchangeResult -> {
                    MovieReview responseBody = movieReviewEntityExchangeResult.getResponseBody();
                    assertEquals("Best movie ever", responseBody.getComment());
                    assertEquals(10.0, responseBody.getRating());
                });
    }

    @Test
    void when_PUT_with_invalid_payload_then_bad_request() {
        final var movieReviewId = UUID.randomUUID().toString();

        final MovieReview movieReview = MovieReview.builder()
                .id(movieReviewId)
                .moveInfoId(null)
                .comment("Best movie ever")
                .rating(-10.0)
                .build();

        when(repository.findById(Mockito.anyString()))
                .thenReturn(Mono.just(
                        MovieReview.builder()
                                .id(movieReviewId)
                                .moveInfoId("1SW")
                                .comment("Nice movie")
                                .rating(5.0)
                                .build()
                ));

        webTestClient.put()
                .uri(API_URL + "/{id}", movieReviewId)
                .bodyValue(movieReview)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    final String responseBody = stringEntityExchangeResult.getResponseBody();
                    assertEquals("rating.move: value must not be null, rating.negative: rating is negative, pls provide a positive value", responseBody);
                });
    }

    @Test
    void when_PUT_with_inexistent_id_then_return_not_found() {
        MovieReview movieReview = MovieReview.builder()
                .moveInfoId("1SW")
                .comment("Best movie ever")
                .rating(10.0)
                .build();

        when(repository.findById(anyString())).thenReturn(Mono.empty());

        webTestClient.put()
                .uri(API_URL + "/EST12312")
                .bodyValue(movieReview)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    final String responsBody = stringEntityExchangeResult.getResponseBody();
                    assertEquals("Movie Review Not found", responsBody);
                });
    }

    @Test
    void when_DELETE_then_do_it() {
        MovieReview movieReview = MovieReview.builder()
                .moveInfoId("1231SW")
                .comment("Best movie ever")
                .rating(10.0)
                .build();

        when(repository.findById(anyString())).thenReturn(Mono.just(movieReview));
        when(repository.deleteById(anyString())).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(API_URL + "/{id}", "123124")
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void when_DELETE_inexistent_moview_review_then_not_found() {
        MovieReview movieReview = MovieReview.builder()
                .moveInfoId("1231SW")
                .comment("Best movie ever")
                .rating(10.0)
                .build();

        when(repository.findById(anyString())).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(API_URL + "/{id}", "123124")
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    final String responsBody = stringEntityExchangeResult.getResponseBody();
                    assertEquals("Movie Review Not found", responsBody);
                });
    }

    @Test
    void when_GET_review_by_movieInfoId_then_return_data() {

        var moviesReviewList = List.of(MovieReview.builder()
                .id(UUID.randomUUID().toString())
                .moveInfoId("1SW")
                .comment("Nice movie")
                .rating(5.0)
                .build());

        when(repository.findReviewsByMoveInfoId(Mockito.anyString()))
                .thenReturn(Flux.fromIterable(moviesReviewList));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(API_URL)
                        .queryParam("moveInfoId", "1SW")
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(MovieReview.class)
                .hasSize(1)
                .consumeWith(listEntityExchangeResult -> {
                    List<MovieReview> movieReviewList = listEntityExchangeResult.getResponseBody();
                    assertFalse(movieReviewList.isEmpty());
                });
    }

    @Test
    void when_GET_review_by_movieInfoId_inexistent_then_not_found() {
        when(repository.findReviewsByMoveInfoId(Mockito.anyString()))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(API_URL)
                        .queryParam("moveInfoId", "21SW")
                        .build())
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    final String responsBody = stringEntityExchangeResult.getResponseBody();
                    assertEquals("Movie Review Not found", responsBody);
                });
    }
}