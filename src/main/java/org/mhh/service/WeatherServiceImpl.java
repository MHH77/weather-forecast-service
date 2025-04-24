package org.mhh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class WeatherServiceImpl implements WeatherService {

    private final RestTemplate restTemplate;

    @Value("${openweathermap.api.key}")
    private String apiKey;

    @Value("${openweathermap.api.url}")
    private String apiUrl;

    @Value("${openweathermap.api.units}")
    private String apiUnits;

    @Autowired
    public WeatherServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Object getWeatherData(String city) {
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("q", city)
                .queryParam("appid", apiKey)
                .queryParam("units", apiUnits)
                .toUriString();

        System.out.println("Requesting URL: " + url);

        try {
            Object response = restTemplate.getForObject(url, Object.class);
            System.out.println("Received response: " + response);
            return response;
        } catch (Exception e) {
            System.err.println("Error calling OpenWeatherMap API: " + e.getMessage());
            // TODO: Implement proper error handling
            return null;
        }
    }
}