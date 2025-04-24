package org.mhh.controller;

import org.mhh.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    public ResponseEntity<?> getWeatherForecast(@RequestParam String city) {

        System.out.println("Received request for city: " + city);

        if (city == null || city.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("City parameter cannot be empty.");
        }
        Object weatherData = weatherService.getWeatherData(city);
        if (weatherData == null) {
            return ResponseEntity.status(503).body("Could not retrieve weather data at the moment.");
        }

        return ResponseEntity.ok(weatherData);
    }
}