package org.mhh.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class CityNotFoundException extends ApiException {

    public CityNotFoundException(String city) {
        super("Weather data not found for city: " + city);
    }

    public CityNotFoundException(String city, Throwable cause) {
        super("Weather data not found for city: " + city, cause);
    }
}