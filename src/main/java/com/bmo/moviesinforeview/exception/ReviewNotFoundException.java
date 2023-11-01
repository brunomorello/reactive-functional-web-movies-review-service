package com.bmo.moviesinforeview.exception;

public class ReviewNotFoundException extends RuntimeException {
    private String message;

    public ReviewNotFoundException(String message) {
        super(message);
        this.message = message;
    }
}
