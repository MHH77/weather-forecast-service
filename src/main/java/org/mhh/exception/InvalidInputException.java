package org.mhh.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidInputException extends ApiException {

    public InvalidInputException(String message) {
        super("Invalid input provided: " + message);
    }
}