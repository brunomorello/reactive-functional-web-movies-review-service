package com.bmo.moviesinforeview.exception;

public class ReviewDataException extends RuntimeException {
    private String msg;

    public ReviewDataException(String message) {
        super(message);
        this.msg = message;
    }
}
