package org.mhh.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    @GetMapping
    public ResponseEntity<?> getWeatherForecast(@RequestParam String city) {

        System.out.println("Received request for city: " + city);
        if (city == null || city.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("City parameter cannot be empty.");
        }
        Map<String, Object> dummyResponse = new HashMap<>();
        dummyResponse.put("city", city);
        dummyResponse.put("temperature", 25.5);
        dummyResponse.put("description", "Sunny");
        dummyResponse.put("message", "Data is hardcoded for now!");
        return ResponseEntity.ok(dummyResponse);
    }
}