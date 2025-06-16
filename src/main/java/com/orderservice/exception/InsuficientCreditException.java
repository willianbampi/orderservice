package com.orderservice.exception;

public class InsuficientCreditException extends RuntimeException {

    public InsuficientCreditException(String message) {
        super(message);
    }

}
