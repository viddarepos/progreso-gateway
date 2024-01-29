package com.prime.gateway.gateway.infrastructure.exception;

public class JsonParsingException extends RuntimeException {

    public JsonParsingException(Throwable rootCause) {
        super(rootCause);
    }
}
