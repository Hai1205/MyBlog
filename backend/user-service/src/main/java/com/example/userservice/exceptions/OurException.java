package com.example.userservice.exceptions;

public class OurException extends RuntimeException {
    private int statusCode;

    public OurException(String message) {
        super(message);
        this.statusCode = 400;
    }

    public OurException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}