package io.spring.batch.springbatch.exception;

public class RetryableException extends RuntimeException {
    public RetryableException() {
        super();
    }

    public RetryableException(String s) {
        super(s);
    }
}
