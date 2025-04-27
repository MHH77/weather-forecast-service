package org.mhh.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
public class ExternalApiException extends ApiException {

    public ExternalApiException(String message) {
        super("Error communicating with external weather service: " + message);
    }

    public ExternalApiException(String message, Throwable cause) {
        super("Error communicating with external weather service: " + message, cause);
    }
}