package com.bmo.moviesinforeview.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class HelloWorldRouter {

    @Bean
    public RouterFunction<ServerResponse> reviewsRoute() {
        return route()
                .GET("/hello-world", (request -> ServerResponse.ok().bodyValue("Hello World Functional Web")))
                .build();
    }
}
