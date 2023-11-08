package com.bmo.moviesinforeview.router;

import com.bmo.moviesinforeview.handler.ReviewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ReviewRouter {

    private String ENDPOINT = "/v1/reviews";

    @Bean
    public RouterFunction<ServerResponse> reviewsRoute(ReviewHandler reviewHandler) {
        return route()
                .nest(path(ENDPOINT), builder ->
                    builder
                        .POST("", request -> reviewHandler.addReview(request))
                        .GET("", request -> reviewHandler.getAllReviews(request))
                        .PUT("/{id}", request -> reviewHandler.updateReview(request))
                        .DELETE("/{id}", request -> reviewHandler.deleteReview(request))
                        .GET("/stream", request -> reviewHandler.getReviewsStream(request))
                )
                .build();
    }
}
