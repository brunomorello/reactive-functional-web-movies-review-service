package com.bmo.moviesinforeview.exceptionhandler;

import com.bmo.moviesinforeview.exception.ReviewDataException;
import com.bmo.moviesinforeview.exception.ReviewNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalErrorHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        LOGGER.error("Exception msg is: {}", ex.getMessage(), ex);

        DataBufferFactory dataBufferFactory = exchange.getResponse().bufferFactory();
        DataBuffer dataBufferWithErrorMsg = dataBufferFactory.wrap(ex.getMessage().getBytes(StandardCharsets.UTF_8));

        if (ex instanceof ReviewDataException) {
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            return exchange.getResponse().writeWith(Mono.just(dataBufferWithErrorMsg));
        }

        if (ex instanceof ReviewNotFoundException) {
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            return exchange.getResponse().writeWith(Mono.just(dataBufferWithErrorMsg));
        }

        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return exchange.getResponse().writeWith(Mono.just(dataBufferWithErrorMsg));
    }
}
